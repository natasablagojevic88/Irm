package rs.irm.common.service;

import rs.irm.common.enums.Language;

public interface TokenService {

	String generateToken(String username, Language language);

	void validToken(String token);
	
	String getUsername(String token);
	
	String getLanguage(String token);
	
}
