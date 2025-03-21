package rs.irm.preview.utils;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.util.LinkedHashMap;

import jakarta.servlet.http.HttpServletRequest;
import rs.irm.common.exceptions.CommonException;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.preview.service.ModelQueryCreatorService;
import rs.irm.preview.service.impl.ModelQueryCreatorServiceImpl;

public class LockItem implements ExecuteMethodWithReturn<LinkedHashMap<String, Object>>{

	private HttpServletRequest httpServletRequest;
	private Long modelId;
	private Long id;
	private ModelQueryCreatorService modelQueryCreatorService;

	public LockItem(HttpServletRequest httpServletRequest, Long modelId, Long id) {
		this.httpServletRequest = httpServletRequest;
		this.modelId = modelId;
		this.id = id;
		this.modelQueryCreatorService=new ModelQueryCreatorServiceImpl(this.httpServletRequest);
	}

	@Override
	public LinkedHashMap<String, Object> execute(Connection connection) {
		LinkedHashMap<String, Object> existing=modelQueryCreatorService.findByExistingId(this.id, modelId, connection);
		if((Boolean) existing.get("lock")) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "itemIsLocked", null);
		}
		if (modelQueryCreatorService.checkLockParent(modelId, id, 0L, connection)) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "parentIsLocked", null);
		}
		modelQueryCreatorService.getLock(modelId, id, connection);
		return modelQueryCreatorService.findByExistingId(this.id, modelId, connection);
	}
	
	

}
