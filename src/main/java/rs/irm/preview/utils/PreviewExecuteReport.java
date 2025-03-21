package rs.irm.preview.utils;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.SqlExecuteResultDTO;
import rs.irm.administration.entity.Report;
import rs.irm.administration.entity.ReportFilter;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.common.service.impl.ResourceBundleServiceImpl;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.database.utils.TableFilter;
import rs.irm.preview.dto.TableReportParameterDTO;

public class PreviewExecuteReport implements ExecuteMethodWithReturn<SqlExecuteResultDTO>{

	private HttpServletRequest httpServletRequest;
	private Long reportId;
	private TableReportParameterDTO tableReportParameterDTO;
	private ResourceBundleService resourceBundleService;
	private DatatableService datatableService;
	public PreviewExecuteReport(HttpServletRequest httpServletRequest, Long reportId,
			TableReportParameterDTO tableReportParameterDTO) {
		this.httpServletRequest = httpServletRequest;
		this.reportId = reportId;
		this.tableReportParameterDTO = tableReportParameterDTO;
		this.resourceBundleService=new ResourceBundleServiceImpl(this.httpServletRequest);
		this.datatableService=new DatatableServiceImpl(this.httpServletRequest);
	}

	@Override
	public SqlExecuteResultDTO execute(Connection connection) {
		SqlExecuteResultDTO sqlExecuteResultDTO=new SqlExecuteResultDTO();
		Report report = this.datatableService.findByExistingId(reportId, Report.class, connection);
		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("report", SearchOperation.equals, String.valueOf(reportId), null));
		List<ReportFilter> filters = this.datatableService.findAll(tableParameterDTO, ReportFilter.class, connection);

		String query = report.getSqlQuery();
		query = new PreviewGraphReport().addParametersToQuery(query, tableReportParameterDTO, filters);
		
		Integer numberOfChanges=null;
		
		try {
			Statement statement=connection.createStatement();
			numberOfChanges=statement.executeUpdate(query);
			statement.close();
		}catch(Exception e) {
			throw new WebApplicationException(e);
		}
		sqlExecuteResultDTO.setMessage(resourceBundleService.getText("updatedRowsNumber", new Object[] {numberOfChanges}));
		return sqlExecuteResultDTO;
	}
	
}
