package rs.irm.database.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.irm.database.enums.SortDirection;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableSort {

	private String field;
	
	private SortDirection sortDirection;
}
