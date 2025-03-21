package rs.irm.administration.utils;

import java.sql.Connection;

import rs.irm.administration.dto.ReportDTO;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethod;
import rs.irm.preview.dto.TableReportParameterDTO;
import rs.irm.preview.utils.CreateDashboardData;
import rs.irm.preview.utils.PreviewExecuteReport;

public class ExecuteExecuteReportJob implements ExecuteMethod {

	private ReportDTO reportDTO;
	private DatatableService datatableService;

	public ExecuteExecuteReportJob(ReportDTO reportDTO) {
		this.reportDTO = reportDTO;
		this.datatableService = new DatatableServiceImpl();
	}

	@Override
	public void execute(Connection connection) {
		TableReportParameterDTO tableReportParameterDTO = new TableReportParameterDTO();
		tableReportParameterDTO
				.setParameters(new CreateDashboardData(null, null).createParameter(reportDTO.getId(), connection));

		PreviewExecuteReport previewExecuteReport = new PreviewExecuteReport(null, reportDTO.getId(),
				tableReportParameterDTO);
		datatableService.executeMethodWithReturn(previewExecuteReport, connection);

	}

}
