package rs.irm.common.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import jakarta.ws.rs.WebApplicationException;
import rs.irm.database.utils.ExecuteMethod;

public class CreateNotificatonFunction implements ExecuteMethod {

	@Override
	public void execute(Connection connection) {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
				this.getClass().getClassLoader().getResourceAsStream("notification_listener.sql")));
		
		String row;
		String functionString="";
		try {
			while((row=bufferedReader.readLine())!=null) {
				functionString+=row+"\n";
			}
			
			bufferedReader.close();
		} catch (IOException e) {
			throw new WebApplicationException(e);
		}
		
		try {
			Statement statement=connection.createStatement();
			statement.executeUpdate(functionString);
			statement.close();
		} catch (SQLException e) {
			throw new WebApplicationException(e);
		}

	}

}
