package rs.irm.administration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TestSmtpDTO {

	@NotNull
	@Email
	private String toAddress;
}
