package rs.irm.preview.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.irm.administration.dto.SqlResultDTO;
import rs.irm.administration.entity.Report;
import rs.irm.administration.entity.ReportFilter;
import rs.irm.administration.enums.GraphType;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.common.service.impl.ResourceBundleServiceImpl;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.database.utils.TableFilter;
import rs.irm.preview.dto.ChartOptionsDTO;
import rs.irm.preview.dto.ChartOptionsDataDTO;
import rs.irm.preview.dto.TableReportParameterDTO;

public class PreviewGraphReport implements ExecuteMethodWithReturn<SqlResultDTO> {

	private HttpServletRequest httpServletRequest;
	private TableReportParameterDTO tableReportParameterDTO;
	private Long reportId;
	private DatatableService datatableService;
	private ResourceBundleService resourceBundleService;
	DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	
	public PreviewGraphReport() {
	}

	public PreviewGraphReport(HttpServletRequest httpServletRequest, TableReportParameterDTO tableReportParameterDTO,
			Long reportId) {
		this.httpServletRequest = httpServletRequest;
		this.tableReportParameterDTO = tableReportParameterDTO;
		this.reportId = reportId;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
		this.resourceBundleService = new ResourceBundleServiceImpl(this.httpServletRequest);
	}

	@Override
	public SqlResultDTO execute(Connection connection) {
		SqlResultDTO sqlResultDTO = new SqlResultDTO();

		Report report = this.datatableService.findByExistingId(reportId, Report.class, connection);

		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("report", SearchOperation.equals, String.valueOf(reportId), null));
		List<ReportFilter> filters = this.datatableService.findAll(tableParameterDTO, ReportFilter.class, connection);
		String query = report.getSqlQuery();
		query = addParametersToQuery(query,this.tableReportParameterDTO, filters);

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			if(report.getGraphType().equals(GraphType.BARS)) {
				createBarsGraph(resultSet, report, sqlResultDTO);
			}else if(report.getGraphType().equals(GraphType.PIE)) {
				createPieGraph(resultSet, report, sqlResultDTO);
			}else if(report.getGraphType().equals(GraphType.LINES)) {
				createLinesGraph(resultSet, report, sqlResultDTO);
			}

			resultSet.close();
			statement.close();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

		return sqlResultDTO;
	}

	public String addParametersToQuery(String query,TableReportParameterDTO tableReportParameterDTO, List<ReportFilter> filters) {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		for (ReportFilter reportFilter : filters) {
			if (!tableReportParameterDTO.getParameters().containsKey(reportFilter.getId().intValue())) {
				query = query.replace("{" + reportFilter.getCode() + "}", "null");
				continue;
			}

			String[] parameters = tableReportParameterDTO.getParameters().get(reportFilter.getId().intValue());

			if (parameters == null) {
				query = query.replace("{" + reportFilter.getCode() + "}", "null");
				continue;
			}

			String columnType = reportFilter.getFieldType();

			String parameter = parameters[0];

			if (parameter == null) {
				query = query.replace("{" + reportFilter.getCode() + "}", "null");
				continue;
			}

			if (parameter.length() == 0) {
				query = query.replace("{" + reportFilter.getCode() + "}", "null");
				continue;
			}

			NumberFormat numberFormatin = NumberFormat.getInstance(Locale.US);
			numberFormatin.setGroupingUsed(true);
			numberFormatin.setMaximumFractionDigits(10);

			NumberFormat numberFormatout = NumberFormat.getInstance(Locale.US);
			numberFormatout.setGroupingUsed(false);
			numberFormatout.setMaximumFractionDigits(10);



			switch (columnType) {
			case "String": {
				query = query.replace("{" + reportFilter.getCode() + "}", "'" + parameter + "'");
				break;
			}
			case "Long", "Integer", "BigDecimal": {
				Number number = null;
				try {
					number = numberFormatin.parse(parameter);
				} catch (ParseException e) {
					throw new WebApplicationException(e);
				}
				query = query.replace("{" + reportFilter.getCode() + "}", numberFormatout.format(number));
				break;
			}
			case "LocalDate": {
				LocalDate date = LocalDate.parse(parameter);
				query = query.replace("{" + reportFilter.getCode() + "}", "'" + dateFormatter.format(date) + "'");
				break;
			}
			case "LocalDateTime": {
				LocalDateTime date = LocalDateTime.parse(parameter);
				query = query.replace("{" + reportFilter.getCode() + "}", "'" + dateTimeFormatter.format(date) + "'");
				break;
			}
			case "Boolean": {
				Boolean bool = Boolean.valueOf(parameter);
				query = query.replace("{" + reportFilter.getCode() + "}", bool.toString());
				break;
			}
			}

		}

		return query;
	}
	
	private void createBarsGraph(ResultSet resultSet, Report report,SqlResultDTO sqlResultDTO) throws Exception{
		String[] columnNames = new String[resultSet.getMetaData().getColumnCount()];

		for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
			columnNames[i - 1] = resultSet.getMetaData().getColumnName(i);
		}

		ChartOptionsDTO chartOptionsDTO = new ChartOptionsDTO();
		LinkedHashMap<String, Object> titleChart = new LinkedHashMap<>();
		titleChart.put("text", resourceBundleService.getText(report.getName(), columnNames));
		chartOptionsDTO.setTitle(titleChart);
		chartOptionsDTO.setAnimationEnabled(true);

		LinkedHashMap<String, Object> axisYChart = new LinkedHashMap<>();
		axisYChart.put("includeZero", true);
		chartOptionsDTO.setAxisY(axisYChart);

		List<ChartOptionsDataDTO> dataChart = new ArrayList<>();
		ChartOptionsDataDTO data = new ChartOptionsDataDTO();
		data.setType("column");
		data.setIndexLabelFontColor("#5A5757");

		while (resultSet.next()) {

			Object[] values = new Object[columnNames.length];

			for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
				
				Class<?> classOfColumn=Class.forName(resultSet.getMetaData().getColumnClassName(i));
				
				String columnType=classOfColumn.getSimpleName();
				switch(columnType) {
				case "Date":{
					java.sql.Date date=(java.sql.Date)resultSet.getObject(i);
					values[i - 1] = date.toLocalDate();
					break;
				}
				case "Timestamp":{
					java.sql.Timestamp date=(java.sql.Timestamp)resultSet.getObject(i);
					values[i - 1] = date.toLocalDateTime();
					break;
				}
				default:{
					values[i - 1] = resultSet.getObject(i);
				}
				}
				
				
			}

			LinkedHashMap<Object, Object> datapoint = new LinkedHashMap<>();
			datapoint.put("label", values[0]);
			datapoint.put("y", values[1]);
			data.getDataPoints().add(datapoint);

		}
		dataChart.add(data);

		chartOptionsDTO.setData(dataChart);

		sqlResultDTO.setChartOptions(chartOptionsDTO);
	}
	
	private void createPieGraph(ResultSet resultSet, Report report,SqlResultDTO sqlResultDTO) throws Exception{
		String[] columnNames = new String[resultSet.getMetaData().getColumnCount()];

		for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
			columnNames[i - 1] = resultSet.getMetaData().getColumnName(i);
		}

		ChartOptionsDTO chartOptionsDTO = new ChartOptionsDTO();
		LinkedHashMap<String, Object> titleChart = new LinkedHashMap<>();
		titleChart.put("text", resourceBundleService.getText(report.getName(), columnNames));
		chartOptionsDTO.setTitle(titleChart);
		chartOptionsDTO.setTheme("light2");
		chartOptionsDTO.setAnimationEnabled(true);

		List<ChartOptionsDataDTO> dataChart = new ArrayList<>();
		ChartOptionsDataDTO data = new ChartOptionsDataDTO();
		data.setType("pie");
		data.setIndexLabel("{name}: {y}");

		while (resultSet.next()) {

			Object[] values = new Object[columnNames.length];

			for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
				
				Class<?> classOfColumn=Class.forName(resultSet.getMetaData().getColumnClassName(i));
				
				String columnType=classOfColumn.getSimpleName();
				switch(columnType) {
				case "Date":{
					java.sql.Date date=(java.sql.Date)resultSet.getObject(i);
					values[i - 1] = date.toLocalDate();
					break;
				}
				case "Timestamp":{
					java.sql.Timestamp date=(java.sql.Timestamp)resultSet.getObject(i);
					values[i - 1] = date.toLocalDateTime();
					break;
				}
				default:{
					values[i - 1] = resultSet.getObject(i);
				}
				}
				
				
			}

			LinkedHashMap<Object, Object> datapoint = new LinkedHashMap<>();
			datapoint.put("name", values[0]);
			datapoint.put("y", values[1]);
			data.getDataPoints().add(datapoint);

		}
		dataChart.add(data);

		chartOptionsDTO.setData(dataChart);

		sqlResultDTO.setChartOptions(chartOptionsDTO);
	}
	
	private void createLinesGraph(ResultSet resultSet, Report report,SqlResultDTO sqlResultDTO) throws Exception{
		String[] columnNames = new String[resultSet.getMetaData().getColumnCount()];

		for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
			columnNames[i - 1] = resultSet.getMetaData().getColumnName(i);
		}

		ChartOptionsDTO chartOptionsDTO = new ChartOptionsDTO();
		LinkedHashMap<String, Object> titleChart = new LinkedHashMap<>();
		titleChart.put("text", resourceBundleService.getText(report.getName(), columnNames));
		chartOptionsDTO.setTitle(titleChart);
		chartOptionsDTO.setTheme("light2");
		chartOptionsDTO.setAnimationEnabled(true);
		
		LinkedHashMap<String, Object> axisYChart = new LinkedHashMap<>();
		axisYChart.put("title", columnNames[2]);
		chartOptionsDTO.setAxisY(axisYChart);

		List<LinesData> linesDatas=new ArrayList<>();

		while (resultSet.next()) {

			Object[] values = new Object[columnNames.length];

			for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
				
				Class<?> classOfColumn=Class.forName(resultSet.getMetaData().getColumnClassName(i));
				
				String columnType=classOfColumn.getSimpleName();
				switch(columnType) {
				case "Date":{
					java.sql.Date date=(java.sql.Date)resultSet.getObject(i);
					values[i - 1] = date.toLocalDate();
					break;
				}
				case "Timestamp":{
					java.sql.Timestamp date=(java.sql.Timestamp)resultSet.getObject(i);
					values[i - 1] = date.toLocalDateTime();
					break;
				}
				default:{
					values[i - 1] = resultSet.getObject(i);
				}
				}
				
				
			}
			
			LinesData linesData=new LinesData(values[0], values[1], values[2]);
			linesDatas.add(linesData);


		}
		
		List<Object> lines=linesDatas.stream().map(a->a.getObject1()).distinct().toList();
		
		for(Object line:lines) {
			ChartOptionsDataDTO chartOptionsDataDTO=new ChartOptionsDataDTO();
			chartOptionsDataDTO.setType("line");
			chartOptionsDataDTO.setName(line.toString());
			chartOptionsDataDTO.setShowInLegend(true);
			
			List<LinesData> objectForLinesDatas=linesDatas.stream().filter(a->a.getObject1().equals(line)).toList();
			
			List<LinkedHashMap<Object, Object>> dataPoints=new ArrayList<>();
			
			for(LinesData linesData:objectForLinesDatas) {
				LinkedHashMap<Object, Object> dataPoint=new LinkedHashMap<>();
				dataPoint.put("label", linesData.getObject2());
				dataPoint.put("y", linesData.getObject3());
				dataPoints.add(dataPoint);
			}
			
			chartOptionsDataDTO.setDataPoints(dataPoints);
			
			chartOptionsDTO.getData().add(chartOptionsDataDTO);
		}


		sqlResultDTO.setChartOptions(chartOptionsDTO);
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	private class LinesData{
		private Object object1;
		private Object object2;
		private Object object3;
	}


}


