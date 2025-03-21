package rs.irm.preview.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import lombok.Data;
import rs.irm.database.utils.TableSort;

@Data
public class TableReportParameterDTO {
	
	private Integer pageNumber=0;
	private Integer pageSize=Integer.MAX_VALUE;
	
	
	private LinkedHashMap<Integer, String[]> parameters=new LinkedHashMap<>();
	
	private List<TableSort> sorts=new ArrayList<>();
	private List<Integer> totals=new ArrayList<>();

}
