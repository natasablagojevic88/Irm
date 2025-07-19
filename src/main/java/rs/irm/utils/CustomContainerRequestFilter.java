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
import rs.irm.administration.dto.RoleForUserDTO;
import rs.irm.administration.entity.AppUser;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.exceptions.FieldRequiredException;
import rs.irm.common.exceptions.MaximumException;
import rs.irm.common.exceptions.MinimumException;
import rs.irm.common.service.TokenService;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.utils.TableFilter;

public class CustomContainerRequestFilter implements ContainerRequestFilter {

	@Context
	private HttpServletRequest httpServletRequest;

	@Inject
	private DatatableService datatableService;

	@Inject
	private ResourceInfo resourceInfo;

	@Inject
	private TokenService tokenService;

	@Override
	public void filter(ContainerRequestContext containerRequestContext) throws IOException {

		checkRequestField(containerRequestContext);

		if (httpServletRequest.getPathInfo().equals("/openapi.json")
				|| httpServletRequest.getPathInfo().startsWith("/login")) {
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

		String token = httpServletRequest.getHeader("Authorization");

		if (token == null) {

			if (httpServletRequest.getCookies() != null) {
				Map<String, String> parameters = new HashMap<>();
				for (Cookie cookie : httpServletRequest.getCookies()) {
					parameters.put(cookie.getName(), cookie.getValue());
				}

				if (parameters.containsKey("session")) {
					token = parameters.get("session");
				}

			}
		} else {
			token = token.substring(7);
		}

		tokenService.validToken(token);

		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		TableFilter tableFilter = new TableFilter();
		tableFilter.setField("username");
		tableFilter.setParameter1(tokenService.getUsername(token));
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableParameterDTO.getTableFilters().add(tableFilter);
		List<AppUser> appUsers = datatableService.findAll(tableParameterDTO, AppUser.class);

		if (appUsers.isEmpty()) {
			throw new CommonException(HttpURLConnection.HTTP_UNAUTHORIZED, "wrongUsername", null);
		}

		AppUser appUser = appUsers.get(0);

		if (!appUser.getActive()) {
			throw new CommonException(HttpURLConnection.HTTP_UNAUTHORIZED, "userIsNotActive", appUser.getUsername());
		}

		httpServletRequest.setAttribute("username", appUser.getUsername());
		httpServletRequest.setAttribute("userid", appUser.getId());
		httpServletRequest.setAttribute("language", tokenService.getLanguage(token));

		tableParameterDTO = new TableParameterDTO();
		tableFilter = new TableFilter();
		tableFilter.setField("appUserId");
		tableFilter.setParameter1(String.valueOf(appUser.getId()));
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableParameterDTO.getTableFilters().add(tableFilter);
		List<RoleForUserDTO> userRoleDTOs = datatableService.findAll(tableParameterDTO, RoleForUserDTO.class);

		List<String> roles = userRoleDTOs.stream().map(a -> a.getRoleCode()).toList();
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

}
