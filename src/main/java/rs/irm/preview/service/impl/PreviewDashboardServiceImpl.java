package rs.irm.preview.service.impl;

import java.net.HttpURLConnection;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import rs.irm.administration.dto.DashboardRoleInfoDTO;
import rs.irm.administration.entity.Dashboard;
import rs.irm.administration.entity.DefaultDashboard;
import rs.irm.administration.utils.ModelData;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.service.CommonService;
import rs.irm.common.utils.CheckAdmin;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.utils.TableFilter;
import rs.irm.preview.dto.DashboardResultDTO;
import rs.irm.preview.service.PreviewDashboardService;
import rs.irm.preview.utils.CreateDashboardData;

@Named
public class PreviewDashboardServiceImpl implements PreviewDashboardService {

	@Inject
	private CommonService commonService;

	@Inject
	private DatatableService datatableService;

	@Context
	private HttpServletRequest httpServletRequest;

	@Override
	public DashboardResultDTO getDashboardData(Long id) {
		checkRight(id);
		CreateDashboardData createDashboardData = new CreateDashboardData(httpServletRequest, id);
		return this.datatableService.executeMethodWithReturn(createDashboardData);
	}

	private void checkRight(Long id) {
		boolean isAdmin = this.commonService.getRoles().contains(CheckAdmin.roleAdmin);

		if (isAdmin) {
			return;
		}

		List<DashboardRoleInfoDTO> dashoboardInfos = ModelData.dashboardRoleDtos.stream()
				.filter(a -> a.getDashboardId().doubleValue() == id.doubleValue()).toList();

		boolean hasRight = false;

		for (DashboardRoleInfoDTO dashboardRoleInfoDTO : dashoboardInfos) {
			if (commonService.getRoles().contains(dashboardRoleInfoDTO.getRoleCode())) {
				hasRight = true;
				break;
			}
		}

		if (!hasRight) {
			throw new CommonException(HttpURLConnection.HTTP_FORBIDDEN, "noRight", null);
		}
	}

	@Override
	public void setDefaultDashboard(Long dashboardid) {
		checkRight(dashboardid);

		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters().add(new TableFilter("appUser", SearchOperation.equals,
				String.valueOf(this.commonService.getAppUser().getId()), null));
		DefaultDashboard defaultDashboard = this.datatableService.findAll(tableParameterDTO, DefaultDashboard.class)
				.stream().findFirst().orElse(null);

		if (defaultDashboard == null) {
			DefaultDashboard newDefaultDashboard = new DefaultDashboard();
			newDefaultDashboard.setId(0L);
			newDefaultDashboard.setAppUser(this.commonService.getAppUser());
			newDefaultDashboard.setDashboard(new Dashboard(dashboardid));
			this.datatableService.save(newDefaultDashboard);
		} else {
			if (defaultDashboard.getDashboard().getId().doubleValue() == dashboardid.doubleValue()) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "dashboardAlreadyDefault", null);
			}

			defaultDashboard.setDashboard(new Dashboard(dashboardid));
			this.datatableService.save(defaultDashboard);
		}

	}

	@Override
	public void removeDefaultDashboard(Long dashboardid) {
		checkRight(dashboardid);

		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters().add(new TableFilter("appUser", SearchOperation.equals,
				String.valueOf(this.commonService.getAppUser().getId()), null));
		DefaultDashboard defaultDashboard = this.datatableService.findAll(tableParameterDTO, DefaultDashboard.class)
				.stream().findFirst().orElse(null);

		if (defaultDashboard == null) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "dashboardIsNotDefault", null);
		} else {
			if (defaultDashboard.getDashboard().getId().doubleValue() != dashboardid.doubleValue()) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "dashboardIsNotDefault", null);
			}

			this.datatableService.delete(defaultDashboard);
		}
		
	}

}
