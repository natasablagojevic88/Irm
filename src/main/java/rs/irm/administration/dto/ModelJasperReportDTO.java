package rs.irm.administration.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import rs.irm.administration.entity.ModelJasperReport;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.annotations.SkipField;
import rs.irm.database.annotations.TableHide;
import rs.irm.database.enums.InitSort;

@Data
@EntityClass(ModelJasperReport.class)
public class ModelJasperReportDTO {
	
	@TableHide
	@NotNull
	private Long id;
	
	@TableHide
	@NotNull
	private Long modelId;
	
	@NotNull
	@InitSort
	private String name;
	
	@NotNull
	private String jasperFileName;
	
	@SkipField
	private String filePath;

}
