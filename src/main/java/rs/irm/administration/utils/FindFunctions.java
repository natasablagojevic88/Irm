package rs.irm.administration.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.WebApplicationException;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.database.utils.ExecuteMethodWithReturn;

public class FindFunctions implements ExecuteMethodWithReturn<List<ComboboxDTO>> {

	private String functionType;

	public FindFunctions(String functionType) {
		this.functionType = functionType;
	}

	@Override
	public List<ComboboxDTO> execute(Connection connection) {
		try {
			List<ComboboxDTO> comboboxDTOs = new ArrayList<>();
			String catalog = connection.getCatalog();
			String schema = connection.getSchema();

			String query = "select \n" 
					+ "routine_name\n" 
					+ "from information_schema.routines\n" 
					+ "where \n"
					+ "routine_catalog='" + catalog + "'\n" 
					+ "and routine_schema ='" + schema + "'\n"
					+ "and data_type = '"+functionType+"'\n" 
					+ "and routine_type ='FUNCTION'\n" 
					+ "order by 1";

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			while (resultSet.next()) {
				comboboxDTOs.add(new ComboboxDTO(resultSet.getObject(1).toString(), resultSet.getObject(1).toString()));
			}

			resultSet.close();
			statement.close();
			return comboboxDTOs;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

}
