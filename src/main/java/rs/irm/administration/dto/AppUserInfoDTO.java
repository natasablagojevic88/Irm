package rs.irm.administration.dto;

import java.util.LinkedHashMap;

import lombok.Data;

@Data
public class AppUserInfoDTO {

	private String username;
	
	private String name;
	
	private String email;
	
	private LinkedHashMap<String, String> names=new LinkedHashMap<>();
}
