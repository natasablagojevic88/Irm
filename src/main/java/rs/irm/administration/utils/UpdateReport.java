package rs.irm.administration.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import rs.irm.administration.dto.ReportColumnInfoDTO;
import rs.irm.administration.dto.ReportDTO;
import rs.irm.administration.entity.ModelColumn;
import rs.irm.administration.entity.Report;
import rs.irm.administration.entity.ReportColumn;
import rs.irm.administration.entity.ReportFilter;
import rs.irm.administration.entity.ReportJasper;
import rs.irm.administration.enums.GraphType;
import rs.irm.administration.enums.ReportType;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.exceptions.FieldRequiredException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.impl.CommonServiceImpl;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.database.utils.LeftTableData;
import rs.irm.database.utils.TableFilter;
import rs.irm.utils.AppParameters;

public class UpdateReport implements ExecuteMethodWithReturn<ReportDTO> {

	private HttpServletRequest httpServletRequest;
	private ReportDTO reportDTO;

	private DatatableService datatableService;
	private ModelMapper modelMapper = new ModelMapper();
	private CommonService commonService;

	public UpdateReport(HttpServletRequest httpServletRequest, ReportDTO reportDTO) {
		this.httpServletRequest = httpServletRequest;
		this.reportDTO = reportDTO;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
		this.commonService = new CommonServiceImpl(this.httpServletRequest);
	}

	@Override
	public ReportDTO execute(Connection connection) {

		Report report = reportDTO.getId() != 0
				? this.datatableService.findByExistingId(reportDTO.getId(), Report.class, connection)
				: new Report();

		modelMapper.map(reportDTO, report);

		if (report.getType().equals(ReportType.STANDARD)) {
			standardReport(report, reportDTO, connection);
		} else if (report.getType().equals(ReportType.GRAPH)) {
			graphReport(report, reportDTO, connection);
		} else if (report.getType().equals(ReportType.SQL) || report.getType().equals(ReportType.KPI)) {
			sqlReport(report, reportDTO, connection);
		} else if (report.getType().equals(ReportType.JASPER)) {
			jasperReport(report, reportDTO, connection);
		}else if (report.getType().equals(ReportType.EXECUTE)) {
			executeReport(report, reportDTO, connection);
		}

		ConvertReportToReportDTO convertReportToReportDTO = new ConvertReportToReportDTO(httpServletRequest, report);
		ModelData.listReportDTOs = this.datatableService.findAll(new TableParameterDTO(), ReportDTO.class, connection);

		return convertReportToReportDTO.execute(connection);
	}

	private void standardReport(Report report, ReportDTO reportDTO, Connection connection) {

		if (reportDTO.getModelId() == null) {
			throw new FieldRequiredException("ReportDTO.modelId");
		}

		if (reportDTO.getColumns().size() == 0) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "mustChooseOneColumn", null);
		}

		report = this.datatableService.save(report, connection);

		String query = "delete from reportcolumn where report=" + report.getId();
		try {
			Statement st = connection.createStatement();
			st.executeUpdate(query);
			st.close();
		} catch (SQLException e) {
			throw new WebApplicationException(e);
		}

		query = "delete from reportfilter where report=" + report.getId();
		try {
			Statement st = connection.createStatement();
			st.executeUpdate(query);
			st.close();
		} catch (SQLException e) {
			throw new WebApplicationException(e);
		}

		for (ReportColumnInfoDTO reportColumnInfoDTO : reportDTO.getColumns()) {
			ReportColumn reportColumn = new ReportColumn();
			reportColumn.setReport(new Report(report.getId()));
			reportColumn.setId(0L);
			reportColumn.setCode(reportColumnInfoDTO.getCode());
			reportColumn.setCustomName(reportColumnInfoDTO.getCustomName());
			reportColumn.setModelColumn(new ModelColumn(reportColumnInfoDTO.getModelColumnId()));
			reportColumn.setSqlMetric(reportColumnInfoDTO.getSqlMetric());
			reportColumn.setOrdernum(reportColumnInfoDTO.getOrdernum());
			reportColumn.setSortDirection(reportColumnInfoDTO.getSortDirection());
			reportColumn.setFieldName(reportColumnInfoDTO.getColumnFieldInfoDTO().getFieldName());
			reportColumn.setFieldType(reportColumnInfoDTO.getColumnFieldInfoDTO().getFieldType());
			reportColumn.setLeftJoinPath(reportColumnInfoDTO.getColumnFieldInfoDTO().getLeftJoinPath());
			ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
			if (!reportColumnInfoDTO.getColumnFieldInfoDTO().getLeftTableDatas().isEmpty()) {
				List<LeftTableData> leftTableDatas = reportColumnInfoDTO.getColumnFieldInfoDTO().getLeftTableDatas();

				try {
					reportColumn.setLeftTableDatas(objectWriter.writeValueAsString(leftTableDatas));
				} catch (JsonProcessingException e) {
					throw new WebApplicationException(e);
				}
			}

			List<String> columnList = reportColumnInfoDTO.getColumnFieldInfoDTO().getColumnList();
			if (!columnList.isEmpty()) {
				try {
					reportColumn.setColumnList(objectWriter.writeValueAsString(columnList));
				} catch (JsonProcessingException e) {
					throw new WebApplicationException(e);
				}
			}

			List<String> tableList = reportColumnInfoDTO.getColumnFieldInfoDTO().getTableList();
			if (!tableList.isEmpty()) {
				try {
					reportColumn.setTableList(objectWriter.writeValueAsString(tableList));
				} catch (JsonProcessingException e) {
					throw new WebApplicationException(e);
				}
			}

			this.datatableService.save(reportColumn, connection);

		}

		for (ReportColumnInfoDTO reportColumnInfoDTO : reportDTO.getFilters()) {
			ReportFilter reportFilter = new ReportFilter();
			reportFilter.setReport(new Report(report.getId()));
			reportFilter.setId(0L);
			reportFilter.setCode(reportColumnInfoDTO.getCode());
			reportFilter.setCustomName(reportColumnInfoDTO.getCustomName());
			reportFilter.setModelColumn(new ModelColumn(reportColumnInfoDTO.getModelColumnId()));
			if (reportColumnInfoDTO.getSearchOperation() == null) {
				throw new FieldRequiredException("ReportColumnInfoDTO.searchOperation");
			}
			reportFilter.setSearchOperation(reportColumnInfoDTO.getSearchOperation());
			reportFilter.setDefaultValue1(reportColumnInfoDTO.getDefaultValue1());
			reportFilter.setDefaultValue2(reportColumnInfoDTO.getDefaultValue2());
			reportFilter.setFieldName(reportColumnInfoDTO.getColumnFieldInfoDTO().getFieldName());
			reportFilter.setFieldType(reportColumnInfoDTO.getColumnFieldInfoDTO().getFieldType());
			reportFilter.setLeftJoinPath(reportColumnInfoDTO.getColumnFieldInfoDTO().getLeftJoinPath());
			reportFilter.setSqlMetric(reportColumnInfoDTO.getSqlMetric());
			
			if(commonService.hasText(reportFilter.getDefaultValue1())) {
				commonService.checkDefaultParameter(reportFilter.getDefaultValue1());
			}
			
			if(commonService.hasText(reportFilter.getDefaultValue2())) {
				commonService.checkDefaultParameter(reportFilter.getDefaultValue2());
			}

			ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
			if (!reportColumnInfoDTO.getColumnFieldInfoDTO().getLeftTableDatas().isEmpty()) {
				List<LeftTableData> leftTableDatas = reportColumnInfoDTO.getColumnFieldInfoDTO().getLeftTableDatas();

				try {
					reportFilter.setLeftTableDatas(objectWriter.writeValueAsString(leftTableDatas));
				} catch (JsonProcessingException e) {
					throw new WebApplicationException(e);
				}
			}

			List<String> columnList = reportColumnInfoDTO.getColumnFieldInfoDTO().getColumnList();
			if (!columnList.isEmpty()) {
				try {
					reportFilter.setColumnList(objectWriter.writeValueAsString(columnList));
				} catch (JsonProcessingException e) {
					throw new WebApplicationException(e);
				}
			}

			List<String> tableList = reportColumnInfoDTO.getColumnFieldInfoDTO().getTableList();
			if (!tableList.isEmpty()) {
				try {
					reportFilter.setTableList(objectWriter.writeValueAsString(tableList));
				} catch (JsonProcessingException e) {
					throw new WebApplicationException(e);
				}
			}

			this.datatableService.save(reportFilter, connection);

		}
	}

	private void graphReport(Report report, ReportDTO reportDTO, Connection connection) {

		if (reportDTO.getGraphType() == null) {
			throw new FieldRequiredException("ReportDTO.graphType");
		}

		if (reportDTO.getGraphType().length() == 0) {
			throw new FieldRequiredException("ReportDTO.graphType");
		}

		if (reportDTO.getSqlQuery() == null) {
			throw new FieldRequiredException("ReportDTO.sqlQuery");
		}

		if (reportDTO.getSqlQuery().length() == 0) {
			throw new FieldRequiredException("ReportDTO.sqlQuery");
		}

		selectUpdateQueryCheck(true, report);

		report = this.datatableService.save(report, connection);

		addParametersToReport(connection, report);

	}

	private void sqlReport(Report report, ReportDTO reportDTO, Connection connection) {

		selectUpdateQueryCheck(true, report);

		report = this.datatableService.save(report, connection);

		addParametersToReport(connection, report);

	}

	private void jasperReport(Report report, ReportDTO reportDTO, Connection connection) {
		if (reportDTO.getId() == 0) {
			if (!commonService.hasText(reportDTO.getFileName())) {
				throw new FieldRequiredException("ReportDTO.fileName");
			}

			if (!commonService.hasText(reportDTO.getFilePath())) {
				throw new FieldRequiredException("ReportDTO.filePath");
			}
		}

		report = this.datatableService.save(report, connection);

		if (reportDTO.getFilePath() != null) {
			File file = new File(reportDTO.getFilePath());

			if (!file.exists()) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "noFile", null);
			}
			byte[] bytes = null;
			try {
				bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
			} catch (IOException e) {
				throw new WebApplicationException(e);
			}
			ReportJasper reportJasper = new ReportJasper();
			reportJasper.setId(0L);
			reportJasper.setReport(report);
			if (reportDTO.getId() != 0) {
				TableParameterDTO tableParameterDTO = new TableParameterDTO();
				tableParameterDTO.getTableFilters()
						.add(new TableFilter("report", SearchOperation.equals, String.valueOf(report.getId()), null));
				reportJasper = this.datatableService.findAll(tableParameterDTO, ReportJasper.class, connection).get(0);
			}

			reportJasper.setName(reportDTO.getFileName());
			reportJasper.setBytes(bytes);

			this.datatableService.save(reportJasper, connection);

			File jasperPath = new File(AppParameters.jasperreportpath);
			if (!(jasperPath.isDirectory() && jasperPath.exists())) {
				jasperPath.mkdirs();
			}

			File createdJasperFile = new File(jasperPath.getAbsolutePath() + "/" + reportJasper.getName());
			try {
				Files.copy(new ByteArrayInputStream(bytes), Paths.get(createdJasperFile.getAbsolutePath()),
						StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				throw new WebApplicationException(e);
			}

		}
		addParametersToReport(connection, report);

	}

	private void executeReport(Report report, ReportDTO reportDTO, Connection connection) {

		selectUpdateQueryCheck(false, report);

		report = this.datatableService.save(report, connection);

		addParametersToReport(connection, report);

	}

	private void selectUpdateQueryCheck(Boolean isSelect, Report report) {
		net.sf.jsqlparser.statement.Statement statement = null;

		String checkQuery = reportDTO.getSqlQuery();
		for (ReportColumnInfoDTO reportColumnInfoDTO : this.reportDTO.getFilters()) {
			checkQuery = checkQuery.replace("{" + reportColumnInfoDTO.getCode() + "}",
					":" + reportColumnInfoDTO.getCode());
		}

		try {
			statement = CCJSqlParserUtil.parse(checkQuery);
		} catch (JSQLParserException e) {
			throw new WebApplicationException(e);
		}

		if (isSelect) {

			if (!(statement instanceof Select)) {

				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "unallowedQuery", null);

			}
			;
		} else {
			if ((statement instanceof Select)) {

				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "unallowedQuery", null);

			}
			;
		}

		if (report.getType().equals(ReportType.GRAPH)) {

			Select select = (Select) statement;
			if (report.getGraphType().equals(GraphType.PIE) || report.getGraphType().equals(GraphType.BARS)) {
				if (select.getPlainSelect().getSelectItems().size() != 2) {
					throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "queryMustHaveColumn", 2);
				}
			} else {
				if (select.getPlainSelect().getSelectItems().size() != 3) {
					throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "queryMustHaveColumn", 3);
				}
			}

		}

		if (report.getType().equals(ReportType.KPI)) {
			Select select = (Select) statement;
			if (select.getPlainSelect().getSelectItems().size() < 1) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "queryMustHaveColumn", 1);
			}

			if (select.getPlainSelect().getSelectItems().size() > 4) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "queryMustHaveMaximumColumn", 4);
			}
		}

	}

	private void addParametersToReport(Connection connection, Report report) {
		String query = "delete from reportfilter where report=" + report.getId();
		try {
			Statement st = connection.createStatement();
			st.executeUpdate(query);
			st.close();
		} catch (SQLException e) {
			throw new WebApplicationException(e);
		}

		List<String> codes = new ArrayList<>();

		for (ReportColumnInfoDTO reportColumnInfoDTO : this.reportDTO.getFilters()) {
			ReportFilter reportFilter = new ReportFilter();
			reportFilter.setId(0L);
			reportFilter.setReport(report);

			if (codes.contains(reportColumnInfoDTO.getCode())) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "reportFilterCodesMustBeUnique",
						reportColumnInfoDTO.getCode());
			}

			reportFilter.setCode(reportColumnInfoDTO.getCode());
			codes.add(reportFilter.getCode());
			reportFilter.setFieldName(reportColumnInfoDTO.getName());
			reportFilter.setCustomName(reportColumnInfoDTO.getName());
			reportFilter.setFieldType(reportColumnInfoDTO.getColumnType().name());
			reportFilter.setDefaultValue1(reportColumnInfoDTO.getDefaultValue1());
			
			if(commonService.hasText(reportFilter.getDefaultValue1())) {
				commonService.checkDefaultParameter(reportFilter.getDefaultValue1());
			}
			
			if(commonService.hasText(reportFilter.getDefaultValue2())) {
				commonService.checkDefaultParameter(reportFilter.getDefaultValue2());
			}
			
			this.datatableService.save(reportFilter, connection);
		}
	}

}
