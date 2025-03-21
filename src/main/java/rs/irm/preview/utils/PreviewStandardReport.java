package rs.irm.preview.utils;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.ReportDTO;
import rs.irm.administration.dto.SqlResultColumnDTO;
import rs.irm.administration.dto.SqlResultDTO;
import rs.irm.administration.entity.ReportColumn;
import rs.irm.administration.entity.ReportFilter;
import rs.irm.administration.utils.ModelData;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.common.service.impl.ResourceBundleServiceImpl;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.ColumnType;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.enums.SortDirection;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.QueryWriterService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.service.impl.QueryWriterServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.database.utils.FieldInfo;
import rs.irm.database.utils.LeftTable;
import rs.irm.database.utils.LeftTableData;
import rs.irm.database.utils.TableFilter;
import rs.irm.database.utils.TableSort;
import rs.irm.preview.dto.TableReportParameterDTO;

public class PreviewStandardReport implements ExecuteMethodWithReturn<SqlResultDTO> {

	private HttpServletRequest httpServletRequest;
	private TableReportParameterDTO tableReportParameterDTO;
	private Long reportId;
	private DatatableService datatableService;
	private QueryWriterService queryWriterService;
	private ResourceBundleService resourceBundleService;

	public PreviewStandardReport(HttpServletRequest httpServletRequest,
			TableReportParameterDTO tableReportParameterDTO, Long reportId) {
		this.httpServletRequest = httpServletRequest;
		this.tableReportParameterDTO = tableReportParameterDTO;
		this.reportId = reportId;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
		this.queryWriterService = new QueryWriterServiceImpl();
		this.resourceBundleService = new ResourceBundleServiceImpl(this.httpServletRequest);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SqlResultDTO execute(Connection connection) {

		ReportDTO reportDTO = ModelData.listReportDTOs.stream()
				.filter(a -> a.getId().doubleValue() == reportId.doubleValue()).findFirst().get();

		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableSorts().add(new TableSort("id", SortDirection.ASC));
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("report", SearchOperation.equals, String.valueOf(reportId), null));

		List<ReportColumn> reportColumns = this.datatableService.findAll(tableParameterDTO, ReportColumn.class,
				connection);
		List<FieldInfo> fields = new ArrayList<>();

		for (ReportColumn reportColumn : reportColumns) {
			FieldInfo fieldInfo = new FieldInfo();
			fieldInfo.setFieldName("c" + String.valueOf(reportColumn.getId()));
			fieldInfo.setFieldType(reportColumn.getFieldType());
			fieldInfo.setOrdernum(reportColumn.getOrdernum());
			fieldInfo.setSortDirection(reportColumn.getSortDirection());
			fieldInfo.setSqlMetric(reportColumn.getSqlMetric());

			if (reportColumn.getLeftTableDatas() != null) {
				try {
					List<LeftTableData> leftTableDatas = new ArrayList<>();
					JSONArray leftTableJsonArray = (JSONArray) new JSONParser().parse(reportColumn.getLeftTableDatas());

					leftTableJsonArray.forEach(a -> {
						JSONObject jsonObject = (JSONObject) a;
						LeftTableData leftTableData = new LeftTableData();
						leftTableData.setFieldColumn(jsonObject.get("fieldColumn").toString());
						leftTableData.setTable(jsonObject.get("table").toString());
						leftTableData.setIdColumn(jsonObject.get("idColumn").toString());
						leftTableData.setPath(jsonObject.get("path").toString());
						leftTableDatas.add(leftTableData);
					});

					fieldInfo.setLeftTableDatas(leftTableDatas);
				} catch (ParseException e) {
					throw new WebApplicationException(e);
				}
			}

			if (reportColumn.getColumnList() != null) {
				List<String> columnList = new ArrayList<>();
				try {
					JSONArray columnListJSON = (JSONArray) new JSONParser().parse(reportColumn.getColumnList());

					columnListJSON.forEach(a -> {
						String jsonObject = (String) a;
						columnList.add(jsonObject);
					});

					fieldInfo.setColumnList(columnList);
				} catch (ParseException e) {
					throw new WebApplicationException(e);
				}
			}

			fieldInfo.setLeftJoinPath(reportColumn.getLeftJoinPath());

			fields.add(fieldInfo);
		}

		List<ReportFilter> reportFilters = this.datatableService.findAll(tableParameterDTO, ReportFilter.class,
				connection);

		for (ReportFilter reportFilter : reportFilters) {
			FieldInfo fieldInfo = new FieldInfo();
			fieldInfo.setFieldName("f" + String.valueOf(reportFilter.getId()));
			fieldInfo.setFieldType(reportFilter.getFieldType());
			fieldInfo.setSqlMetric(reportFilter.getSqlMetric());

			if (reportFilter.getLeftTableDatas() != null) {
				try {
					List<LeftTableData> leftTableDatas = new ArrayList<>();
					JSONArray leftTableJsonArray = (JSONArray) new JSONParser().parse(reportFilter.getLeftTableDatas());

					leftTableJsonArray.forEach(a -> {
						JSONObject jsonObject = (JSONObject) a;
						LeftTableData leftTableData = new LeftTableData();
						leftTableData.setFieldColumn(jsonObject.get("fieldColumn").toString());
						leftTableData.setTable(jsonObject.get("table").toString());
						leftTableData.setIdColumn(jsonObject.get("idColumn").toString());
						leftTableData.setPath(jsonObject.get("path").toString());
						leftTableDatas.add(leftTableData);
					});

					fieldInfo.setLeftTableDatas(leftTableDatas);
				} catch (ParseException e) {
					throw new WebApplicationException(e);
				}
			}

			if (reportFilter.getColumnList() != null) {
				List<String> columnList = new ArrayList<>();
				try {
					JSONArray columnListJSON = (JSONArray) new JSONParser().parse(reportFilter.getColumnList());

					columnListJSON.forEach(a -> {
						String jsonObject = (String) a;
						columnList.add(jsonObject);
					});

					fieldInfo.setColumnList(columnList);
				} catch (ParseException e) {
					throw new WebApplicationException(e);
				}
			}

			fieldInfo.setLeftJoinPath(reportFilter.getLeftJoinPath());

			fields.add(fieldInfo);
		}

		TableParameterDTO reportTableParameterDTO = new TableParameterDTO();
		reportTableParameterDTO.setPageNumber(tableReportParameterDTO.getPageNumber());
		reportTableParameterDTO.setPageSize(tableReportParameterDTO.getPageSize());

		List<TableFilter> tableFilters = new ArrayList<>();
		List<TableFilter> havingFilters = new ArrayList<>();

		Iterator<Integer> itrParameters = this.tableReportParameterDTO.getParameters().keySet().iterator();

		while (itrParameters.hasNext()) {

			Integer filterColumnId = itrParameters.next();
			ReportFilter reportFilter = reportFilters.stream()
					.filter(a -> a.getId().intValue() == filterColumnId.intValue()).findFirst().get();
			TableFilter tableFilter = new TableFilter();
			tableFilter.setField("f" + filterColumnId);

			String[] params = this.tableReportParameterDTO.getParameters().get(filterColumnId);
			tableFilter.setParameter1(params[0] == null ? null : params[0].toString());
			tableFilter.setParameter2(params[1] == null ? null : params[1].toString());
			tableFilter.setSearchOperation(reportFilter.getSearchOperation());

			if (reportFilter.getSqlMetric() == null) {

				tableFilters.add(tableFilter);
			} else {
				havingFilters.add(tableFilter);
			}

		}

		for (TableSort tableSort : this.tableReportParameterDTO.getSorts()) {
			Integer orderNumber = Integer.valueOf(tableSort.getField());
			TableSort tableSortParameter = new TableSort();
			tableSortParameter.setSortDirection(tableSort.getSortDirection());

			FieldInfo fieldInfo = fields.get(orderNumber - 1);
			tableSortParameter.setField(fieldInfo.getFieldName());
			reportTableParameterDTO.getTableSorts().add(tableSortParameter);
		}

		reportTableParameterDTO.setTableFilters(tableFilters);
		reportTableParameterDTO.setHavingFilters(havingFilters);

		List<LeftTable> leftTables = this.queryWriterService.leftJoinTable(fields);
		String select = this.queryWriterService
				.selectPart(fields.stream().filter(a -> a.getFieldName().startsWith("c")).toList());
		String from = this.queryWriterService.fromPart(reportDTO.getModelCode(), leftTables);
		String where = this.queryWriterService.wherePart(reportTableParameterDTO, fields);
		String groupBy = this.queryWriterService
				.groupByPart(fields.stream().filter(a -> a.getFieldName().startsWith("c")).toList());
		String having = this.queryWriterService.havingPart(reportTableParameterDTO, fields);
		String order = orderPart(reportTableParameterDTO, fields);
		String page = this.queryWriterService.pageablePart(reportTableParameterDTO);

		String query = select + from + where + groupBy + having + order + page;
		String totalSufix = select + from + where+groupBy + having;

		SqlResultDTO sqlResultDTO = executeQuery(query, connection, fields, reportColumns);
		sqlResultDTO.setSqlQuery(query);
		calculateTotal(sqlResultDTO, tableReportParameterDTO, totalSufix, reportColumns,connection);
		return sqlResultDTO;
	}
	
	private String orderPart(TableParameterDTO tableParameterDTO, List<FieldInfo> fieldInfos) {
		try {
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			List<TableSort> tableSorts = tableParameterDTO.getTableSorts();

			if (tableSorts.isEmpty()) {
				List<FieldInfo> fieldInfosSorted = fieldInfos.stream().filter(a -> a.getOrdernum() != null)
						.sorted(Comparator.comparing(FieldInfo::getOrdernum)).toList();
				if (fieldInfosSorted.isEmpty()) {
					bufferedWriter.close();
					return stringWriter.toString();
				}

				for (FieldInfo fieldInfo : fieldInfosSorted) {
					TableSort tableSort = new TableSort();
					tableSort.setField(fieldInfo.getFieldName());
					tableSort.setSortDirection(fieldInfo.getSortDirection());
					tableParameterDTO.getTableSorts().add(tableSort);
				}
			}
			bufferedWriter.newLine();
			bufferedWriter.write("order by");

			int index = 0;
			for (TableSort tableSort : tableSorts) {
				index++;

				bufferedWriter.newLine();
				
				int findIndex=0;
				for(FieldInfo fieldInfo:fieldInfos) {
					findIndex++;
					if(!fieldInfo.getFieldName().equals(tableSort.getField())){
						continue;
					}
					break;
				}
				
				bufferedWriter.write(
						findIndex + " " + tableSort.getSortDirection().name());

				if (index != tableSorts.size()) {
					bufferedWriter.write(",");
				}
			}

			bufferedWriter.close();
			return stringWriter.toString();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	private SqlResultDTO executeQuery(String query, Connection connection, List<FieldInfo> fields,
			List<ReportColumn> reportColumns) {
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			SqlResultDTO resultDTO = new SqlResultDTO();

			List<SqlResultColumnDTO> columns = new ArrayList<>();
			for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
				String columnResultName = resultSet.getMetaData().getColumnName(i);
				Integer columnId = Integer.valueOf(columnResultName.substring(1, columnResultName.length()));
				SqlResultColumnDTO resultColumnDTO = new SqlResultColumnDTO();
				ReportColumn reportColumn = reportColumns.stream()
						.filter(a -> a.getId().doubleValue() == columnId.doubleValue()).findFirst().get();

				String name = resourceBundleService.getText(reportColumn.getCode(), null);

				if (reportColumn.getModelColumn() != null) {
					name = resourceBundleService.getText(reportColumn.getModelColumn().getName(), null);
				}
				if(reportColumn.getSqlMetric()!=null) {
					name=name+" - "+reportColumn.getSqlMetric().name();
				}

				resultColumnDTO.setName(reportColumn.getCustomName() != null ? reportColumn.getCustomName() : name);
				
				resultColumnDTO.setOrderNumber(i);
				resultColumnDTO.setType(ColumnType.valueOf(reportColumn.getFieldType()));
				resultColumnDTO.setSqlMetric(reportColumn.getSqlMetric());
				columns.add(resultColumnDTO);
			}

			while (resultSet.next()) {
				LinkedHashMap<Integer, Object> data = new LinkedHashMap<>();

				for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
					String columnResultName = resultSet.getMetaData().getColumnName(i);
					Integer columnId = Integer.valueOf(columnResultName.substring(1, columnResultName.length()));

					if (resultSet.getObject(i) != null) {

						ReportColumn reportColumn = reportColumns.stream()
								.filter(a -> a.getId().doubleValue() == columnId.doubleValue()).findFirst().get();

						String columnType = reportColumn.getFieldType();

						switch (columnType) {
						case "Long": {
							Number number = (Number) resultSet.getObject(i);
							data.put(i, number.longValue());
							break;
						}
						case "Integer": {
							Number number = (Number) resultSet.getObject(i);
							data.put(i, number.intValue());
							break;
						}
						case "BigDecimal": {
							Number number = (Number) resultSet.getObject(i);
							data.put(i, BigDecimal.valueOf(number.doubleValue()));
							break;
						}
						case "LocalDate": {
							java.sql.Date date = (java.sql.Date) resultSet.getObject(i);
							data.put(i, date.toLocalDate());
							break;
						}
						case "LocalDateTime": {
							java.sql.Timestamp date = (java.sql.Timestamp) resultSet.getObject(i);
							data.put(i, date.toLocalDateTime());
							break;
						}
						case "Boolean": {

							data.put(i, Boolean.valueOf(resultSet.getObject(i).toString()));
							break;
						}
						case "String": {

							data.put(i, resultSet.getObject(i).toString());
							break;
						}
						}

					} else {
						data.put(i, null);
					}

				}

				resultDTO.getList().add(data);
			}

			resultDTO.setColumns(columns);

			resultSet.close();
			statement.close();

			return resultDTO;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	private void calculateTotal(SqlResultDTO sqlResultDTO, TableReportParameterDTO tableReportParameterDTO,
			String totalSufix,List<ReportColumn> reportColumns, Connection connection) {

		String query = "select count(*)";
		
		for(Integer totalColumn:tableReportParameterDTO.getTotals()) {
			ReportColumn reportColumn=reportColumns.get(totalColumn-1);
			query+=",";
			query+=reportColumn.getSqlMetric().name()+"(";
			query+="c"+reportColumn.getId();
			query+=")";
			
		}
		if(!tableReportParameterDTO.getTotals().isEmpty()) {
			sqlResultDTO.setHasTotal(true);
		}
		
		query+=" from (" + totalSufix + ") as t";

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			resultSet.next();

			Long totalItemNumber = ((Number) resultSet.getObject(1)).longValue();

			sqlResultDTO.setTotalItems(totalItemNumber);

			Long numberOfPages = totalItemNumber / tableReportParameterDTO.getPageSize();

			if (totalItemNumber % tableReportParameterDTO.getPageSize() != 0) {
				numberOfPages = numberOfPages + 1;
			}
			
			sqlResultDTO.setNumberOfPages(numberOfPages.intValue());
			
			int index=1;
			for(Integer totalColumn:tableReportParameterDTO.getTotals()) {
				index++;
				
				Object valueTotal=resultSet.getObject(index);
				
				if(valueTotal==null) {
					sqlResultDTO.getTotalsColumn().put(totalColumn, null);
				}else {
					String className= resultSet.getMetaData().getColumnClassName(index);
					Class<?> resultClass=Class.forName(className);
					
					String classNameSimple=resultClass.getSimpleName();
					
					switch(classNameSimple) {
					case "Date":{
						java.sql.Date date=(java.sql.Date) valueTotal;
						sqlResultDTO.getTotalsColumn().put(totalColumn, date.toLocalDate());
						break;
					}
					case "Timestamp":{
						java.sql.Timestamp date=(java.sql.Timestamp) valueTotal;
						sqlResultDTO.getTotalsColumn().put(totalColumn, date.toLocalDateTime());
						break;
					}
					case "BigDecimal":{
						BigDecimal number=(BigDecimal) valueTotal;
						sqlResultDTO.getTotalsColumn().put(totalColumn, number.doubleValue());
						break;
					}
					default:{
						sqlResultDTO.getTotalsColumn().put(totalColumn, valueTotal.toString());
						break;
					}
					}
				}
			}
			resultSet.close();
			statement.close();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

	}

}
