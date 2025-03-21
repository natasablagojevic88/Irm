package rs.irm.preview.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import jakarta.ws.rs.WebApplicationException;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.database.utils.ExecuteMethodWithReturn;

public class ChangeEventExecute implements ExecuteMethodWithReturn<LinkedHashMap<String, Object>> {

	private LinkedHashMap<String, Object> value;
	private String executeFuntion;

	public ChangeEventExecute(LinkedHashMap<String, Object> value, String executeFuntion) {
		this.value = value;
		this.executeFuntion = executeFuntion;
	}

	@SuppressWarnings("unchecked")
	@Override
	public LinkedHashMap<String, Object> execute(Connection connection) {

		JSONObject jsonObject = new JSONObject();
		Iterator<String> itr = value.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();

			if (value.get(key) != null) {
				jsonObject.put(key, value.get(key).toString());
			} else {
				jsonObject.put(key, null);
			}

		}

		String query = "select " + executeFuntion + "(?::json)";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			preparedStatement.setObject(1, jsonObject.toJSONString());
			ResultSet resultSet = preparedStatement.executeQuery();

			resultSet.next();

			value = new LinkedHashMap<>();

			JSONObject resultFromBase = (JSONObject) new JSONParser().parse(resultSet.getObject(1).toString());

			LinkedHashMap<String, Object> result = new LinkedHashMap<>();
			if (resultFromBase.size() == 2 && resultFromBase.get("value") != null
					&& resultFromBase.get("codebook") != null) {
				jsonObject = (JSONObject) resultFromBase.get("value");
				itr = jsonObject.keySet().iterator();

				while (itr.hasNext()) {
					String key = itr.next();

					if (jsonObject.get(key) != null) {
						value.put(key, jsonObject.get(key).toString());
					} else {
						value.put(key, null);
					}
				}
				
				JSONObject codebookObject=(JSONObject)resultFromBase.get("codebook");
				
				LinkedHashMap<String, List<ComboboxDTO>> codebook=new LinkedHashMap<>();
				
				Iterator<String> iteratorCodebook=codebookObject.keySet().iterator();
				
				while(iteratorCodebook.hasNext()) {
					String codebookCode=iteratorCodebook.next();
					
					JSONArray listCodebook=(JSONArray) codebookObject.get(codebookCode);
					List<ComboboxDTO> listComboboxDTOs=new ArrayList<>();
					for(int i=0;i<listCodebook.size();i++) {
						JSONObject item=(JSONObject)listCodebook.get(i);
						ComboboxDTO comboboxDTO=new ComboboxDTO(item.get("value").toString(), item.get("option").toString());
						listComboboxDTOs.add(comboboxDTO);
					}
					codebook.put(codebookCode, listComboboxDTOs);
				
				}
				
				result.put("codebook", codebook);
				
			} else {
				jsonObject = resultFromBase;
				itr = jsonObject.keySet().iterator();

				while (itr.hasNext()) {
					String key = itr.next();

					if (jsonObject.get(key) != null) {
						value.put(key, jsonObject.get(key).toString());
					} else {
						value.put(key, null);
					}
				}
			}

			resultSet.close();
			preparedStatement.close();

			result.put("value", value);

			return result;

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

	}

}
