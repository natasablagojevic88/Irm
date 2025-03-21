package rs.irm.preview.utils;

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.preview.service.ModelQueryCreatorService;
import rs.irm.preview.service.impl.ModelQueryCreatorServiceImpl;

public class CreateListCombobox implements ExecuteMethodWithReturn<LinkedHashMap<String, List<ComboboxDTO>>> {

	private Long modelId;
	private HttpServletRequest request;

	private ModelQueryCreatorService modelQueryCreatorService;

	public CreateListCombobox(Long modelId, HttpServletRequest request) {
		this.modelId = modelId;
		this.request = request;
		this.modelQueryCreatorService = new ModelQueryCreatorServiceImpl(this.request);
	}
	
	@Override
	public LinkedHashMap<String, List<ComboboxDTO>> execute(Connection connection) {
		LinkedHashMap<String, List<ComboboxDTO>> map = this.modelQueryCreatorService.getCodebooks(this.modelId,
				connection);

		LinkedHashMap<String, List<ComboboxDTO>> listOfValue = this.modelQueryCreatorService.createListOfValue(modelId,
				connection);
		map.putAll(listOfValue);

		return map;
	}

}
