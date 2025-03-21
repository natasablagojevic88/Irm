package rs.irm.administration.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import rs.irm.administration.entity.Model;
import rs.irm.administration.enums.ModelType;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.annotations.EnumField;
import rs.irm.database.annotations.SkipField;
import rs.irm.database.annotations.TableHide;
import rs.irm.database.enums.InitSort;

@Data
@EntityClass(Model.class)
public class ModelDTO {

	@NotNull
	@TableHide
	private Long id;
	
	@NotNull
	@InitSort
	private String code;
	
	@NotNull
	private String name;
	
	@NotNull
	@EnumField(ModelType.class)
	@TableHide
	private String type;
	
	@NotNull
	@TableHide
	private Boolean numericCode;
	
	@NotNull
	@TableHide
	private String icon;
	
	@TableHide
	private Long parentId;
	
	@TableHide
	private String parentCode;
	
	@TableHide
	private String parentType;
	
	@NotNull
	@TableHide
	private Long previewRoleId;
	
	@TableHide
	private String previewRoleCode;
	
	@NotNull
	@TableHide
	private Long updateRoleId;
	
	@TableHide
	private String updateRoleCode;
	
	@NotNull
	@TableHide
	private Long lockRoleId;
	
	@TableHide
	private String lockRoleCode;
	
	@NotNull
	@TableHide
	private Long unlockRoleId;
	
	@TableHide
	private String unlockRoleCode;
	
	@TableHide
	@Min(1)
	private Integer dialogWidth;

	@TableHide
	@Min(1)
	private Integer columnsNumber;
	
	@TableHide
	@Min(0)
	private Integer tableWidth;
	
	@SkipField
	private List<ModelDTO> children=new ArrayList<>();
	
}
