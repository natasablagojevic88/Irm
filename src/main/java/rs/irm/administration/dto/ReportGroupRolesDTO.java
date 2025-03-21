package rs.irm.administration.dto;

import lombok.Data;
import rs.irm.administration.entity.ReportGroupRole;
import rs.irm.database.annotations.EntityClass;

@Data
@EntityClass(ReportGroupRole.class)
public class ReportGroupRolesDTO {
	
	private Long reportGroupId;
	
	private String reportGroupName;
	
	private String roleCode;

}
