package rs.irm.administration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.irm.administration.entity.DashboardRole;
import rs.irm.database.annotations.EntityClass;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityClass(DashboardRole.class)
public class DashboardRoleInfoDTO {

	private Long dashboardId;
	
	private String roleCode;
}
