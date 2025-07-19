package rs.irm.common.service.impl;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCrypt;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Context;
import rs.irm.administration.entity.AppUser;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.common.dto.LoginDTO;
import rs.irm.common.enums.Language;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.service.LoginService;
import rs.irm.common.service.TokenService;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.TableFilter;
import rs.irm.utils.AppParameters;

@Named
public class LoginServiceImpl implements LoginService {

	@Inject
	private DatatableService datatableService;

	@Inject
	private TokenService tokenService;

	@Context
	private HttpServletResponse servletResponse;

	@Context
	private HttpServletRequest servletRequest;

	@SuppressWarnings("static-access")
	@Override
	public void login(LoginDTO loginDTO) {


		TableParameterDTO tableParameterDTO = new TableParameterDTO();

		this.datatableService = this.datatableService == null ? new DatatableServiceImpl() : this.datatableService;
		this.tokenService = this.tokenService == null ? new TokenServiceImpl() : tokenService;

		TableFilter tableFilter = new TableFilter();
		tableFilter.setField("username");
		tableFilter.setParameter1(loginDTO.getUsername());
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableParameterDTO.getTableFilters().add(tableFilter);
		List<AppUser> appUsers = datatableService.findAll(tableParameterDTO, AppUser.class);

		if (appUsers.isEmpty()) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "wrongUsername", null);
		}

		AppUser appUser = appUsers.get(0);

		if (!appUser.getActive()) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "userIsNotActive", appUser.getUsername());
		}

		if (!new BCrypt().checkpw(loginDTO.getPassword(), appUser.getPassword())) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "wrongPassword", null);
		}
		Long durationInSeconds=AppParameters.sessionduration/1000;

		
		String token = tokenService.generateToken(loginDTO.getUsername(), loginDTO.getLanguage());
		Cookie cookie = new Cookie("session", token);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setMaxAge(durationInSeconds.intValue());
		cookie.setPath(AppParameters.production ? AppParameters.baseurl : "/");
		cookie.setAttribute("SameSite", "Strict");
		servletResponse.addCookie(cookie);

		cookie = new Cookie("lang", loginDTO.getLanguage().name());
		cookie.setHttpOnly(false);
		cookie.setPath(AppParameters.production ? AppParameters.baseurl : "/");
		servletResponse.addCookie(cookie);

	}

	@Override
	public List<ComboboxDTO> language() {
		List<ComboboxDTO> comboboxDTOs = new ArrayList<>();

		Map<Language, String> description = new HashMap<>();
		description.put(Language.srRS, "Српски");
		description.put(Language.srLatnRS, "Srpski");
		description.put(Language.enUS, "English");

		Language defaultLanguage = Language.valueOf(AppParameters.defaultlang);
		ComboboxDTO comboboxDTO = new ComboboxDTO(defaultLanguage.name(), description.get(defaultLanguage));
		comboboxDTOs.add(comboboxDTO);
		for (Language lang : Arrays.asList(Language.values())) {

			if (lang.equals(defaultLanguage)) {
				continue;
			}

			ComboboxDTO combobox = new ComboboxDTO(lang.name(), description.get(lang));
			comboboxDTOs.add(combobox);
		}

		return comboboxDTOs;
	}

	@Override
	public void logout() {
		Cookie cookie = new Cookie("session", "");
		cookie.setHttpOnly(true);
		cookie.setMaxAge(0);
		cookie.setPath(AppParameters.production ? AppParameters.baseurl : "/");
		servletResponse.addCookie(cookie);

	}
}
