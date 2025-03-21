package rs.irm.administration.utils;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.modelmapper.ModelMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.DashboardItemDTO;
import rs.irm.administration.entity.Dashboard;
import rs.irm.administration.entity.DashboardItem;
import rs.irm.common.exceptions.CommonException;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethod;

public class UpdateDashboardReports implements ExecuteMethod {

	private HttpServletRequest httpServletRequest;
	private Long dashboardId;
	private List<DashboardItemDTO> dashboardItemDTOs;
	private DatatableService datatableService;
	private ModelMapper modelMapper = new ModelMapper();

	public UpdateDashboardReports(HttpServletRequest httpServletRequest, Long dashboardId,
			List<DashboardItemDTO> dashboardItemDTOs) {
		this.httpServletRequest = httpServletRequest;
		this.dashboardId = dashboardId;
		this.dashboardItemDTOs = dashboardItemDTOs;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
	}

	@Override
	public void execute(Connection connection) {
		Dashboard dashboard = this.datatableService.findByExistingId(dashboardId, Dashboard.class, connection);
		checkRowsAndColumn(dashboard);
		String deleteItemQuery = "delete from \n" + "dashboarditem \n" + "where \n" + "dashboard=" + dashboard.getId();
		try {
			Statement statement = connection.createStatement();
			statement.execute(deleteItemQuery);
			statement.close();
		} catch (SQLException e) {
			throw new WebApplicationException(e);
		}

		for (DashboardItemDTO dashboardItemDTO : dashboardItemDTOs) {
			if (dashboardItemDTO.getReportId() == null) {
				continue;
			}
			dashboardItemDTO.setId(0L);
			dashboardItemDTO.setDashboardId(dashboardId);
			DashboardItem dashboardItem = modelMapper.map(dashboardItemDTO, DashboardItem.class);
			this.datatableService.save(dashboardItem, connection);

		}

	}

	private void checkRowsAndColumn(Dashboard dashboard) {

		for (DashboardItemDTO dashboardItemDTO : dashboardItemDTOs) {
			if (dashboardItemDTO.getColumn() < 1) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "notAllow", dashboardItemDTO.getColumn());
			}

			if (dashboardItemDTO.getRow() < 1) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "notAllow", dashboardItemDTO.getRow());
			}

			if (dashboardItemDTO.getColspan() < 1) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "notAllow", dashboardItemDTO.getRow());
			}
		}

		for (int i = 1; i <= dashboard.getRownumber(); i++) {
			final int j = i;
			List<DashboardItemDTO> dashboardItemDTOsForRow = this.dashboardItemDTOs.stream()
					.filter(a -> a.getRow().intValue() == j).sorted(Comparator.comparing(DashboardItemDTO::getColumn))
					.toList();

			List<Integer> usedColumn = new ArrayList<>();

			for (DashboardItemDTO dashboardItemDTO : dashboardItemDTOsForRow) {

				if (usedColumn.contains(dashboardItemDTO.getColumn())) {
					throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "columnNumberInUse",
							dashboardItemDTO.getColumn());
				}

				for (int k = 1; k < dashboardItemDTO.getColspan(); k++) {
					Integer columnColumn = dashboardItemDTO.getColumn() + k;
					if (usedColumn.contains(columnColumn)) {
						throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "columnNumberInUse",
								dashboardItemDTO.getColumn());
					}
				}

				if ((dashboardItemDTO.getColumn() + dashboardItemDTO.getColspan() - 1) > dashboard.getColumnnumber()) {
					throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "maximumColumnAllow",
							dashboard.getColumnnumber());
				}

				usedColumn.add(dashboardItemDTO.getColumn());
				for (int k = 1; k < dashboardItemDTO.getColspan(); k++) {
					usedColumn.add(dashboardItemDTO.getColumn() + k);
				}
			}

			if (!dashboardItemDTOs.stream().filter(a -> a.getRow().intValue() > dashboard.getRownumber().intValue())
					.toList().isEmpty()) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "maximumRowAllow",
						dashboard.getRownumber());
			}
		}
	}
}
