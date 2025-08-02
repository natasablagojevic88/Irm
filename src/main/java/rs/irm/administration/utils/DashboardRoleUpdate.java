package rs.irm.administration.utils;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import rs.irm.administration.dto.DashboardRoleDTO;
import rs.irm.administration.entity.Dashboard;
import rs.irm.administration.entity.DashboardRole;
import rs.irm.administration.entity.Role;
import rs.irm.common.exceptions.CommonException;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethod;
import rs.irm.database.utils.TableFilter;

public class DashboardRoleUpdate implements ExecuteMethod {

	private Long dashboardId;
	private DashboardRoleDTO dashboardRoleDTO;
	private HttpServletRequest httpServletRequest;
	private DatatableService datatableService;

	public DashboardRoleUpdate(Long dashboardId, DashboardRoleDTO dashboardRoleDTO,
			HttpServletRequest httpServletRequest) {
		this.dashboardId = dashboardId;
		this.dashboardRoleDTO = dashboardRoleDTO;
		this.httpServletRequest = httpServletRequest;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
	}

	@Override
	public void execute(Connection connection) {
		Dashboard dashboard = this.datatableService.findByExistingId(dashboardId, Dashboard.class, connection);

		TableParameterDTO parameterDTO = new TableParameterDTO();
		parameterDTO.getTableFilters()
				.add(new TableFilter("dashboard", SearchOperation.equals, String.valueOf(dashboard.getId()), null));
		parameterDTO.getTableFilters()
				.add(new TableFilter("role", SearchOperation.equals, String.valueOf(dashboardRoleDTO.getId()), null));

		List<DashboardRole> dashboardRoles = this.datatableService.findAll(parameterDTO, DashboardRole.class,
				connection);

		if (dashboardRoleDTO.getHasRight()) {
			if (!dashboardRoles.isEmpty()) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "dashboardAlreadyHasRole", null);
			}
			DashboardRole dashboardRole = new DashboardRole();
			dashboardRole.setId(0L);
			dashboardRole.setRole(new Role(dashboardRoleDTO.getId()));
			dashboardRole.setDashboard(dashboard);
			this.datatableService.save(dashboardRole, connection);
		} else {
			if (dashboardRoles.isEmpty()) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "dashboardHasNoRole", null);
			}

			this.datatableService.delete(dashboardRoles.get(0), connection);
		}

	}

}
