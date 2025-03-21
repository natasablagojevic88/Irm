package rs.irm.administration.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SqlQueryParametersDTO {
	
	private Integer pageNumber=0;
	private Integer pageSize=Integer.MAX_VALUE;
	
	@NotNull
	private String sqlQuery;
	
	private List<Integer> totals=new ArrayList<>();
	
	private List<SqlQuerySortDTO> sorts=new ArrayList<>();

}
