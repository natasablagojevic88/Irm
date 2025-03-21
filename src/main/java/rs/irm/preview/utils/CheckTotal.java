package rs.irm.preview.utils;

import java.sql.Connection;
import java.util.LinkedHashMap;

import jakarta.servlet.http.HttpServletRequest;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.preview.service.ModelQueryCreatorService;
import rs.irm.preview.service.impl.ModelQueryCreatorServiceImpl;

public class CheckTotal implements ExecuteMethodWithReturn<TableDataDTO<LinkedHashMap<String, Object>>> {

	private HttpServletRequest httpServletRequest;
	private Long modelId;
	private TableParameterDTO tableParameterDTO;
	
	private ModelQueryCreatorService modelQueryCreatorService;

	public CheckTotal(HttpServletRequest httpServletRequest, Long modelId, TableParameterDTO tableParameterDTO) {
		this.httpServletRequest = httpServletRequest;
		this.modelId = modelId;
		this.tableParameterDTO = tableParameterDTO;
		this.modelQueryCreatorService=new ModelQueryCreatorServiceImpl(this.httpServletRequest);
	}

	@Override
	public TableDataDTO<LinkedHashMap<String, Object>> execute(Connection connection) {
		TableDataDTO<LinkedHashMap<String, Object>> tableDataDTO=new TableDataDTO<>();
		this.modelQueryCreatorService.checkTotal(tableDataDTO, tableParameterDTO, modelId, connection);
		return tableDataDTO;
	}

}
