package rs.irm.administration.utils;

import java.sql.Connection;
import java.sql.Statement;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.entity.ReportGroup;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethod;
import rs.irm.utils.DatabaseListenerJob;

public class ReportGroupDelete implements ExecuteMethod {

	private HttpServletRequest httpServletRequest;
	private Long id;
	private DatatableService datatableService;

	public ReportGroupDelete(HttpServletRequest httpServletRequest, Long id) {
		this.httpServletRequest = httpServletRequest;
		this.id = id;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
	}

	@Override
	public void execute(Connection connection) {
		ReportGroup reportGroup = this.datatableService.findByExistingId(id, ReportGroup.class, connection);
		this.datatableService.delete(reportGroup, connection);

		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("NOTIFY " + DatabaseListenerJob.reportgroup_listener + ", 'Report group changed';");
			statement.executeUpdate(
					"NOTIFY " + DatabaseListenerJob.reportgrouprole_listener + ", 'Report group role changed';");
			statement.close();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

	}

}
