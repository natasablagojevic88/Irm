package rs.irm.administration.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.irm.database.enums.InitSort;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportGroupRoleDTO {

	@NotNull
	private Boolean hasRight=false;
	
	@NotNull
	private Long id;
	
	@InitSort
	private String code;
	
	private String description;
}
