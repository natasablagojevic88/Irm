package rs.irm.preview.utils;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.ModelJavaClassInfo;
import rs.irm.administration.enums.TriggerEvent;
import rs.irm.administration.utils.ExecuteClass;
import rs.irm.administration.utils.ModelData;
import rs.irm.common.exceptions.CommonException;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.preview.service.ModelQueryCreatorService;
import rs.irm.preview.service.impl.ModelQueryCreatorServiceImpl;

public class UnlockItem implements ExecuteMethodWithReturn<LinkedHashMap<String, Object>> {

	private HttpServletRequest httpServletRequest;
	private Long modelId;
	private Long id;
	private ModelQueryCreatorService modelQueryCreatorService;

	public UnlockItem(HttpServletRequest httpServletRequest, Long modelId, Long id) {
		this.httpServletRequest = httpServletRequest;
		this.modelId = modelId;
		this.id = id;
		this.modelQueryCreatorService = new ModelQueryCreatorServiceImpl(this.httpServletRequest);
	}

	@Override
	public LinkedHashMap<String, Object> execute(Connection connection) {
		LinkedHashMap<String, Object> existing = modelQueryCreatorService.findByExistingId(this.id, modelId,
				connection);
		if (!(Boolean) existing.get("lock")) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "itemIsUnlocked", null);
		}
		if (modelQueryCreatorService.checkLockParent(modelId, id, 0L, connection)) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "parentIsLocked", null);
		}
		modelQueryCreatorService.getUnlock(modelId, id, connection);
		
		List<ModelJavaClassInfo> javaCodes = ModelData.modelJavaClassInfo.stream()
				.filter(a -> a.getModelId().doubleValue() == this.modelId.doubleValue())
				.filter(a -> a.getEvent().equals(TriggerEvent.UNLOCK))
				.toList();
		for (ModelJavaClassInfo javaClassInfo : javaCodes) {
			ExecuteClass executeClass = new ExecuteClass(id, javaClassInfo.getJavaClassClassText(),
					javaClassInfo.getJavaClassClassName(), javaClassInfo.getJavaClassMethodName());
			
			try {
				executeClass.execute();
			} catch (Exception e) {
				throw new WebApplicationException(e);
			}
		}
		return modelQueryCreatorService.findByExistingId(this.id, modelId, connection);
	}

}
