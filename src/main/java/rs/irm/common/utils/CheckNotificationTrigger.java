package rs.irm.common.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import jakarta.ws.rs.WebApplicationException;
import rs.irm.database.utils.ExecuteMethodWithReturn;

public class CheckNotificationTrigger implements ExecuteMethodWithReturn<Long> {
	
	private String query="select count(*)\n"
			+ "from information_schema.triggers\n"
			+ "where \n"
			+ "trigger_catalog =current_catalog \n"
			+ "and trigger_schema =current_schema \n"
			+ "and event_object_table ='notification'\n"
			+ "and trigger_name ='notification_listen'";

	@Override
	public Long execute(Connection connection) {
		
		try {
			Statement statement=connection.createStatement();
			ResultSet resultSet=statement.executeQuery(query);
			resultSet.next();
			Long count=((Number) resultSet.getObject(1)).longValue();
			resultSet.close();
			statement.close();
			return count;
		} catch (SQLException e) {
			throw new WebApplicationException(e);
		}
		
		
	}

}
