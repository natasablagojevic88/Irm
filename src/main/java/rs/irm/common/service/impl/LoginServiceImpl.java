package rs.irm.common.service.impl;

import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;

import org.springframework.security.crypto.bcrypt.BCrypt;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import rs.irm.administration.entity.AppUser;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.common.dto.LoginDTO;
import rs.irm.common.dto.PublicKeyDTO;
import rs.irm.common.enums.Language;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.exceptions.FieldRequiredException;
import rs.irm.common.service.CommonService;
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

	@Inject
	private CommonService commonService;

	@SuppressWarnings("static-access")
	@Override
	public void login(LoginDTO loginDTO) {

		if (!(commonService.hasText(loginDTO.getPassword()) || commonService.hasText(loginDTO.getEncyptPassword()))) {
			throw new FieldRequiredException("LoginDTO.password");
		}

		if (commonService.hasText(loginDTO.getEncyptPassword())) {
			HttpSession httpSession = this.servletRequest.getSession(false);
			PrivateKey privateKey = (PrivateKey) httpSession.getAttribute("privateKey");

			try {
				Cipher cipher = Cipher.getInstance("RSA");
				cipher.init(Cipher.DECRYPT_MODE, privateKey);
				byte[] endcryByte=Base64.getDecoder().decode(loginDTO.getEncyptPassword());
				loginDTO.setPassword(new String(cipher.doFinal(endcryByte),
						Charset.forName("UTF-8")));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

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
		if(servletRequest.getCookies()!=null) {
			for(Cookie cookie:servletRequest.getCookies()) {
				if(!cookie.getName().equals("JSESSIONID")) {
					continue;
				}
				
				cookie.setSecure(true);
				cookie.setPath(AppParameters.baseurl);
				
				cookie.setMaxAge(durationInSeconds.intValue());
				servletResponse.addCookie(cookie);
			}
		}
		
		String token = tokenService.generateToken(loginDTO.getUsername(), loginDTO.getLanguage());
		Cookie cookie = new Cookie("session", token);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setMaxAge(durationInSeconds.intValue());
		cookie.setPath(AppParameters.production ? AppParameters.baseurl : "/");
		servletResponse.addCookie(cookie);

		cookie = new Cookie("lang", loginDTO.getLanguage().name());
		cookie.setHttpOnly(false);
		cookie.setPath(AppParameters.production ? AppParameters.baseurl : "/");
		servletResponse.addCookie(cookie);
		
		if(AppParameters.production) {
			servletRequest.getSession(false).setAttribute("username", loginDTO.getUsername());
			servletRequest.getSession(false).setAttribute("locale", loginDTO.getLanguage().locale);
			servletRequest.getSession(false).removeAttribute("privateKey");
			servletRequest.getSession(false).removeAttribute("publicKey");
		}
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

		cookie = new Cookie("JSSESIONID", "");
		cookie.setHttpOnly(true);
		cookie.setMaxAge(0);
		cookie.setPath(AppParameters.production ? AppParameters.baseurl : "/");
		servletResponse.addCookie(cookie);

	}

	@Override
	public PublicKeyDTO addSession() {
		HttpSession httpSession = servletRequest.getSession();

		KeyPairGenerator keyPairGenerator;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			throw new WebApplicationException(e);
		}
		keyPairGenerator.initialize(2048);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		httpSession.setAttribute("publicKey", keyPair.getPublic());
		httpSession.setAttribute("privateKey", keyPair.getPrivate());

		return new PublicKeyDTO(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
	}
}
