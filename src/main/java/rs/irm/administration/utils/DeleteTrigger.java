package rs.irm.administration.utils;

import java.sql.Connection;
import java.sql.Statement;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.entity.ModelTrigger;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethod;


public class DeleteTrigger implements ExecuteMethod{
	
	private Long id;
	private HttpServletRequest httpServletRequest;
	
	private DatatableService datatableService;

	public DeleteTrigger(Long id, HttpServletRequest httpServletRequest) {
		this.id = id;
		this.httpServletRequest = httpServletRequest;
		this.datatableService=new DatatableServiceImpl(this.httpServletRequest);
	}



	@Override
	public void execute(Connection connection) {
		ModelTrigger modelTrigger=this.datatableService.findByExistingId(id, ModelTrigger.class, connection);
		String triggerName=modelTrigger.getCode();
		String tableName=modelTrigger.getModel().getCode();
		this.datatableService.delete(modelTrigger, connection);
		
		try {
			String query="DROP TRIGGER "+triggerName+" ON "+tableName;
			
			Statement statement=connection.createStatement();
			statement.executeUpdate(query);
			statement.close();
		}catch(Exception e) {
			throw new WebApplicationException(e);
		}
		
	}
	
	

}
