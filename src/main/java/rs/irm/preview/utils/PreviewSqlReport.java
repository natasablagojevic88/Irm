package rs.irm.preview.utils;

import java.sql.Connection;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import rs.irm.administration.dto.SqlQueryParametersDTO;
import rs.irm.administration.dto.SqlQuerySortDTO;
import rs.irm.administration.dto.SqlResultColumnDTO;
import rs.irm.administration.dto.SqlResultDTO;
import rs.irm.administration.entity.Report;
import rs.irm.administration.entity.ReportFilter;
import rs.irm.administration.enums.SqlMetric;
import rs.irm.administration.utils.SqlQueryExecute;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.ColumnType;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.database.utils.TableFilter;
import rs.irm.database.utils.TableSort;
import rs.irm.preview.dto.TableReportParameterDTO;

public class PreviewSqlReport implements ExecuteMethodWithReturn<SqlResultDTO> {

	private HttpServletRequest httpServletRequest;
	private TableReportParameterDTO tableReportParameterDTO;
	private Long reportId;

	private DatatableService datatableService;

	public PreviewSqlReport(HttpServletRequest httpServletRequest, TableReportParameterDTO tableReportParameterDTO,
			Long reportId) {
		this.httpServletRequest = httpServletRequest;
		this.tableReportParameterDTO = tableReportParameterDTO;
		this.reportId = reportId;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
	}

	@Override
	public SqlResultDTO execute(Connection connection) {
		Report report = this.datatableService.findByExistingId(reportId, Report.class, connection);
		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("report", SearchOperation.equals, String.valueOf(reportId), null));
		List<ReportFilter> filters = this.datatableService.findAll(tableParameterDTO, ReportFilter.class, connection);

		String query = report.getSqlQuery();
		query = new PreviewGraphReport().addParametersToQuery(query, tableReportParameterDTO, filters);

		SqlQueryParametersDTO sqlQueryParametersDTO = new SqlQueryParametersDTO();
		sqlQueryParametersDTO.setPageNumber(this.tableReportParameterDTO.getPageNumber());
		sqlQueryParametersDTO.setPageSize(this.tableReportParameterDTO.getPageSize());
		sqlQueryParametersDTO.setSorts(this.tableReportParameterDTO.getSorts().stream().map(a -> mapSort(a)).toList());
		sqlQueryParametersDTO.setSqlQuery(query);
		sqlQueryParametersDTO.setTotals(this.tableReportParameterDTO.getTotals());

		SqlQueryExecute sqlQueryExecute = new SqlQueryExecute(sqlQueryParametersDTO);

		SqlResultDTO sqlResultDTO = this.datatableService.executeMethodWithReturn(sqlQueryExecute, connection);

		for (SqlResultColumnDTO sqlResultColumnDTO : sqlResultDTO.getColumns()) {
			if (!sqlResultColumnDTO.getType().equals(ColumnType.BigDecimal)) {
				continue;
			}

			sqlResultColumnDTO.setSqlMetric(SqlMetric.SUM);
		}

		return sqlResultDTO;
	}

	private SqlQuerySortDTO mapSort(TableSort tableSort) {

		SqlQuerySortDTO sqlQuerySortDTO = new SqlQuerySortDTO();
		sqlQuerySortDTO.setOrderNumber(Integer.valueOf(tableSort.getField()));
		sqlQuerySortDTO.setSortDirection(tableSort.getSortDirection());
		return sqlQuerySortDTO;
	}

}
