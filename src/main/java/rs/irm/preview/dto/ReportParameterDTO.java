package rs.irm.preview.dto;

import lombok.Data;
import rs.irm.administration.enums.SqlMetric;
import rs.irm.database.enums.SearchOperation;

@Data
public class ReportParameterDTO {

	private Long id;
	
	private String code;
	
	private String modelColumnName;
	
	private String name;
	
	private String customname;
	
	private SearchOperation searchOperation;
	
	private SqlMetric sqlMetric;
	
	private String defaultValue1;
	
	private String defaultValue2;
	
	private String fieldType;
	
}
