package rs.irm.administration.utils;

import java.sql.Connection;
import java.sql.Statement;

import org.modelmapper.ModelMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.ModelProcedureDTO;
import rs.irm.administration.entity.ModelProcedure;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.utils.DatabaseListenerJob;

public class ModelProcedureUpdate implements ExecuteMethodWithReturn<ModelProcedureDTO> {

	private ModelProcedureDTO modelProcedureDTO;
	private HttpServletRequest httpServletRequest;
	private DatatableService datatableService;

	public ModelProcedureUpdate(ModelProcedureDTO modelProcedureDTO, HttpServletRequest httpServletRequest) {
		this.modelProcedureDTO = modelProcedureDTO;
		this.httpServletRequest = httpServletRequest;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
	}

	@Override
	public ModelProcedureDTO execute(Connection connection) {
		ModelProcedure modelProcedure = modelProcedureDTO.getId() == 0 ? new ModelProcedure()
				: this.datatableService.findByExistingId(modelProcedureDTO.getId(), ModelProcedure.class, connection);

		new ModelMapper().map(modelProcedureDTO, modelProcedure);
		modelProcedure = this.datatableService.save(modelProcedure, connection);

		try {
			Statement statement = connection.createStatement();
			statement
					.executeUpdate("NOTIFY " + DatabaseListenerJob.modelprocedure_listener + ", 'Model procedure changed';");
			statement.close();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

		return new ModelMapper().map(modelProcedure, ModelProcedureDTO.class);
	}

}
