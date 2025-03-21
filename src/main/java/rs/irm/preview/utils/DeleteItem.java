package rs.irm.preview.utils;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.util.LinkedHashMap;

import jakarta.servlet.http.HttpServletRequest;
import rs.irm.common.exceptions.CommonException;
import rs.irm.database.utils.ExecuteMethod;
import rs.irm.preview.service.ModelQueryCreatorService;
import rs.irm.preview.service.impl.ModelQueryCreatorServiceImpl;

public class DeleteItem implements ExecuteMethod {

	private HttpServletRequest httpServletRequest;
	private Long modelId;
	private Long id;

	private ModelQueryCreatorService modelQueryCreatorService;

	public DeleteItem(HttpServletRequest httpServletRequest, Long modelId, Long id) {
		this.httpServletRequest = httpServletRequest;
		this.modelId = modelId;
		this.id = id;
		this.modelQueryCreatorService = new ModelQueryCreatorServiceImpl(this.httpServletRequest);
	}

	@Override
	public void execute(Connection connection) {

		LinkedHashMap<String, Object> existing = this.modelQueryCreatorService.findByExistingId(this.id, this.modelId,
				connection);

		if ((Boolean) existing.get("lock")) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "itemIsLocked", null);
		}
		if (modelQueryCreatorService.checkLockParent(modelId, id, 0L, connection)) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "parentIsLocked", null);
		}

		modelQueryCreatorService.getDelete(this.modelId, this.id, connection);
		

	}

}
