package rs.irm.administration.utils;

import java.sql.Connection;
import java.sql.Statement;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.entity.ModelProcedure;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethod;
import rs.irm.utils.DatabaseListenerJob;

public class ModelProcedureDelete implements ExecuteMethod {

	private Long id;
	private HttpServletRequest httpServletRequest;
	private DatatableService datatableService;

	public ModelProcedureDelete(Long id, HttpServletRequest httpServletRequest) {
		this.id = id;
		this.httpServletRequest = httpServletRequest;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
	}

	@Override
	public void execute(Connection connection) {
		ModelProcedure modelProcedure = this.datatableService.findByExistingId(id, ModelProcedure.class, connection);
		this.datatableService.delete(modelProcedure, connection);

		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(
					"NOTIFY " + DatabaseListenerJob.modelprocedure_listener + ", 'Model procedure changed';");
			statement.close();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

}
