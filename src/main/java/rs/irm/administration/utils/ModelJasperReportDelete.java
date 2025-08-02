package rs.irm.administration.utils;

import java.sql.Connection;

import jakarta.servlet.http.HttpServletRequest;
import rs.irm.administration.entity.ModelJasperReport;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethod;

public class ModelJasperReportDelete implements ExecuteMethod {

	private Long id;
	private HttpServletRequest httpServletRequest;
	private DatatableService datatableService;

	public ModelJasperReportDelete(Long id, HttpServletRequest httpServletRequest) {
		this.id = id;
		this.httpServletRequest = httpServletRequest;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
	}

	@Override
	public void execute(Connection connection) {
		ModelJasperReport modelJasperReport=this.datatableService.findByExistingId(id, ModelJasperReport.class,connection);
		this.datatableService.delete(modelJasperReport,connection);
	}

}
