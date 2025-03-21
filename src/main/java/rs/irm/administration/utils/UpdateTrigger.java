package rs.irm.administration.utils;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.Statement;

import org.modelmapper.ModelMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.ModelTriggerDTO;
import rs.irm.administration.entity.Model;
import rs.irm.administration.entity.ModelTrigger;
import rs.irm.administration.enums.ModelType;
import rs.irm.administration.enums.TriggerEvent;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.impl.CommonServiceImpl;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;

public class UpdateTrigger implements ExecuteMethodWithReturn<ModelTriggerDTO> {

	private ModelTriggerDTO modelTriggerDTO;
	private HttpServletRequest httpServletRequest;

	private DatatableService datatableService;
	private ModelMapper modelMapper = new ModelMapper();
	private CommonService commonService;

	public UpdateTrigger(ModelTriggerDTO modelTriggerDTO, HttpServletRequest httpServletRequest) {
		this.modelTriggerDTO = modelTriggerDTO;
		this.httpServletRequest = httpServletRequest;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
		this.commonService = new CommonServiceImpl(this.httpServletRequest);
	}


	@Override
	public ModelTriggerDTO execute(Connection connection) {
		ModelTrigger modelTrigger = modelTriggerDTO.getId() == 0 ? new ModelTrigger()
				: this.datatableService.findByExistingId(modelTriggerDTO.getId(), ModelTrigger.class, connection);
		modelTriggerDTO.setCode(modelTriggerDTO.getCode().toLowerCase());

		if (modelTriggerDTO.getTriggerEvent().equals(TriggerEvent.LOCK.name())
				|| modelTriggerDTO.getTriggerEvent().equals(TriggerEvent.UNLOCK.name())) {
			modelTriggerDTO.setCondition(null);
		}

		Model model = this.datatableService.findByExistingId(modelTriggerDTO.getModelId(), Model.class, connection);
		if (!model.getType().equals(ModelType.TABLE)) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "modelNotTable", model.getCode());
		}

		modelMapper.map(modelTriggerDTO, modelTrigger);
		modelTrigger = this.datatableService.save(modelTrigger, connection);

		try {
			createOrUpdateTrigger(modelTrigger, connection);
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

		return modelMapper.map(modelTrigger, ModelTriggerDTO.class);
	}

	private void createOrUpdateTrigger(ModelTrigger modelTrigger, Connection connection) throws Exception {

		StringWriter stringWriter = new StringWriter();
		BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
		bufferedWriter.write("CREATE OR REPLACE TRIGGER " + modelTrigger.getCode());
		bufferedWriter.newLine();
		bufferedWriter.write(modelTrigger.getTriggerTime().name());
		bufferedWriter.write(" ");
		String action = (modelTrigger.getTriggerEvent().equals(TriggerEvent.LOCK)
				|| modelTrigger.getTriggerEvent().equals(TriggerEvent.UNLOCK)) ? TriggerEvent.UPDATE.name()
						: modelTrigger.getTriggerEvent().equals(TriggerEvent.INSERTORUPDATE) ? "INSERT OR UPDATE"
								: modelTrigger.getTriggerEvent().name();
		bufferedWriter.write(action);
		bufferedWriter.newLine();
		if (modelTrigger.getTriggerEvent().equals(TriggerEvent.LOCK)
				|| modelTrigger.getTriggerEvent().equals(TriggerEvent.UNLOCK)) {
			bufferedWriter.write("OF LOCK");
			bufferedWriter.newLine();
		}
		bufferedWriter.write("ON " + modelTrigger.getModel().getCode());
		bufferedWriter.newLine();
		bufferedWriter.write("FOR EACH ROW");
		bufferedWriter.newLine();

		if (modelTrigger.getTriggerEvent().equals(TriggerEvent.LOCK)
				|| modelTrigger.getTriggerEvent().equals(TriggerEvent.UNLOCK)) {
			bufferedWriter.write("WHEN ( new.lock = "
					+ (modelTrigger.getTriggerEvent().equals(TriggerEvent.LOCK) ? "true" : "false") + " )");
			bufferedWriter.newLine();
		} else {
			if (commonService.hasText(modelTrigger.getCondition())) {
				bufferedWriter.write("WHEN ( " + modelTrigger.getCondition() + " )");
				bufferedWriter.newLine();
			}
		}

		bufferedWriter.write("EXECUTE FUNCTION " + modelTrigger.getTriggerFunction() + "()");

		bufferedWriter.close();

		Statement statement = connection.createStatement();
		statement.executeUpdate(stringWriter.toString());
		statement.close();

	}

}
