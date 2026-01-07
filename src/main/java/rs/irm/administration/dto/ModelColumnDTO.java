package rs.irm.administration.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.irm.administration.entity.ModelColumn;
import rs.irm.administration.enums.ModelColumnType;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.annotations.EnumField;
import rs.irm.database.annotations.TableHide;
import rs.irm.database.enums.InitSort;

@Data
@EntityClass(ModelColumn.class)
@NoArgsConstructor
@AllArgsConstructor
public class ModelColumnDTO {
	
	@TableHide
	@NotNull
	private Long id;

	@TableHide
	@NotNull
	private Long modelId;
	
	
	@NotNull
	private String code;
	
	@NotNull
	private String name;
	
	@EnumField(ModelColumnType.class)
	@NotNull
	private String columnType;
	
	@TableHide
	private Long codebookModelId;
	
	@TableHide
	private String codebookModelCode;
	
	@TableHide
	private Boolean codebookModelNumericCode;
	
	@TableHide
	@Min(1)
	private Integer length;
	
	@TableHide
	@Min(0)
	private Integer precision;
	
	@NotNull
	private Boolean nullable;
	
	@NotNull
	@Min(1)
	@InitSort
	private Integer rowNumber;
	
	@NotNull
	@Min(1)
	@InitSort(2)
	private Integer columnNumber;
	
	@NotNull
	@Min(1)
	@TableHide
	private Integer colspan;
	
	@TableHide
	private Long parentId;
	
	@TableHide
	private String parentCode;
	
	@TableHide
	private String defaultValue;
	
	@TableHide
	private String listOfValues;
	
	@NotNull
	@TableHide
	private Boolean showInTable;
	
	@NotNull
	@TableHide
	private Boolean disabled;
	
	@NotNull
	@TableHide
	private Boolean description;
	
	@TableHide
	private String eventFunction;
}
