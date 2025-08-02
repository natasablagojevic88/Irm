package rs.irm.administration.utils;

import java.sql.Connection;
import java.sql.Statement;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.entity.Report;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethod;
import rs.irm.utils.DatabaseListenerJob;

public class ReportDelete implements ExecuteMethod {

	private Long id;
	private HttpServletRequest httpServletRequest;
	private DatatableService datatableService;

	public ReportDelete(Long id, HttpServletRequest httpServletRequest) {
		this.id = id;
		this.httpServletRequest = httpServletRequest;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
	}

	@Override
	public void execute(Connection connection) {
		Report report = this.datatableService.findByExistingId(id, Report.class, connection);
		this.datatableService.delete(report, connection);
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("NOTIFY " + DatabaseListenerJob.report_listener + ", 'Report changed';");
			statement.close();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

}
