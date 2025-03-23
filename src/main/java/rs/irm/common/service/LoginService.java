package rs.irm.common.service;

import java.util.List;

import rs.irm.common.dto.ComboboxDTO;
import rs.irm.common.dto.LoginDTO;
import rs.irm.common.dto.PublicKeyDTO;

public interface LoginService {
	
	void login(LoginDTO loginDTO);
	
	List<ComboboxDTO> language();
	
	void logout();
	
	PublicKeyDTO addSession();

}
