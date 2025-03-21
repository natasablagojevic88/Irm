package rs.irm.administration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleAppUserDTO {

	private Boolean hasRight=false;
	
	private Long id;
	
	private String username;
	
	private String name;
}
