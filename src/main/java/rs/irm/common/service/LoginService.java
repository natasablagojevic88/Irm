package rs.irm.common.service;

import java.util.List;

import jakarta.servlet.http.Cookie;
import rs.irm.administration.entity.AppUser;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.common.dto.LoginDTO;
import rs.irm.common.entity.TokenDatabase;

public interface LoginService {
	
	void login(LoginDTO loginDTO);
	
	List<ComboboxDTO> language();
	
	void logout();

	Cookie createCookie(String name, String value, Integer duration);

	String generateOpaqueToken();

	TokenDatabase insertTokenToBase(Long id, AppUser appUser, Boolean active);

}
