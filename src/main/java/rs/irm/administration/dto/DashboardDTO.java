package rs.irm.administration.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.irm.administration.entity.Dashboard;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.annotations.TableHide;
import rs.irm.database.enums.InitSort;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EntityClass(Dashboard.class)
public class DashboardDTO {
	
	@NotNull
	@TableHide
	private Long id;
	
	@NotNull
	@InitSort
	private String name;
	
	@NotNull
	@Min(1)
	private Integer rownumber;
	
	@NotNull
	@Min(1)
	private Integer columnnumber;

}
