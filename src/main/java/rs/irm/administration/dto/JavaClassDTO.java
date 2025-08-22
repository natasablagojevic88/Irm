package rs.irm.administration.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import rs.irm.administration.entity.JavaClass;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.annotations.TableHide;
import rs.irm.database.enums.InitSort;

@Data
@EntityClass(JavaClass.class)
public class JavaClassDTO {
	
	@NotNull
	@TableHide
	private Long id;
	
	@NotEmpty
	@InitSort
	private String name;
	
	@NotEmpty
	@TableHide
	private String classText;
	
	@NotEmpty
	private String className;
	
	@NotEmpty
	private String methodName;

}
