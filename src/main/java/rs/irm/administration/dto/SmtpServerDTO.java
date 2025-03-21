package rs.irm.administration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import rs.irm.administration.entity.SmtpServer;
import rs.irm.administration.enums.SmtpSecurity;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.annotations.EnumField;
import rs.irm.database.annotations.TableHide;
import rs.irm.database.enums.InitSort;

@Data
@EntityClass(SmtpServer.class)
public class SmtpServerDTO {

	@NotNull
	@TableHide
	private Long id;
	
	@InitSort
	@NotNull
	private String name;
	
	@NotNull
	private String host;
	
	@NotNull
	@TableHide
	private Integer port;
	
	@NotNull
	@TableHide
	private Boolean authentication;
	
	@EnumField(SmtpSecurity.class)
	@NotNull
	@TableHide
	private String security;
	
	@NotNull
	@Email
	private String fromMail;

	@TableHide
	private String username;

	@TableHide
	private String password;
}
