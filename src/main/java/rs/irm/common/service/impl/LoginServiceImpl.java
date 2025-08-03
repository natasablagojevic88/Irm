package rs.irm.common.service.impl;

import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
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
import rs.irm.common.entity.TokenDatabase;
import rs.irm.common.enums.Language;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.LoginService;
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

	@Context
	private HttpServletResponse servletResponse;

	@Context
	private HttpServletRequest servletRequest;

	@Inject
	private CommonService commonService;

	@SuppressWarnings("static-access")
	@Override
	public void login(LoginDTO loginDTO) {

		TableParameterDTO tableParameterDTO = new TableParameterDTO();

		this.datatableService = this.datatableService == null ? new DatatableServiceImpl() : this.datatableService;

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

		TokenDatabase tokenDatabase = insertTokenToBase(0L, appUser, true);
		servletResponse.addCookie(createCookie("session", tokenDatabase.getSessionToken(),
				AppParameters.refreshtokenduration.intValue() * 60));
		servletResponse.addCookie(createCookie("refresh_token", tokenDatabase.getRefreshToken(),
				AppParameters.refreshtokenduration.intValue() * 60));
		servletResponse.addCookie(createCookie("lang", loginDTO.getLanguage().name(),
				AppParameters.refreshtokenduration.intValue() * 60));
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

		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("sessionToken", SearchOperation.equals, commonService.getSession(), null));
		List<TokenDatabase> tokenDatabaseDTOs = this.datatableService.findAll(tableParameterDTO, TokenDatabase.class);

		TokenDatabase tokenDatabaseDTO = tokenDatabaseDTOs.get(0);
		this.insertTokenToBase(tokenDatabaseDTO.getId(), tokenDatabaseDTO.getAppUser(), false);

		servletResponse.addCookie(createCookie("session", null, 0));
		servletResponse.addCookie(createCookie("refresh_token", null, 0));
		servletResponse.addCookie(createCookie("lang", null, 0));

	}

	@Override
	public Cookie createCookie(String name, String value, Integer duration) {
		Cookie cookie = new Cookie(name, value);
		cookie.setHttpOnly(name.equals("lang") ? false : true);
		cookie.setSecure(true);
		cookie.setMaxAge(duration);
		cookie.setPath(AppInitServiceImpl.contextPath == null ? "/"
				: AppInitServiceImpl.contextPath.length() == 0 ? "/" : AppInitServiceImpl.contextPath);
		cookie.setAttribute("SameSite", "Strict");
		return cookie;
	}

	@Override
	public String generateOpaqueToken() {
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[32];
		random.nextBytes(bytes);
		String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		return token;
	}

	@Override
	public TokenDatabase insertTokenToBase(Long id, AppUser appUser, Boolean active) {
		LocalDateTime currentTime = LocalDateTime.now();
		TokenDatabase tokenDatabase = new TokenDatabase();
		tokenDatabase.setId(id);
		tokenDatabase.setActive(active);
		tokenDatabase.setAppUser(appUser);
		tokenDatabase.setRefreshToken(generateOpaqueToken());
		tokenDatabase.setRefreshEnd(currentTime.plusMinutes(AppParameters.refreshtokenduration));
		tokenDatabase.setSessionToken(generateOpaqueToken());
		tokenDatabase.setSessionEnd(currentTime.plusMinutes(AppParameters.sessionduration));
		tokenDatabase = this.datatableService.save(tokenDatabase);
		return tokenDatabase;
	}

}
