package rs.irm.common.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.ws.rs.WebApplicationException;
import rs.irm.database.utils.ExecuteMethodWithReturn;

public class CheckNotificationTrigger implements ExecuteMethodWithReturn<Long> {

	private String tableName;
	private String triggerName;

	public CheckNotificationTrigger(String tableName, String triggerName) {
		this.tableName = tableName;
		this.triggerName = triggerName;
	}

	private String query = "select count(*)\n" + "from information_schema.triggers\n" + "where \n"
			+ "trigger_catalog =current_catalog \n" + "and trigger_schema =current_schema \n"
			+ "and event_object_table =?\n" + "and trigger_name =?";

	@Override
	public Long execute(Connection connection) {

		try {
			PreparedStatement statement = connection.prepareStatement(this.query);
			statement.setObject(1, this.tableName);
			statement.setObject(2, this.triggerName);
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			Long count = ((Number) resultSet.getObject(1)).longValue();
			resultSet.close();
			statement.close();
			return count;
		} catch (SQLException e) {
			throw new WebApplicationException(e);
		}

	}

}
