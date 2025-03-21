package rs.irm.database.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import rs.irm.database.utils.TableFilter;
import rs.irm.database.utils.TableSort;

@Data
public class TableParameterDTO {
	
	private Integer pageNumber=0;
	private Integer pageSize=Integer.MAX_VALUE;

	private List<TableFilter> tableFilters=new ArrayList<>();
	
	private List<TableFilter> havingFilters=new ArrayList<>();
	
	private List<TableSort> tableSorts=new ArrayList<>();
}
