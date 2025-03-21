package rs.irm.preview.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.entity.ReportFilter;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.common.service.impl.ResourceBundleServiceImpl;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.enums.SortDirection;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.database.utils.TableFilter;
import rs.irm.database.utils.TableSort;
import rs.irm.preview.dto.ReportParameterDTO;

public class CreateStandardParameters implements ExecuteMethodWithReturn<List<ReportParameterDTO>> {

	private Long reportId;
	private HttpServletRequest httpServletRequest;

	private DatatableService datatableService;
	private ResourceBundleService resourceBundleService;

	public CreateStandardParameters(Long reportId, HttpServletRequest httpServletRequest) {
		this.reportId = reportId;
		this.httpServletRequest = httpServletRequest;

		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
		this.resourceBundleService = new ResourceBundleServiceImpl(this.httpServletRequest);
	}

	@Override
	public List<ReportParameterDTO> execute(Connection connection) {
		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("report", SearchOperation.equals, String.valueOf(reportId), null));
		tableParameterDTO.getTableSorts().add(new TableSort("id", SortDirection.ASC));

		List<ReportFilter> list = this.datatableService.findAll(tableParameterDTO, ReportFilter.class, connection);

		List<ReportParameterDTO> listDtos = new ArrayList<>();

		ModelMapper customMapper = new ModelMapper();
		customMapper.addMappings(new PropertyMap<ReportFilter, ReportParameterDTO>() {

			@Override
			protected void configure() {
				skip(destination.getName());

			}
		});
		for (ReportFilter reportFilter : list) {
			ReportParameterDTO parameterDTO = new ReportParameterDTO();
			customMapper.map(reportFilter, parameterDTO);
			parameterDTO.setCustomname(reportFilter.getCustomName());
			listDtos.add(parameterDTO);
		}

		for (ReportParameterDTO reportParameterDTO : listDtos) {
			String name = resourceBundleService.getText(reportParameterDTO.getCode(), null);

			if (reportParameterDTO.getModelColumnName() != null) {
				name = resourceBundleService.getText(reportParameterDTO.getModelColumnName(), null);

			}

			reportParameterDTO.setName(reportParameterDTO.getCustomname() != null
					? resourceBundleService.getText(reportParameterDTO.getCustomname(), null)
					: name);
			
			
			if(reportParameterDTO.getSqlMetric()!=null) {
				reportParameterDTO.setName(reportParameterDTO.getName()+" - "+reportParameterDTO.getSqlMetric().name());
			}

			if (reportParameterDTO.getDefaultValue1() != null) {
				try {
					Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery(reportParameterDTO.getDefaultValue1());

					if (resultSet.next()) {
						if (resultSet.getObject(1) == null) {
							reportParameterDTO.setDefaultValue1(null);
						} else {
							reportParameterDTO.setDefaultValue1(resultSet.getObject(1).toString());
						}
					} else {
						reportParameterDTO.setDefaultValue1(null);
					}

					resultSet.close();
					statement.close();
				} catch (SQLException e) {
					throw new WebApplicationException(e);
				}

			} else {
				if (reportParameterDTO.getFieldType().equals("Boolean")) {
					reportParameterDTO.setDefaultValue1("true");
				}
			}

			if (reportParameterDTO.getDefaultValue2() != null) {
				try {
					Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery(reportParameterDTO.getDefaultValue2());

					if (resultSet.next()) {
						if (resultSet.getObject(1) == null) {
							reportParameterDTO.setDefaultValue2(null);
						} else {
							reportParameterDTO.setDefaultValue2(resultSet.getObject(1).toString());
						}
					} else {
						reportParameterDTO.setDefaultValue2(null);
					}

					resultSet.close();
					statement.close();
				} catch (SQLException e) {
					throw new WebApplicationException(e);
				}

			}
		}

		return listDtos;
	}

}
