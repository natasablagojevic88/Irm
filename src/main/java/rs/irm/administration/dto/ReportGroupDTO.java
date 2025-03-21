package rs.irm.administration.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import rs.irm.administration.entity.ReportGroup;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.annotations.TableHide;
import rs.irm.database.enums.InitSort;

@Data
@EntityClass(ReportGroup.class)
public class ReportGroupDTO {

	@NotNull
	@TableHide
	private Long id;
	
	@NotNull
	@InitSort
	private String code;
	
	@NotNull
	private String name;
}
