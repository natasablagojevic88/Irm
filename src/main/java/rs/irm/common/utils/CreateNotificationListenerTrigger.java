package rs.irm.common.utils;

import java.sql.Connection;
import java.sql.Statement;

import jakarta.ws.rs.WebApplicationException;
import rs.irm.database.utils.ExecuteMethod;

public class CreateNotificationListenerTrigger implements ExecuteMethod{
	String query="create trigger notification_listen after\n"
			+ "insert\n"
			+ "    or\n"
			+ "delete\n"
			+ "    or\n"
			+ "update\n"
			+ "    on\n"
			+ "notification for each row execute function notification_listen()";

	@Override
	public void execute(Connection connection) {
		try {
			Statement statement=connection.createStatement();
			statement.executeUpdate(query);
			statement.close();
		}catch(Exception e) {
			throw new WebApplicationException(e);
		}
		
	}

}
