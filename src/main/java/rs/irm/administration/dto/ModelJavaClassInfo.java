package rs.irm.administration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.irm.administration.entity.ModelJavaClass;
import rs.irm.administration.enums.TriggerEvent;
import rs.irm.database.annotations.EntityClass;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityClass(ModelJavaClass.class)
public class ModelJavaClassInfo {
	
	private Long modelId;
	
	private TriggerEvent event;
	
	private String javaClassClassText;
	
	private String javaClassClassName;
	
	private String javaClassMethodName;

}
