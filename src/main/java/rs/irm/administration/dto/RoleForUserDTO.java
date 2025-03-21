package rs.irm.administration.dto;

import lombok.Data;
import rs.irm.administration.entity.AppUserRole;
import rs.irm.database.annotations.EntityClass;

@Data
@EntityClass(AppUserRole.class)
public class RoleForUserDTO {

	private Long appUserId;
	
	private String roleCode;
}
