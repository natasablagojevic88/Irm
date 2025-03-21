package rs.irm.preview.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.SqlResultDTO;
import rs.irm.administration.entity.Report;
import rs.irm.administration.entity.ReportFilter;
import rs.irm.common.enums.Language;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.database.utils.TableFilter;
import rs.irm.preview.dto.TableReportParameterDTO;
import rs.irm.utils.AppParameters;

public class PreviewKpiReport implements ExecuteMethodWithReturn<SqlResultDTO>{
	
	private HttpServletRequest httpServletRequest;
	private TableReportParameterDTO tableReportParameterDTO;
	private Long reportId;
	private DatatableService datatableService;
	
	public PreviewKpiReport(HttpServletRequest httpServletRequest, TableReportParameterDTO tableReportParameterDTO,
			Long reportId) {
		this.httpServletRequest = httpServletRequest;
		this.tableReportParameterDTO = tableReportParameterDTO;
		this.reportId = reportId;
		this.datatableService=new DatatableServiceImpl(this.httpServletRequest);
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
		SqlResultDTO sqlResultDTO=new SqlResultDTO();
		try {
			Statement statement=connection.createStatement();
			ResultSet resultSet=statement.executeQuery(query);
			
			Class<?> typeClass=Class.forName(resultSet.getMetaData().getColumnClassName(1));
			String typeName=typeClass.getSimpleName();
			LinkedHashMap<Integer, Object> result=new LinkedHashMap<>();
			
			if(resultSet.next()) {
				
				Object value=resultSet.getObject(1);
				
				if(value==null) {
					result.put(1, "");
				}else {

					switch(typeName) {
					case "Integer","BigDecimal","Long":{
						NumberFormat numberFormat=NumberFormat.getInstance(findLocale());
						numberFormat.setGroupingUsed(true);
						numberFormat.setMaximumFractionDigits(10);
						Number number=(Number) value;
						result.put(1, numberFormat.format(number));
						break;
					}
					case "Date":{
						java.sql.Date date=(java.sql.Date) value;
						DateTimeFormatter dateTimeFormatter=DateTimeFormatter.ofPattern("dd.MM.yyyy");
						result.put(1, dateTimeFormatter.format(date.toLocalDate()));
						break;
					}
					case "Timestamp":{
						java.sql.Timestamp date=(java.sql.Timestamp) value;
						DateTimeFormatter dateTimeFormatter=DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
						result.put(1, dateTimeFormatter.format(date.toLocalDateTime()));
						break;
					}
					default:{
						result.put(1, value.toString());
					}
					}
				}
				
				
				if(resultSet.getMetaData().getColumnCount()>1) {
					if(resultSet.getObject(2)==null) {
						result.put(2, "");
					}else {
						result.put(2, resultSet.getObject(2).toString());
					}
				}
				
				if(resultSet.getMetaData().getColumnCount()>2) {
					if(resultSet.getObject(3)==null) {
						result.put(3, "");
					}else {
						result.put(3, resultSet.getObject(3).toString());
					}
				}
				
				if(resultSet.getMetaData().getColumnCount()>3) {
					if(resultSet.getObject(4)==null) {
						result.put(4, "");
					}else {
						result.put(4, resultSet.getObject(4).toString());
					}
				}
			}else {
				result.put(1, "");
				result.put(2, "");
				result.put(3, "");
				result.put(4, "");
			}
			sqlResultDTO.getList().add(result);
			
			resultSet.close();
			statement.close();
		}catch(Exception e) {
			throw new WebApplicationException(e);
		}
		
		return sqlResultDTO;
	}
	
	private Locale findLocale() {
		Language language=Language.valueOf(AppParameters.defaultlang);
		if(httpServletRequest!=null) {
			if(httpServletRequest.getAttribute("language")!=null) {
				language=Language.valueOf(httpServletRequest.getAttribute("language").toString());
			}
		}
		
		Locale locale=language.locale;
		
		return locale;
	}
	
	

}
