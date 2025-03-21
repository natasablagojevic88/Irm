package rs.irm.administration.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import rs.irm.administration.enums.SqlMetric;
import rs.irm.database.enums.ColumnType;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.enums.SortDirection;

@Data
public class ReportColumnInfoDTO {
	
	private String code;
	
	private String name;
	
	private String customName;

	private Long modelColumnId;
	
	private ColumnType columnType;
	
	private String icon;
	
	private SqlMetric sqlMetric;
	
	private Integer ordernum;
	private SortDirection sortDirection;
	
	private SearchOperation searchOperation;
	
	private String defaultValue1;
	private String defaultValue2;

	private ReportColumnFieldInfoDTO columnFieldInfoDTO = new ReportColumnFieldInfoDTO();
	
	private List<ReportColumnInfoDTO> children=new ArrayList<>();

}
