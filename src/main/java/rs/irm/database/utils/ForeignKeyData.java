package rs.irm.database.utils;

import lombok.Data;

@Data
public class ForeignKeyData {

	private String name;
	private String column;
	private String table;
	private Boolean cascade;
	
}
