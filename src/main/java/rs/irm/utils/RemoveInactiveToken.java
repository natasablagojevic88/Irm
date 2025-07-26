package rs.irm.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.ws.rs.WebApplicationException;
import rs.irm.database.utils.ExecuteMethod;

public class RemoveInactiveToken implements ExecuteMethod {

	@Override
	public void execute(Connection connection) {
		try {
			PreparedStatement preparedStatement=connection.prepareStatement("delete from \n"
					+ "tokendatabase t \n"
					+ "where \n"
					+ "t.active =false \n"
					+ "or t.refreshend <  now()");
			
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			throw new WebApplicationException(e);
		}

	}

}
