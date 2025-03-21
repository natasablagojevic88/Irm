package rs.irm.preview.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.ModelColumnDTO;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.database.utils.ExecuteMethodWithReturn;

public class CheckDisabledCodebook implements ExecuteMethodWithReturn<LinkedHashMap<String, List<ComboboxDTO>>> {

	private LinkedHashMap<String, Object> value;
	private List<ModelColumnDTO> columnDTOs;

	public CheckDisabledCodebook(LinkedHashMap<String, Object> value, List<ModelColumnDTO> columnDTOs) {
		this.value = value;
		this.columnDTOs = columnDTOs;
	}

	@Override
	public LinkedHashMap<String, List<ComboboxDTO>> execute(Connection connection) {
		LinkedHashMap<String, List<ComboboxDTO>> codebook = new LinkedHashMap<>();

		for (ModelColumnDTO modelColumnDTO : columnDTOs) {
			
			String codebookString=modelColumnDTO.getCode();

			if (value.get(modelColumnDTO.getCode()) == null) {
				continue;
			}
			
			if(value.get(modelColumnDTO.getCode()).toString().length() == 0) {
				
			}
			
			String valueId=value.get(modelColumnDTO.getCode()).toString();
			
			String query="select id,code from "+modelColumnDTO.getCodebookModelCode()+" where id="+valueId;
			
			try {
				Statement statement=connection.createStatement();
				ResultSet resultSet=statement.executeQuery(query);
				
				resultSet.next();
				
				List<ComboboxDTO> comboboxDTOs=new ArrayList<>();
				ComboboxDTO comboboxDTO=new ComboboxDTO(resultSet.getObject(1).toString(), resultSet.getObject(2).toString());
				comboboxDTOs.add(comboboxDTO);
				
				codebook.put(codebookString, comboboxDTOs);
				
				resultSet.close();
				statement.close();
			} catch (SQLException e) {
				throw new WebApplicationException(e);
			}
			
			
		}

		return codebook;
	}

}
