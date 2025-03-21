package rs.irm.database.utils;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import rs.irm.administration.enums.SqlMetric;
import rs.irm.database.enums.SortDirection;

@Setter
@Getter
public class FieldInfo {
	
	String fieldName;
	String fieldType;
	String alias;
	String columnName;
	Integer ordernum;
	SortDirection sortDirection;
	SqlMetric sqlMetric;
	List<LeftTableData> leftTableDatas = new ArrayList<>();
	List<String> columnList = new ArrayList<>();
	String leftJoinPath;

}
