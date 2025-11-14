package rs.irm.administration.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.irm.administration.entity.ModelJavaClass;
import rs.irm.administration.enums.TriggerEvent;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.annotations.EnumField;
import rs.irm.database.annotations.TableHide;
import rs.irm.database.enums.InitSort;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityClass(ModelJavaClass.class)
public class ModelJavaClassDTO {

	@NotNull
	@TableHide
	private Long id;
	
	@NotNull
	@TableHide
	private Long modelId;
	
	@NotNull
	@TableHide
	private Long javaClassId;
	
	@InitSort
	private String javaClassName;
	
	@EnumField(TriggerEvent.class)
	@NotNull
	private String event;
}
