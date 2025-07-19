package rs.irm.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import rs.irm.common.enums.Language;

@Data
public class LoginDTO {

	@NotNull
	private String username;
	@NotNull
	private String password;
	
	@NotNull
	private Language language;
}
