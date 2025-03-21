package rs.irm.preview.utils;

import java.sql.Connection;
import java.util.LinkedHashMap;

import jakarta.servlet.http.HttpServletRequest;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.preview.service.ModelQueryCreatorService;
import rs.irm.preview.service.impl.ModelQueryCreatorServiceImpl;

public class CreateDefaultValue implements ExecuteMethodWithReturn<LinkedHashMap<String, Object>>{
	
	private HttpServletRequest httpServletRequest;
	private Long modelId;
	private Long parentId;
	private ModelQueryCreatorService modelQueryCreatorService;
	public CreateDefaultValue(HttpServletRequest httpServletRequest, Long modelId, Long parentId) {
		this.httpServletRequest = httpServletRequest;
		this.modelId = modelId;
		this.parentId=parentId;
		this.modelQueryCreatorService=new ModelQueryCreatorServiceImpl(this.httpServletRequest);
	}

	@Override
	public LinkedHashMap<String, Object> execute(Connection connection) {
		
		return modelQueryCreatorService.getDefaultValues(modelId,this.parentId, connection);
	}
	
	

}
