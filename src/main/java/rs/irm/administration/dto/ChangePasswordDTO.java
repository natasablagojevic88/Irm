package rs.irm.administration.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangePasswordDTO {
	
	@NotNull
	private String password;

}
