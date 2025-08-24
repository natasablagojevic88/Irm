package rs.irm.preview.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.entity.Report;
import rs.irm.administration.entity.ReportFilter;
import rs.irm.administration.enums.ReportType;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.database.utils.TableFilter;
import rs.irm.preview.dto.CardParameterDTO;
import rs.irm.preview.dto.CardResultDTO;

public class CreateCardReport implements ExecuteMethodWithReturn<CardResultDTO> {

	private Long reportId;
	private HttpServletRequest httpServletRequest;
	private DatatableService datatableService;

	public CreateCardReport() {
	}

	public CreateCardReport(Long reportId, HttpServletRequest httpServletRequest) {
		this.reportId = reportId;
		this.httpServletRequest = httpServletRequest;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
	}

	@Override
	public CardResultDTO execute(Connection connection) {

		Report report = this.datatableService.findByExistingId(reportId, Report.class, connection);
		
		if(!report.getType().equals(ReportType.CARD)) {
			return new CardResultDTO();
		}

		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("report", SearchOperation.equals, String.valueOf(reportId), null));
		List<ReportFilter> filters = this.datatableService.findAll(tableParameterDTO, ReportFilter.class, connection);

		CardResultDTO cardResultDTO = new CardResultDTO();

		String query = report.getSqlQuery();

		for (ReportFilter filter : filters) {
			CardParameterDTO cardParameters = new CardParameterDTO();
			cardParameters.setFilter(filter.getCode());
			cardParameters.setName(filter.getFieldName());
			List<ComboboxDTO> comboboxDTOs = new ArrayList<>();

			try {
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(filter.getDefaultValue1());

				while (resultSet.next()) {
					ComboboxDTO comboboxDTO = new ComboboxDTO(resultSet.getObject(1),
							resultSet.getObject(2).toString());
					comboboxDTOs.add(comboboxDTO);
				}

				resultSet.close();
				statement.close();

				query = query.replace("{" + cardParameters.getFilter() + "}", "''");

			} catch (Exception e) {
				throw new WebApplicationException(e);
			}

			cardParameters.setValues(comboboxDTOs);

			cardResultDTO.getParameters().add(cardParameters);
		}

		cardResultDTO.setResult(createListOfResult(connection, query));

		return cardResultDTO;
	}
	
	public List<LinkedHashMap<String, Object>> createListOfResult(Connection connection,String query){
		
		List<LinkedHashMap<String, Object>> list=new ArrayList<>();
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			while (resultSet.next()) {
				for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
					LinkedHashMap<String, Object> result=new LinkedHashMap<>();
					result.put("name", resultSet.getMetaData().getColumnName(i));
					result.put("value", resultSet.getObject(i));
					list.add(result);
				}
			}

			resultSet.close();
			statement.close();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
		
		return list;
	}

}
