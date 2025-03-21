package rs.irm.administration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import rs.irm.administration.entity.AppUser;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.annotations.TableHide;
import rs.irm.database.enums.InitSort;

@Data
@EntityClass(AppUser.class)
public class AppUserDTO {

	@NotNull
	@TableHide
	private Long id;
	
	@InitSort
	@NotNull
	private String username;

	@TableHide
	private String password;
	
	@NotNull
	private String name;

	@NotNull
	private Boolean active;

	@Email
	private String email;
}
