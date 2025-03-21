package rs.irm.preview.utils;

import java.sql.Connection;
import java.util.LinkedHashMap;

import jakarta.servlet.http.HttpServletRequest;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.preview.service.ModelQueryCreatorService;
import rs.irm.preview.service.impl.ModelQueryCreatorServiceImpl;

public class PreviewItem implements ExecuteMethodWithReturn<LinkedHashMap<String, Object>>{

	private HttpServletRequest httpServletRequest;
	private Long modelId;
	private Long id;
	private ModelQueryCreatorService modelQueryCreatorService; 
	
	public PreviewItem(HttpServletRequest httpServletRequest, Long modelId, Long id) {
		this.httpServletRequest = httpServletRequest;
		this.modelId = modelId;
		this.id = id;
		this.modelQueryCreatorService=new ModelQueryCreatorServiceImpl(this.httpServletRequest);
	}

	@Override
	public LinkedHashMap<String, Object> execute(Connection connection) {
		return modelQueryCreatorService.findByExistingId(id, modelId, connection);
	}
	
	
}
