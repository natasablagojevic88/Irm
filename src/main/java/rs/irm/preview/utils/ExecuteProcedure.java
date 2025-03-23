package rs.irm.preview.utils;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.entity.ModelProcedure;
import rs.irm.common.entity.Track;
import rs.irm.common.enums.TrackAction;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.impl.CommonServiceImpl;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethod;
import rs.irm.preview.service.ModelQueryCreatorService;
import rs.irm.preview.service.impl.ModelQueryCreatorServiceImpl;

public class ExecuteProcedure implements ExecuteMethod {

	private Long procedureId;
	private Long modelId;
	private Long id;
	private HttpServletRequest httpServletRequest;

	private DatatableService datatableService;
	private CommonService commonService;
	private ModelQueryCreatorService modelQueryCreatorService;

	public ExecuteProcedure(Long procedureId, Long modelId, Long id, HttpServletRequest httpServletRequest) {
		this.procedureId = procedureId;
		this.modelId = modelId;
		this.id = id;
		this.httpServletRequest = httpServletRequest;
		this.datatableService = new DatatableServiceImpl(httpServletRequest);
		this.commonService = new CommonServiceImpl(this.httpServletRequest);
		this.modelQueryCreatorService=new ModelQueryCreatorServiceImpl(this.httpServletRequest);
	}

	@Override
	public void execute(Connection connection) {

		ModelProcedure modelProcedure = this.datatableService.findByExistingId(this.procedureId, ModelProcedure.class,
				connection);

		if (modelProcedure.getModel().getId().doubleValue() != this.modelId.doubleValue()) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "wrongModel", modelId);
		}
		
		LinkedHashMap<String, Object> existing = this.modelQueryCreatorService.findByExistingId(this.id, this.modelId,
				connection);

		if ((Boolean) existing.get("lock")) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "itemIsLocked", null);
		}
		if (modelQueryCreatorService.checkLockParent(modelId, id, 0L, connection)) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "parentIsLocked", null);
		}

		String procedureQuery="call "+modelProcedure.getSqlProcedure()+"(?)";
		
		try {
			PreparedStatement preparedStatement=connection.prepareStatement(procedureQuery);
			preparedStatement.setObject(1, this.id);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
		
		this.modelQueryCreatorService.insertTrack(modelId, id, TrackAction.EXECUTE, connection);
		
		Track track=new Track();
		track.setAction(TrackAction.EXECUTE);
		track.setAddress(commonService.getIpAddress());
		track.setAppUser(commonService.getAppUser());
		track.setDataid(modelProcedure.getId());
		track.setId(0L);
		track.setTableName("modelprocedure");
		track.setTime(LocalDateTime.now());
		this.datatableService.save(track, connection);
	}

}
