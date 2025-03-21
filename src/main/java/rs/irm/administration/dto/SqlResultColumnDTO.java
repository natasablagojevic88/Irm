package rs.irm.administration.dto;

import lombok.Data;
import rs.irm.administration.enums.SqlMetric;
import rs.irm.database.enums.ColumnType;

@Data
public class SqlResultColumnDTO {
	
	private Integer orderNumber;
	
	private String name;
	
	private ColumnType type;
	
	private SqlMetric sqlMetric;

}
