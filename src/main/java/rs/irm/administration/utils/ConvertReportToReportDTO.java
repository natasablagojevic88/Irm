package rs.irm.administration.utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.modelmapper.ModelMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.ReportColumnFieldInfoDTO;
import rs.irm.administration.dto.ReportColumnInfoDTO;
import rs.irm.administration.dto.ReportDTO;
import rs.irm.administration.entity.ModelColumn;
import rs.irm.administration.entity.Report;
import rs.irm.administration.entity.ReportColumn;
import rs.irm.administration.entity.ReportFilter;
import rs.irm.administration.enums.ReportType;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.common.service.impl.ResourceBundleServiceImpl;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.ColumnType;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.enums.SortDirection;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.database.utils.LeftTableData;
import rs.irm.database.utils.TableFilter;
import rs.irm.database.utils.TableSort;

public class ConvertReportToReportDTO implements ExecuteMethodWithReturn<ReportDTO> {

	private HttpServletRequest httpServletRequest;
	private Report report;

	private ResourceBundleService resourceBundleService;
	private DatatableService datatableService;
	private ModelMapper modelMapper = new ModelMapper();

	public ConvertReportToReportDTO(HttpServletRequest httpServletRequest, Report report) {
		this.httpServletRequest = httpServletRequest;
		this.report = report;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
		this.resourceBundleService = new ResourceBundleServiceImpl(this.httpServletRequest);
	}

	@Override
	public ReportDTO execute(Connection connection) {
		ReportDTO reportDTO = new ReportDTO();
		modelMapper.map(report, reportDTO);

		if (report.getType().equals(ReportType.STANDARD)) {
			standardReport(reportDTO, connection);
		} else  {
			graphOrSqlReport(reportDTO, connection);
		}

		return reportDTO;
	}

	@SuppressWarnings("unchecked")
	private void standardReport(ReportDTO reportDTO, Connection connection) {

		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("report", SearchOperation.equals, String.valueOf(reportDTO.getId()), null));
		tableParameterDTO.getTableSorts().add(new TableSort("id", SortDirection.ASC));

		List<ReportColumn> reportColumns = this.datatableService.findAll(tableParameterDTO, ReportColumn.class,
				connection);
		for (ReportColumn reportColumn : reportColumns) {
			ReportColumnInfoDTO reportColumnInfo = new ReportColumnInfoDTO();
			reportColumnInfo.setCode(reportColumn.getCode());
			String name = resourceBundleService.getText(reportColumnInfo.getCode(), null);
			ModelColumn modelColumn = reportColumn.getModelColumn();
			name = modelColumn == null ? name : resourceBundleService.getText(modelColumn.getName(), null);
			reportColumnInfo.setName(name);
			reportColumnInfo.setCustomName(reportColumn.getCustomName());
			reportColumnInfo.setModelColumnId(modelColumn == null ? null : modelColumn.getId());
			reportColumnInfo.setColumnType(ColumnType.valueOf(reportColumn.getFieldType()));
			reportColumnInfo.setIcon(null);
			reportColumnInfo.setSqlMetric(reportColumn.getSqlMetric());
			reportColumnInfo.setOrdernum(reportColumn.getOrdernum());
			reportColumnInfo.setSortDirection(reportColumn.getSortDirection());

			ReportColumnFieldInfoDTO reportColumnFieldInfo = new ReportColumnFieldInfoDTO();
			reportColumnFieldInfo.setFieldName(reportColumn.getFieldName());
			reportColumnFieldInfo.setFieldType(reportColumn.getFieldType());
			reportColumnFieldInfo.setLeftJoinPath(reportColumn.getLeftJoinPath());
			reportColumnFieldInfo.setSqlMetric(reportColumn.getSqlMetric());
			reportColumnFieldInfo.setOrdernum(reportColumn.getOrdernum());
			reportColumnFieldInfo.setSortDirection(reportColumn.getSortDirection());

			if (reportColumn.getLeftTableDatas() != null) {
				List<LeftTableData> leftTableDatas = new ArrayList<>();
				JSONParser jsonParser = new JSONParser();
				try {
					JSONArray leftTableDatasJsonArray = (JSONArray) jsonParser.parse(reportColumn.getLeftTableDatas());
					leftTableDatasJsonArray.forEach(c -> {
						JSONObject jsonObject = (JSONObject) c;
						LeftTableData leftTableData = new LeftTableData();
						leftTableData.setFieldColumn(jsonObject.get("fieldColumn").toString());
						leftTableData.setTable(jsonObject.get("table").toString());
						leftTableData.setIdColumn(jsonObject.get("idColumn").toString());
						leftTableData.setPath(jsonObject.get("path").toString());
						leftTableDatas.add(leftTableData);
					});
				} catch (ParseException e) {
					throw new WebApplicationException(e);
				}
				reportColumnFieldInfo.setLeftTableDatas(leftTableDatas);
			}

			if (reportColumn.getColumnList() != null) {
				List<String> columnList = new ArrayList<>();
				JSONParser jsonParser = new JSONParser();
				try {
					JSONArray leftTableDatasJsonArray = (JSONArray) jsonParser.parse(reportColumn.getColumnList());
					leftTableDatasJsonArray.forEach(c -> {
						columnList.add(c.toString());
					});
				} catch (ParseException e) {
					throw new WebApplicationException(e);
				}
				reportColumnFieldInfo.setColumnList(columnList);
			}

			reportColumnInfo.setColumnFieldInfoDTO(reportColumnFieldInfo);

			reportDTO.getColumns().add(reportColumnInfo);
		}

		List<ReportFilter> reportFilters = this.datatableService.findAll(tableParameterDTO, ReportFilter.class,
				connection);
		for (ReportFilter reportFilter : reportFilters) {
			ReportColumnInfoDTO reportColumnInfo = new ReportColumnInfoDTO();
			reportColumnInfo.setCode(reportFilter.getCode());
			String name = resourceBundleService.getText(reportColumnInfo.getCode(), null);
			ModelColumn modelColumn = reportFilter.getModelColumn();
			name = modelColumn == null ? name : resourceBundleService.getText(modelColumn.getName(), null);
			reportColumnInfo.setName(name);
			reportColumnInfo.setCustomName(reportFilter.getCustomName());
			reportColumnInfo.setModelColumnId(modelColumn == null ? null : modelColumn.getId());
			reportColumnInfo.setColumnType(ColumnType.valueOf(reportFilter.getFieldType()));
			reportColumnInfo.setSqlMetric(reportFilter.getSqlMetric());
			reportColumnInfo.setIcon(null);
			reportColumnInfo.setSearchOperation(reportFilter.getSearchOperation());
			reportColumnInfo.setDefaultValue1(reportFilter.getDefaultValue1());
			reportColumnInfo.setDefaultValue2(reportFilter.getDefaultValue2());

			ReportColumnFieldInfoDTO reportColumnFieldInfo = new ReportColumnFieldInfoDTO();
			reportColumnFieldInfo.setFieldName(reportFilter.getFieldName());
			reportColumnFieldInfo.setFieldType(reportFilter.getFieldType());
			reportColumnFieldInfo.setLeftJoinPath(reportFilter.getLeftJoinPath());

			if (reportFilter.getLeftTableDatas() != null) {
				List<LeftTableData> leftTableDatas = new ArrayList<>();
				JSONParser jsonParser = new JSONParser();
				try {
					JSONArray leftTableDatasJsonArray = (JSONArray) jsonParser.parse(reportFilter.getLeftTableDatas());
					leftTableDatasJsonArray.forEach(c -> {
						JSONObject jsonObject = (JSONObject) c;
						LeftTableData leftTableData = new LeftTableData();
						leftTableData.setFieldColumn(jsonObject.get("fieldColumn").toString());
						leftTableData.setTable(jsonObject.get("table").toString());
						leftTableData.setIdColumn(jsonObject.get("idColumn").toString());
						leftTableData.setPath(jsonObject.get("path").toString());
						leftTableDatas.add(leftTableData);
					});
				} catch (ParseException e) {
					throw new WebApplicationException(e);
				}
				reportColumnFieldInfo.setLeftTableDatas(leftTableDatas);
			}

			if (reportFilter.getColumnList() != null) {
				List<String> columnList = new ArrayList<>();
				JSONParser jsonParser = new JSONParser();
				try {
					JSONArray leftTableDatasJsonArray = (JSONArray) jsonParser.parse(reportFilter.getColumnList());
					leftTableDatasJsonArray.forEach(c -> {
						columnList.add(c.toString());
					});
				} catch (ParseException e) {
					throw new WebApplicationException(e);
				}
				reportColumnFieldInfo.setColumnList(columnList);
			}

			reportColumnInfo.setColumnFieldInfoDTO(reportColumnFieldInfo);

			reportDTO.getFilters().add(reportColumnInfo);
		}
	}

	private void graphOrSqlReport(ReportDTO reportDTO, Connection connection) {
		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("report", SearchOperation.equals, String.valueOf(reportDTO.getId()), null));
		tableParameterDTO.getTableSorts().add(new TableSort("id", SortDirection.ASC));

		List<ReportFilter> reportFilters = this.datatableService.findAll(tableParameterDTO, ReportFilter.class,
				connection);
		List<ReportColumnInfoDTO> filters = new ArrayList<>();

		for (ReportFilter reportFilter : reportFilters) {
			ReportColumnInfoDTO reportColumnInfo = new ReportColumnInfoDTO();
			reportColumnInfo.setCode(reportFilter.getCode());
			reportColumnInfo.setName(reportFilter.getFieldName());
			reportColumnInfo.setColumnType(ColumnType.valueOf(reportFilter.getFieldType()));
			reportColumnInfo.setDefaultValue1(reportFilter.getDefaultValue1());
			filters.add(reportColumnInfo);
		}

		reportDTO.setFilters(filters);
	}
}
