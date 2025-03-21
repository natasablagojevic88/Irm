package rs.irm.database.utils;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LeftTable {
	String alias;
	String table;
	String idColumn;
	String fieldAlias;
	String fieldColumn;
	String path;
}