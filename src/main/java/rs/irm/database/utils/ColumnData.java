package rs.irm.database.utils;

import lombok.Data;
import rs.irm.database.enums.ColumnType;

@Data
public class ColumnData {

	private String code;
	
	private ColumnType columnType;
	
	private String name;
	
	private Integer numberOfDecimal;
}
