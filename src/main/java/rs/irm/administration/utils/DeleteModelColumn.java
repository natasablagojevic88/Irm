package rs.irm.administration.utils;
import java.sql.Connection;
import java.sql.Statement;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.entity.ModelColumn;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethod;
import rs.irm.utils.DatabaseListenerJob;

public class DeleteModelColumn implements ExecuteMethod{
	
	private HttpServletRequest httpServletRequest;
	private Long id;
	
	private DatatableService datatableService;

	public DeleteModelColumn(HttpServletRequest httpServletRequest, Long id) {
		this.httpServletRequest = httpServletRequest;
		this.id = id;
		this.datatableService=new DatatableServiceImpl(this.httpServletRequest);
	}



	@Override
	public void execute(Connection connection) {
		
		ModelColumn modelColumn=this.datatableService.findByExistingId(id, ModelColumn.class, connection);
		
		this.datatableService.delete(modelColumn, connection);
		
		String query="alter table ";
		query+=modelColumn.getModel().getCode();
		query+=" drop column ";
		query+=modelColumn.getCode();
		
		try {
			Statement statement=connection.createStatement();
			statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
		
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("NOTIFY " + DatabaseListenerJob.modelcolumn_listener + ", 'Model column changed';");
			statement.close();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

}
