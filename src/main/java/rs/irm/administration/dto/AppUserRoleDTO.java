package rs.irm.administration.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppUserRoleDTO {

	@NotNull
	private Boolean hasRight=false;
	
	@NotNull
	private Long id;
	
	@Column(nullable = false)
	private String code;
	
	@Column
	private String description;
	
}
