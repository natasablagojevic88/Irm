package rs.irm.database.utils;

import java.sql.Connection;

@FunctionalInterface
public interface ExecuteMethod {

	void execute(Connection connection);
}
