package rs.irm.administration.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.irm.administration.entity.DashboardItem;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.enums.InitSort;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityClass(DashboardItem.class)
public class DashboardItemDTO {

	private Long id;
	
	@NotNull
	private Long dashboardId;
	
	@Min(1)
	@NotNull
	@InitSort
	private Integer row;
	
	@Min(1)
	@NotNull
	@InitSort(2)
	private Integer column;
	
	@Min(1)
	@NotNull
	private Integer colspan;
	
	@Min(1)
	@NotNull
	private Integer rowspan;
	
	private Long reportId;
	
	private String reportName;
	
	private String reportType;

}
