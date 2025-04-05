package rs.irm.common.service.impl;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;

import io.jsonwebtoken.lang.Arrays;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import rs.irm.administration.entity.AppUser;
import rs.irm.common.dto.Base64DownloadFileDTO;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.ResourceBundleService;

@Named
public class CommonServiceImpl implements CommonService {

	@Context
	private HttpServletRequest httpServletRequest;

	public CommonServiceImpl() {
	}

	public CommonServiceImpl(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}



	@Override
	public String getUsername() {
		if (httpServletRequest != null) {
			return httpServletRequest.getAttribute("username").toString();
		}
		return null;
	}

	@Override
	public AppUser getAppUser() {
		if (httpServletRequest != null) {
			Long userId = (Long) httpServletRequest.getAttribute("userid");

			AppUser appUser = new AppUser();
			appUser.setId(userId);
			return appUser;
		}
		return null;
	}

	@Override
	public String getIpAddress() {
		if (httpServletRequest != null) {
			if(hasText(httpServletRequest.getHeader("X-Real-IP"))) {
				httpServletRequest.getHeader("X-Real-IP");
			}else {
				return httpServletRequest.getRemoteAddr();
			}
			
		}
		return null;
	}

	@Override
	public List<String> getRoles() {
		if (httpServletRequest != null) {
			@SuppressWarnings("unchecked")
			List<String> roles = (List<String>) httpServletRequest.getAttribute("roles");

			return roles;
		}
		return null;
	}

	@Override
	public List<ComboboxDTO> enumToCombobox(Class<? extends Enum<?>> inEnumClass) {
		List<ComboboxDTO> list = new ArrayList<>();
		ResourceBundleService resourceBundleService = new ResourceBundleServiceImpl(httpServletRequest);
		for (@SuppressWarnings("rawtypes")
		Enum enumValue : Arrays.asList(inEnumClass.getEnumConstants())) {
			list.add(new ComboboxDTO(enumValue.name(),
					resourceBundleService.getText(inEnumClass.getSimpleName() + "." + enumValue.name(), null)));
		}
		return list;
	}

	@Override
	public LinkedHashMap<String, String> classToNames(Class<?> inClass) {
		ResourceBundleService resourceBundleService = new ResourceBundleServiceImpl(httpServletRequest);
		LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
		linkedHashMap.put(inClass.getSimpleName(), resourceBundleService.getText(inClass.getSimpleName()+".title", null));
		for (Field field : Arrays.asList(inClass.getDeclaredFields())) {
			field.setAccessible(true);
			linkedHashMap.put(field.getName(),
					resourceBundleService.getText(inClass.getSimpleName() + "." + field.getName(), null));
		}
		return linkedHashMap;
	}

	@Override
	public Boolean hasText(String string) {
		
		if(string == null) {
			return false;
		}
		
		if(string.length()==0) {
			return false;
		}
		
		return true;
		
	}

	@Override
	public Base64DownloadFileDTO responseToBase64(Response response) {
		Base64DownloadFileDTO base64DownloadFileDTO = new Base64DownloadFileDTO();
		base64DownloadFileDTO.setFilename(response.getHeaderString("filename"));
		base64DownloadFileDTO.setType(response.getHeaderString("Content-Type"));

		String base64String = Base64.getEncoder().encodeToString((byte[]) response.getEntity());
		base64DownloadFileDTO.setBase64String(base64DownloadFileDTO.getType() + ";base64," + base64String);
		return base64DownloadFileDTO;
	}

	@Override
	public void checkDefaultParameter(String query) {
		query=query.replace("{parent}", ":parent");
		
		net.sf.jsqlparser.statement.Statement statement;
		try {
			statement = CCJSqlParserUtil.parse(query);
		} catch (JSQLParserException e) {
			throw new WebApplicationException(e);
		}

		if (!(statement instanceof Select)) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "unallowedQuery", null);
		}
		
	}

}
