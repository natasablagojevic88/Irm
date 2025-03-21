package rs.irm.preview.utils;

import java.sql.Connection;
import java.util.LinkedHashMap;

import jakarta.servlet.http.HttpServletRequest;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.preview.service.ModelQueryCreatorService;
import rs.irm.preview.service.impl.ModelQueryCreatorServiceImpl;

public class PreviewTableCreate implements ExecuteMethodWithReturn<TableDataDTO<LinkedHashMap<String, Object>>> {

	private TableParameterDTO tableParameterDTO;
	private Long modelId;
	private Long parentId;
	private HttpServletRequest httpServletRequest;
	private ModelQueryCreatorService modelQueryCreatorService;

	public PreviewTableCreate(TableParameterDTO tableParameterDTO, Long modelId, Long parentId,
			HttpServletRequest httpServletRequest) {
		this.tableParameterDTO = tableParameterDTO;
		this.modelId = modelId;
		this.parentId = parentId;
		this.httpServletRequest = httpServletRequest;
		this.modelQueryCreatorService = new ModelQueryCreatorServiceImpl(this.httpServletRequest);
	}

	@Override
	public TableDataDTO<LinkedHashMap<String, Object>> execute(Connection connection) {

		return modelQueryCreatorService.createTableDataDTO(tableParameterDTO, modelId, parentId, connection);
	}

}
