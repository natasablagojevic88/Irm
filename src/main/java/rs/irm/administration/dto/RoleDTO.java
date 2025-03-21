package rs.irm.administration.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import rs.irm.administration.entity.Role;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.annotations.TableHide;
import rs.irm.database.enums.InitSort;

@Data
@EntityClass(Role.class)
public class RoleDTO {

	@NotNull
	@TableHide
	private Long id;

	@NotNull
	@InitSort
	private String code;

	private String description;

}
