package rs.irm.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import rs.irm.administration.entity.AppUser;
import rs.irm.administration.entity.AppUserRole;
import rs.irm.administration.utils.ModelData;
import rs.irm.common.entity.TokenDatabase;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.exceptions.FieldRequiredException;
import rs.irm.common.exceptions.MaximumException;
import rs.irm.common.exceptions.MinimumException;
import rs.irm.common.service.LoginService;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.utils.TableFilter;

public class CustomContainerRequestFilter implements ContainerRequestFilter {

	@Context
	private HttpServletRequest httpServletRequest;

	@Context
	private HttpServletResponse httpServletResponse;

	@Inject
	private ResourceInfo resourceInfo;

	@Inject
	private LoginService loginService;

	@Override
	public void filter(ContainerRequestContext containerRequestContext) throws IOException {

		checkRequestField(containerRequestContext);

		if (httpServletRequest.getPathInfo().equals("/openapi.json")
				|| httpServletRequest.getPathInfo().equals("/login")) {
			return;
		}

		checkToken();

	}

	private void checkRequestField(ContainerRequestContext containerRequestContext) {
		Method method = resourceInfo.getResourceMethod();

		Parameter parameter = null;
		for (Parameter parameterIn : method.getParameters()) {

			if (parameterIn.isAnnotationPresent(Valid.class)) {
				parameter = parameterIn;
				break;
			}
		}

		if (parameter != null) {
			try {
				Class<?> parametersType = Class.forName(parameter.getType().getCanonicalName());

				InputStream inputStream = containerRequestContext.getEntityStream();
				JSONObject jsonBody = (JSONObject) new JSONParser()
						.parse(new InputStreamReader(inputStream, Charset.forName("UTF-8")));

				for (Field field : Arrays.asList(parametersType.getDeclaredFields())) {
					field.setAccessible(true);

					if (field.isAnnotationPresent(NotNull.class) || field.isAnnotationPresent(NotEmpty.class)
							|| field.isAnnotationPresent(NotBlank.class)) {
						String fieldName = field.getName();

						if (jsonBody.get(fieldName) == null) {
							throw new FieldRequiredException(parametersType.getSimpleName() + "." + fieldName);
						}

						if (field.getType().getSimpleName().equals("String")) {
							if (jsonBody.get(fieldName).toString().length() == 0) {
								throw new FieldRequiredException(parametersType.getSimpleName() + "." + fieldName);
							}
						}
					}

					if (field.isAnnotationPresent(Min.class)) {
						Min min = field.getAnnotation(Min.class);
						String fieldName = field.getName();
						if (jsonBody.get(fieldName) != null) {
							NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
							numberFormat.setMinimumFractionDigits(10);

							Number number = numberFormat.parse(jsonBody.get(field.getName()).toString());

							if (number.longValue() < min.value()) {
								throw new MinimumException(parametersType.getSimpleName() + "." + fieldName,
										min.value());
							}
						}
					}

					if (field.isAnnotationPresent(Max.class)) {
						Max max = field.getAnnotation(Max.class);
						String fieldName = field.getName();
						if (jsonBody.get(fieldName) != null) {
							NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
							numberFormat.setMinimumFractionDigits(10);

							Number number = numberFormat.parse(jsonBody.get(field.getName()).toString());

							if (number.longValue() > max.value()) {
								throw new MaximumException(parametersType.getSimpleName() + "." + fieldName,
										max.value());
							}
						}
					}

					if (field.isAnnotationPresent(Email.class)) {
						String fieldName = field.getName();
						if (jsonBody.get(fieldName) != null) {
							if (jsonBody.get(fieldName).toString().length() > 0) {
								int splitPosition = jsonBody.get(fieldName).toString().lastIndexOf('@');

								if (splitPosition < 0) {
									throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "wrongEmailFormat",
											jsonBody.get(fieldName).toString());
								}
								String localPart = jsonBody.get(fieldName).toString().substring(0, splitPosition);

								if (localPart.length() == 0) {
									throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "wrongEmailFormat",
											jsonBody.get(fieldName).toString());
								}

								String domainPart = jsonBody.get(fieldName).toString().substring(splitPosition + 1);

								if (domainPart.length() == 0) {
									throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "wrongEmailFormat",
											jsonBody.get(fieldName).toString());
								}
							}
						}
					}
				}

				containerRequestContext.setEntityStream(
						new ByteArrayInputStream(jsonBody.toJSONString().getBytes(Charset.forName("UTF-8"))));
			} catch (Exception e) {
				throw new WebApplicationException(e);
			}
		}
	}

	private void checkToken() {
		LocalDateTime now = LocalDateTime.now();

		if (httpServletRequest.getCookies() == null) {
			removeCookies();
			throw new CommonException(HttpURLConnection.HTTP_UNAUTHORIZED, "noCookie", null);
		}

		Map<String, String> parameters = new HashMap<>();
		for (Cookie cookie : httpServletRequest.getCookies()) {
			parameters.put(cookie.getName(), cookie.getValue());
		}

		String session = parameters.get("session");
		String refresh_token = parameters.get("refresh_token");
		String lang = parameters.get("lang");

		if (session == null || refresh_token == null) {
			removeCookies();
			throw new CommonException(HttpURLConnection.HTTP_UNAUTHORIZED, "noCookie", null);
		}

		TokenDatabase tokenDatabaseDTO;
	
		try {
			tokenDatabaseDTO=checkToken(session, refresh_token);
		}catch(Exception e) {
			removeCookies();
			throw new WebApplicationException(e);
		}

		if (now.isAfter(tokenDatabaseDTO.getSessionEnd())) {
			TokenDatabase tokenDatabase = this.loginService.insertTokenToBase(tokenDatabaseDTO.getId(),
					tokenDatabaseDTO.getAppUser(), true);
			 
			session=tokenDatabaseDTO.getSessionToken();
			httpServletResponse.addCookie(loginService.createCookie("session", tokenDatabase.getSessionToken(),
					AppParameters.refreshtokenduration.intValue() * 60));
			httpServletResponse.addCookie(loginService.createCookie("refresh_token", tokenDatabase.getRefreshToken(),
					AppParameters.refreshtokenduration.intValue() * 60));
			
			TokenDatabase tokenDatableCurrent=ModelData.datatableTokens.stream().filter(a->a.getId().doubleValue()==tokenDatabase.getId().doubleValue())
					.findFirst().get();
			int index=ModelData.datatableTokens.indexOf(tokenDatableCurrent);
			if(index>-1) {
				ModelData.datatableTokens.set(index, tokenDatabase);
			}
		}
		
		AppUser appUser=ModelData.appUsers.stream().filter(a->a.getId().doubleValue()==tokenDatabaseDTO.getAppUser().getId().doubleValue())
				.findFirst().get();

		httpServletRequest.setAttribute("username", appUser.getUsername());
		httpServletRequest.setAttribute("userid", appUser.getId());
		httpServletRequest.setAttribute("language", lang);
		httpServletRequest.setAttribute("session", session);

		List<AppUserRole> userRoles = ModelData.appUserRoles.stream().filter(a->a.getAppUser().getId().doubleValue()==appUser.getId().doubleValue())
				.toList();

		List<String> roles = userRoles.stream().map(a -> findRoleCode(a.getRole().getId()))
				.filter(a->a!=null)
				.toList();
		httpServletRequest.setAttribute("roles", roles);
		Method method = resourceInfo.getResourceMethod();

		if (method.isAnnotationPresent(RolesAllowed.class)) {
			RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);

			boolean hasRight = false;

			for (String roleAllow : Arrays.asList(rolesAllowed.value())) {
				if (roles.contains(roleAllow)) {
					hasRight = true;
					break;
				}
			}

			if (!hasRight) {
				throw new CommonException(HttpURLConnection.HTTP_FORBIDDEN, "NoRight", null);
			}
		}
	}
	
	private String findRoleCode(Long roleId) {
		return ModelData.roles.stream().filter(a->a.getId().doubleValue()==roleId.doubleValue())
				.map(a->a.getCode()).findFirst().orElse(null);
	}
	
	public TokenDatabase checkToken(String session,String refresh_token) throws Exception{

		LocalDateTime now=LocalDateTime.now();
		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters().add(new TableFilter("sessionToken", SearchOperation.equals, session, null));
		List<TokenDatabase> tokenDatabaseDTOs = ModelData.datatableTokens.stream().filter(a->a.getSessionToken().equals(session))
				.toList();

		if (tokenDatabaseDTOs.isEmpty()) {
			throw new CommonException(HttpURLConnection.HTTP_UNAUTHORIZED, "wrongToken", null);
		}
		
		TokenDatabase tokenDatabaseDTO = tokenDatabaseDTOs.get(0);
		
		List<AppUser> appUsers=ModelData.appUsers.stream().filter(a->a.getId().doubleValue()==tokenDatabaseDTO.getAppUser().getId().doubleValue())
				.toList();
		
		if(appUsers.isEmpty()) {
			throw new CommonException(HttpURLConnection.HTTP_UNAUTHORIZED, "userIsNotActive",
					null);
		}
		
		AppUser appUser=appUsers.get(0);

		if (!appUser.getActive()) {
			throw new CommonException(HttpURLConnection.HTTP_UNAUTHORIZED, "userIsNotActive",
					appUser.getUsername());
		}
		
		if (!tokenDatabaseDTO.getRefreshToken().equals(refresh_token)) {
			throw new CommonException(HttpURLConnection.HTTP_UNAUTHORIZED, "wrongRefreshToken",
					appUser.getUsername());
		}

		if (!tokenDatabaseDTO.getActive()) {
			throw new CommonException(HttpURLConnection.HTTP_UNAUTHORIZED, "wrongToken", null);
		}

		if (now.isAfter(tokenDatabaseDTO.getSessionEnd()) && now.isAfter(tokenDatabaseDTO.getRefreshEnd())) {
			throw new CommonException(HttpURLConnection.HTTP_UNAUTHORIZED, "tokenExpired", null);
		}
		
		return tokenDatabaseDTO;
	}

	private void removeCookies() {
		this.httpServletResponse.addCookie(loginService.createCookie("session", null, 0));
		this.httpServletResponse.addCookie(loginService.createCookie("refresh_token", null, 0));
		this.httpServletResponse.addCookie(loginService.createCookie("lang", null, 0));
	}

}
