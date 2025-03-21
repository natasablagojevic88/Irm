package rs.irm.database.utils;

import lombok.Data;
import rs.irm.database.enums.BaseType;

@Data
public class CreateColumnData {
	
	private String name;
	
	private Boolean id;
	
	private BaseType baseType;
	
	private Integer length;
	
	private Integer precision;
	
	private Boolean nullable;

}
