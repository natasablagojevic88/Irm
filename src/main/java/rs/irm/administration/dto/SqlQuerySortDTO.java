package rs.irm.administration.dto;

import lombok.Data;
import rs.irm.database.enums.SortDirection;

@Data
public class SqlQuerySortDTO {

	private Integer orderNumber;
	
	private SortDirection sortDirection;
}
