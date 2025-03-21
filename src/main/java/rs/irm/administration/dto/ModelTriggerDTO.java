package rs.irm.administration.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import rs.irm.administration.entity.ModelTrigger;
import rs.irm.administration.enums.TriggerEvent;
import rs.irm.administration.enums.TriggerTime;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.annotations.EnumField;
import rs.irm.database.annotations.TableHide;
import rs.irm.database.enums.InitSort;

@Data
@EntityClass(ModelTrigger.class)
public class ModelTriggerDTO {

	@NotNull
	@TableHide
	private Long id;
	
	@NotNull
	@TableHide
	private Long modelId;
	
	@NotNull
	@InitSort
	private String code;
	
	@NotNull
	private String name;
	
	@EnumField(TriggerTime.class)
	@NotNull
	private String triggerTime;
	
	@EnumField(TriggerEvent.class)
	@NotNull
	private String triggerEvent;
	
	@TableHide
	private String condition;
	
	@NotNull
	private String triggerFunction;
}
