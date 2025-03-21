package rs.irm.database.utils;

import java.sql.Connection;

@FunctionalInterface
public interface ExecuteMethodWithReturn<C> {

	C execute(Connection connection);
}
