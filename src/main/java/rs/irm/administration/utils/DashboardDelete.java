package rs.irm.administration.utils;

import java.sql.Connection;
import java.sql.Statement;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.entity.Dashboard;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethod;
import rs.irm.utils.DatabaseListenerJob;

public class DashboardDelete implements ExecuteMethod {
	
	private Long id;
	private HttpServletRequest httpServletRequest;
	private DatatableService datatableService;

	public DashboardDelete(Long id, HttpServletRequest httpServletRequest) {
		this.id = id;
		this.httpServletRequest = httpServletRequest;
		this.datatableService=new DatatableServiceImpl(this.httpServletRequest);
	}


	@Override
	public void execute(Connection connection) {
		Dashboard dashboard = this.datatableService.findByExistingId(id, Dashboard.class,connection);
		this.datatableService.delete(dashboard,connection);
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("NOTIFY " + DatabaseListenerJob.dashboard_listener + ", 'Dashboard changed';");
			statement.close();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

	}

}
