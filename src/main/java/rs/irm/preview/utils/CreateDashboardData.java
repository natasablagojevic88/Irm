package rs.irm.preview.utils;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.irm.administration.dto.DashboardDTO;
import rs.irm.administration.dto.DashboardItemDTO;
import rs.irm.administration.entity.DefaultDashboard;
import rs.irm.administration.entity.ReportFilter;
import rs.irm.administration.enums.ReportType;
import rs.irm.administration.utils.ModelData;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.common.service.impl.CommonServiceImpl;
import rs.irm.common.service.impl.ResourceBundleServiceImpl;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.database.utils.TableFilter;
import rs.irm.preview.dto.DashboardResultDTO;
import rs.irm.preview.dto.DashboardResultItemDTO;
import rs.irm.preview.dto.TableReportParameterDTO;

public class CreateDashboardData implements ExecuteMethodWithReturn<DashboardResultDTO> {

	private HttpServletRequest httpServletRequest;
	private Long dashboardId;

	private DatatableService datatableService;
	private ResourceBundleService resourceBundleService;
	private CommonService commonService;

	public CreateDashboardData(HttpServletRequest httpServletRequest, Long dashboardId) {
		this.httpServletRequest = httpServletRequest;
		this.dashboardId = dashboardId;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
		this.resourceBundleService = new ResourceBundleServiceImpl(this.httpServletRequest);
		this.commonService = new CommonServiceImpl(this.httpServletRequest);
	}

	@Override
	public DashboardResultDTO execute(Connection connection) {
		DashboardResultDTO dashboardResultDTO = new DashboardResultDTO();

		DashboardDTO dashboardDTO = ModelData.listDashboardDTOs.stream()
				.filter(a -> a.getId().doubleValue() == dashboardId.doubleValue()).findFirst().get();

		dashboardResultDTO.setId(dashboardId);
		dashboardResultDTO.setTitle(resourceBundleService.getText(dashboardDTO.getName(), null));
		dashboardResultDTO.setNumberOfRows(dashboardDTO.getRownumber());
		dashboardResultDTO.setNumberOfColumns(dashboardDTO.getColumnnumber());
		checkDefault(dashboardResultDTO, connection);

		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("dashboardId", SearchOperation.equals, String.valueOf(dashboardId), null));
		List<DashboardItemDTO> dashboardItems = this.datatableService.findAll(tableParameterDTO, DashboardItemDTO.class,
				connection);

		List<usedItem> usedData = new ArrayList<>();

		for (int i = 1; i <= dashboardDTO.getRownumber(); i++) {
			for (int k = 1; k <= dashboardDTO.getColumnnumber(); k++) {
				final int j = i;
				final int q = k;
				if (!usedData.stream().filter(a -> a.getRow() == j && a.getColumn() == q).toList().isEmpty()) {
					continue;
				}

				List<DashboardItemDTO> foundDashboardItemDTOs = dashboardItems.stream()
						.filter(a -> a.getRow() == j && a.getColumn() == q).toList();

				DashboardResultItemDTO dashboardResultItemDTO = new DashboardResultItemDTO();
				dashboardResultItemDTO.setRow(j);
				dashboardResultItemDTO.setColumn(q);
				dashboardResultItemDTO.setColspan(1);
				dashboardResultItemDTO.setRowspan(1);

				if (!foundDashboardItemDTOs.isEmpty()) {
					DashboardItemDTO dashboardItemDTO = foundDashboardItemDTOs.get(0);

					dashboardResultItemDTO.setColspan(dashboardItemDTO.getColspan());
					dashboardResultItemDTO.setColumn(dashboardItemDTO.getColumn());
					dashboardResultItemDTO
							.setName(resourceBundleService.getText(dashboardItemDTO.getReportName(), null));
					dashboardResultItemDTO.setReportId(dashboardItemDTO.getReportId());
					dashboardResultItemDTO.setReportType(ReportType.valueOf(dashboardItemDTO.getReportType()));
					dashboardResultItemDTO.setRow(dashboardItemDTO.getRow());
					dashboardResultItemDTO.setRowspan(dashboardItemDTO.getRowspan());
					addParameters(dashboardResultItemDTO, connection);
					addReportResult(connection, dashboardResultItemDTO);
				}

				dashboardResultDTO.getItems().add(dashboardResultItemDTO);

				usedItem use = new usedItem(dashboardResultItemDTO.getRow(), dashboardResultItemDTO.getColumn());
				usedData.add(use);

				for (int m = 1; m < dashboardResultItemDTO.getColspan(); m++) {
					use = new usedItem(dashboardResultItemDTO.getRow(), dashboardResultItemDTO.getColumn() + m);
					usedData.add(use);
				}

				for (int n = 1; n < dashboardResultItemDTO.getRowspan(); n++) {
					use = new usedItem(dashboardResultItemDTO.getRow() + n, dashboardResultItemDTO.getColumn());
					usedData.add(use);

					for (int m = 1; m < dashboardResultItemDTO.getColspan(); m++) {
						use = new usedItem(dashboardResultItemDTO.getRow() + n, dashboardResultItemDTO.getColumn() + m);
						usedData.add(use);
					}
				}

			}
		}

		return dashboardResultDTO;
	}

	private void addParameters(DashboardResultItemDTO item, Connection connection) {

		item.setParameters(createParameter(item.getReportId(), connection));
	}

	public LinkedHashMap<Integer, String[]> createParameter(Long reportId, Connection connection) {
		TableParameterDTO parameterDTO = new TableParameterDTO();
		parameterDTO.getTableFilters()
				.add(new TableFilter("report", SearchOperation.equals, String.valueOf(reportId), null));
		List<ReportFilter> filters = this.datatableService.findAll(parameterDTO, ReportFilter.class, connection);
		LinkedHashMap<Integer, String[]> parameters = new LinkedHashMap<>();
		for (ReportFilter reportFilter : filters) {

			String[] params = new String[2];

			if (reportFilter.getFieldType().equals("Boolean")) {
				params[0] = "true";
			}

			if (commonService.hasText(reportFilter.getDefaultValue1())) {
				try {
					Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery(reportFilter.getDefaultValue1());
					if (resultSet.next()) {
						if (resultSet.getObject(1) != null) {
							params[0] = resultSet.getObject(1).toString();
						}
					}
					resultSet.close();
					statement.close();
				} catch (Exception e) {

				}
			}

			if (commonService.hasText(reportFilter.getDefaultValue2())) {
				try {
					Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery(reportFilter.getDefaultValue2());
					if (resultSet.next()) {
						if (resultSet.getObject(1) != null) {
							params[1] = resultSet.getObject(1).toString();
						}
					}
					resultSet.close();
					statement.close();
				} catch (Exception e) {

				}
			}

			parameters.put(reportFilter.getId().intValue(), params);
		}

		return parameters;
	}

	private void addReportResult(Connection connection, DashboardResultItemDTO item) {
		TableReportParameterDTO tableReportParameterDTO = new TableReportParameterDTO();
		tableReportParameterDTO.setParameters(item.getParameters());
		Long reportId = item.getReportId();

		String typeOfReport = item.getReportType().name();
		switch (typeOfReport) {
		case "STANDARD": {
			tableReportParameterDTO.setPageNumber(0);
			tableReportParameterDTO.setPageSize(20);

			PreviewStandardReport previewStandardReport = new PreviewStandardReport(httpServletRequest,
					tableReportParameterDTO, reportId);
			item.setSqlResultDTO(previewStandardReport.execute(connection));
			break;
		}
		case "GRAPH": {
			PreviewGraphReport previewGraphReport = new PreviewGraphReport(this.httpServletRequest,
					tableReportParameterDTO, reportId);
			item.setSqlResultDTO(previewGraphReport.execute(connection));
			break;
		}
		case "SQL": {
			tableReportParameterDTO.setPageNumber(0);
			tableReportParameterDTO.setPageSize(20);
			PreviewSqlReport previewSqlReport = new PreviewSqlReport(this.httpServletRequest, tableReportParameterDTO,
					reportId);
			item.setSqlResultDTO(previewSqlReport.execute(connection));
			break;
		}
		case "KPI": {
			PreviewKpiReport previewKpiReport = new PreviewKpiReport(this.httpServletRequest, tableReportParameterDTO,
					reportId);
			item.setSqlResultDTO(previewKpiReport.execute(connection));
			break;
		}
		case "JASPER": {
			PreviewJasperReport previewJasperReport = new PreviewJasperReport(reportId, tableReportParameterDTO,
					httpServletRequest);
			item.setJasperBase64(commonService.responseToBase64(previewJasperReport.execute(connection)));
			break;
		}
		default: {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "notImplemented", typeOfReport);
		}
		}

	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	class usedItem {
		private Integer row;
		private Integer column;
	}

	private void checkDefault(DashboardResultDTO dashboardResultDTO, Connection connection) {
		TableParameterDTO parameterDTO = new TableParameterDTO();
		parameterDTO.getTableFilters().add(new TableFilter("appUser", SearchOperation.equals,
				String.valueOf(commonService.getAppUser().getId()), null));
		parameterDTO.getTableFilters()
				.add(new TableFilter("dashboard", SearchOperation.equals, String.valueOf(this.dashboardId), null));

		if (this.datatableService.findAll(parameterDTO, DefaultDashboard.class, connection).isEmpty()) {
			dashboardResultDTO.setSetDefault(true);
		} else {
			dashboardResultDTO.setSetDefault(false);
		}
	}

}
