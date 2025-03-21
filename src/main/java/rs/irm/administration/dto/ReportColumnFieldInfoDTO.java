package rs.irm.administration.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import rs.irm.administration.enums.SqlMetric;
import rs.irm.database.enums.SortDirection;
import rs.irm.database.utils.LeftTableData;

@Data
public class ReportColumnFieldInfoDTO {

	private String fieldName;
	private String fieldType;
	private String leftJoinPath;
	private SqlMetric sqlMetric;
	private Integer ordernum;
	private SortDirection sortDirection;
	
	private List<LeftTableData> leftTableDatas = new ArrayList<>();
	private List<String> columnList = new ArrayList<>();
	private List<String> tableList = new ArrayList<>();

}
