package rs.irm.administration.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import rs.irm.administration.entity.ModelProcedure;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.annotations.TableHide;
import rs.irm.database.enums.InitSort;

@Data
@EntityClass(ModelProcedure.class)
public class ModelProcedureDTO {

	@TableHide
	@NotNull
	public Long id;
	
	@TableHide
	@NotNull
	private Long modelId;
	
	@InitSort
	@NotEmpty
	private String name;
	
	@NotEmpty
	private String sqlProcedure;
}
