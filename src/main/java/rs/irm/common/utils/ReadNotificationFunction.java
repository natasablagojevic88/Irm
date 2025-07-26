package rs.irm.common.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import jakarta.ws.rs.WebApplicationException;
import rs.irm.database.utils.ExecuteMethodWithReturn;

public class ReadNotificationFunction implements ExecuteMethodWithReturn<String> {
	
	private String query="select routine_definition \n"
			+ "from information_schema.routines\n"
			+ "where \n"
			+ "routine_catalog =current_catalog \n"
			+ "and routine_schema =current_schema \n"
			+ "and data_type ='trigger'\n"
			+ "and routine_type ='FUNCTION'\n"
			+ "and routine_name ='notification_listen'";

	@Override
	public String execute(Connection connection) {
		
		try {
			Statement statement=connection.createStatement();
			ResultSet resultSet=statement.executeQuery(query);
			String function=null;
			if(resultSet.next()) {
				function=resultSet.getObject(1).toString();
			}
			resultSet.close();
			statement.close();
			return function;
		} catch (SQLException e) {
			throw new WebApplicationException(e);
		}
		
		
	}

}
