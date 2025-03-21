package rs.irm.administration.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.irm.administration.entity.Report;
import rs.irm.administration.enums.GraphType;
import rs.irm.administration.enums.ReportType;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.annotations.EnumField;
import rs.irm.database.annotations.SkipField;
import rs.irm.database.annotations.TableHide;
import rs.irm.database.enums.InitSort;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityClass(Report.class)
public class ReportDTO {

	@NotNull
	@TableHide
	private Long id;
	
	@NotNull
	@TableHide
	private Long reportGroupId;
	
	@NotNull
	@InitSort
	private String code;
	
	@NotNull
	private String name;
	
	@NotNull
	@EnumField(ReportType.class)
	private String type;
	
	
	@TableHide
	private Long modelId;
	
	@TableHide
	private String modelCode;
	
	@EnumField(GraphType.class)
	@TableHide
	private String graphType;
	
	@TableHide
	private String sqlQuery;
	
	@SkipField
	@TableHide
	private List<ReportColumnInfoDTO> columns=new ArrayList<>();
	
	@SkipField
	@TableHide
	private List<ReportColumnInfoDTO> filters=new ArrayList<>();
	
	@SkipField
	@TableHide
	private String fileName;
	
	@SkipField
	@TableHide
	private String filePath;
	
}
