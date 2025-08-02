package rs.irm.common.utils;

import java.sql.Connection;
import java.sql.Statement;

import jakarta.ws.rs.WebApplicationException;
import rs.irm.database.utils.ExecuteMethod;

public class CreateNotificationListenerTrigger implements ExecuteMethod{
	private String query;
	private String tableName;
	private String triggerName;

	public CreateNotificationListenerTrigger(String tableName, String triggerName) {
		this.tableName = tableName;
		this.triggerName = triggerName;
		this.query="create trigger "+this.triggerName+" after\n"
				+ "insert\n"
				+ "    or\n"
				+ "delete\n"
				+ "    or\n"
				+ "update\n"
				+ "    on\n"
				+ ""+this.tableName+" for each row execute function "+this.triggerName+"()";
	}



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
