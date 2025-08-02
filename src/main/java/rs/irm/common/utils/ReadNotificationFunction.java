package rs.irm.common.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.ws.rs.WebApplicationException;
import rs.irm.database.utils.ExecuteMethodWithReturn;

public class ReadNotificationFunction implements ExecuteMethodWithReturn<String> {

	private String functionName;

	public ReadNotificationFunction(String functionName) {
		this.functionName = functionName;
	}
	

	private String query = "select routine_definition \n" + "from information_schema.routines\n" + "where \n"
			+ "routine_catalog =current_catalog \n" + "and routine_schema =current_schema \n"
			+ "and data_type ='trigger'\n" + "and routine_type ='FUNCTION'\n"
			+ "and routine_name =?";

	@Override
	public String execute(Connection connection) {

		try {
			PreparedStatement preparedStatement=connection.prepareStatement(query);
			preparedStatement.setObject(1, this.functionName);
			ResultSet resultSet = preparedStatement.executeQuery();
			String function = null;
			if (resultSet.next()) {
				function = resultSet.getObject(1).toString();
			}
			resultSet.close();
			preparedStatement.close();
			return function;
		} catch (SQLException e) {
			throw new WebApplicationException(e);
		}

	}

}
