package rs.irm.administration.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.WebApplicationException;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.database.utils.ExecuteMethodWithReturn;

public class FindProcedures implements ExecuteMethodWithReturn<List<ComboboxDTO>> {

	@Override
	public List<ComboboxDTO> execute(Connection connection) {
		List<ComboboxDTO> list = new ArrayList<>();

		try {

			String catalog = connection.getCatalog();
			String schema = connection.getSchema();

			String query = "select \n" + "routine_name \n" + "from information_schema.routines\n" + "where \n"
					+ "routine_catalog = '" + catalog + "' \n" + "and routine_schema = '" + schema + "' \n"
					+ "and routine_type ='PROCEDURE'\n" + "order by \n" + "1";

			Statement st = connection.createStatement();
			ResultSet resultSet = st.executeQuery(query);

			while (resultSet.next()) {
				ComboboxDTO comboboxDTO = new ComboboxDTO(resultSet.getObject(1).toString(),
						resultSet.getObject(1).toString());
				list.add(comboboxDTO);
			}

			st.close();
			resultSet.close();

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
		return list;
	}

}
