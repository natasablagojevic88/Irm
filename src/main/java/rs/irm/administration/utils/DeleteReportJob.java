package rs.irm.administration.utils;

import java.sql.Connection;

import jakarta.servlet.http.HttpServletRequest;
import rs.irm.administration.entity.ReportJob;
import rs.irm.administration.service.LoadReportJobService;
import rs.irm.administration.service.impl.LoadReportJobServiceImpl;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethod;

public class DeleteReportJob implements ExecuteMethod {

	private HttpServletRequest httpServletRequest;
	private Long id;

	private DatatableService datatableService;
	private LoadReportJobService loadReportJobService;

	public DeleteReportJob(HttpServletRequest httpServletRequest, Long id) {
		this.httpServletRequest = httpServletRequest;
		this.id = id;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
		this.loadReportJobService = new LoadReportJobServiceImpl();
	}

	@Override
	public void execute(Connection connection) {
		ReportJob reportJob = this.datatableService.findByExistingId(id, ReportJob.class, connection);
		this.datatableService.delete(reportJob, connection);
		this.loadReportJobService.removeJob(reportJob);

	}

}
