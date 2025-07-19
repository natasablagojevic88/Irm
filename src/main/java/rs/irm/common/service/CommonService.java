package rs.irm.common.service;

import java.util.LinkedHashMap;
import java.util.List;

import jakarta.ws.rs.core.Response;
import rs.irm.administration.entity.AppUser;
import rs.irm.common.dto.Base64DownloadFileDTO;
import rs.irm.common.dto.ComboboxDTO;

public interface CommonService {
	
	String getUsername();
	
	AppUser getAppUser();
	
	String getIpAddress();
	
	String getSession();
	
	List<String> getRoles();
	
	List<ComboboxDTO> enumToCombobox(Class<? extends Enum<?>> inEnumClass);
	
	LinkedHashMap<String, String> classToNames(Class<?> inClass);
	
	Boolean hasText(String string);
	
	Base64DownloadFileDTO responseToBase64(Response response);

	void checkDefaultParameter(String query);
}
