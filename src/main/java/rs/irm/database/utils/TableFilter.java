package rs.irm.database.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.irm.database.enums.SearchOperation;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableFilter {

	private String field;
	
	private SearchOperation searchOperation;
	
	private String parameter1;
	
	private String parameter2;
}
