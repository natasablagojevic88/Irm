package rs.irm.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import rs.irm.common.enums.Language;

@Data
public class LoginDTO {

	@NotNull
	private String username;
	
	private String password;
	
	private String encyptPassword;
	
	@NotNull
	private Language language;
}
