package rs.irm.utils;

import java.sql.Connection;
import java.util.List;

public class AppConnections {

	public static List<Connection> allConnections;
	public static List<Connection> freeConnections;
	public static Connection checkConnection;
	public static Connection checkNotification;
}
