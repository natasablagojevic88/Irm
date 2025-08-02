package rs.irm.administration.utils;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import org.modelmapper.ModelMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.DashboardDTO;
import rs.irm.administration.entity.Dashboard;
import rs.irm.administration.entity.DashboardItem;
import rs.irm.common.exceptions.CommonException;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.database.utils.TableFilter;
import rs.irm.utils.DatabaseListenerJob;

public class DashboardUpdate implements ExecuteMethodWithReturn<DashboardDTO> {

	private DashboardDTO dashboardDTO;
	private HttpServletRequest httpServletRequest;
	private DatatableService datatableService;
	private ModelMapper modelMapper = new ModelMapper();

	public DashboardUpdate(DashboardDTO dashboardDTO, HttpServletRequest httpServletRequest) {
		this.dashboardDTO = dashboardDTO;
		this.httpServletRequest = httpServletRequest;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
	}

	@Override
	public DashboardDTO execute(Connection connection) {
		Dashboard dashboard = dashboardDTO.getId() == 0 ? new Dashboard()
				: this.datatableService.findByExistingId(dashboardDTO.getId(), Dashboard.class, connection);
		modelMapper.map(dashboardDTO, dashboard);

		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("dashboard", SearchOperation.equals, String.valueOf(dashboardDTO.getId()), null));
		List<DashboardItem> dashboardItems = this.datatableService.findAll(tableParameterDTO, DashboardItem.class,
				connection);

		for (DashboardItem dashboardItem : dashboardItems) {
			if ((dashboardItem.getColumn() + dashboardItem.getColspan() - 1) > dashboardDTO.getColumnnumber()) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "wrongNumberOfColumns",
						dashboardDTO.getColumnnumber());
			}

			if ((dashboardItem.getRow() + dashboardItem.getRowspan() - 1) > dashboardDTO.getRownumber()) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "wrongNumberOfRow",
						dashboardDTO.getColumnnumber());
			}
		}

		dashboard = this.datatableService.save(dashboard);
		ModelData.listDashboardDTOs = this.datatableService.findAll(new TableParameterDTO(), DashboardDTO.class,
				connection);

		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("NOTIFY " + DatabaseListenerJob.dashboard_listener + ", 'Dashboard changed';");
			statement.close();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
		return modelMapper.map(dashboard, DashboardDTO.class);

	}

}
