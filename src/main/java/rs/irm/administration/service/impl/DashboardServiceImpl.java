package rs.irm.administration.service.impl;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.modelmapper.ModelMapper;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import rs.irm.administration.dto.DashboardDTO;
import rs.irm.administration.dto.DashboardItemDTO;
import rs.irm.administration.dto.DashboardRoleDTO;
import rs.irm.administration.dto.DashboardRoleInfoDTO;
import rs.irm.administration.dto.ReportDTO;
import rs.irm.administration.dto.RoleDTO;
import rs.irm.administration.entity.Dashboard;
import rs.irm.administration.entity.DashboardItem;
import rs.irm.administration.entity.DashboardRole;
import rs.irm.administration.entity.Role;
import rs.irm.administration.service.DashboardService;
import rs.irm.administration.utils.ModelData;
import rs.irm.administration.utils.UpdateDashboardReports;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.common.exceptions.CommonException;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.utils.TableFilter;

@Named
public class DashboardServiceImpl implements DashboardService {

	@Inject
	private DatatableService datatableService;

	private ModelMapper modelMapper = new ModelMapper();

	@Context
	private HttpServletRequest httpServletRequest;

	@Override
	public TableDataDTO<DashboardDTO> getTable(TableParameterDTO tableParameterDTO) {

		return datatableService.getTableDataDTO(tableParameterDTO, DashboardDTO.class);
	}

	@Override
	public DashboardDTO getUpdate(DashboardDTO dashboardDTO) {
		Dashboard dashboard = dashboardDTO.getId() == 0 ? new Dashboard()
				: this.datatableService.findByExistingId(dashboardDTO.getId(), Dashboard.class);
		modelMapper.map(dashboardDTO, dashboard);

		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("dashboard", SearchOperation.equals, String.valueOf(dashboardDTO.getId()), null));
		List<DashboardItem> dashboardItems = this.datatableService.findAll(tableParameterDTO, DashboardItem.class);

		for (DashboardItem dashboardItem : dashboardItems) {
			if ((dashboardItem.getColumn() + dashboardItem.getColspan() - 1) > dashboardDTO.getColumnnumber()) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "wrongNumberOfColumns",
						dashboardDTO.getColumnnumber());
			}
			
			if ((dashboardItem.getRow()+ dashboardItem.getRowspan()- 1) > dashboardDTO.getRownumber()) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "wrongNumberOfRow",
						dashboardDTO.getColumnnumber());
			}
		}

		dashboard = this.datatableService.save(dashboard);
		ModelData.listDashboardDTOs = this.datatableService.findAll(new TableParameterDTO(), DashboardDTO.class);
		return modelMapper.map(dashboard, DashboardDTO.class);
	}

	@Override
	public void getDelete(Long id) {
		Dashboard dashboard = this.datatableService.findByExistingId(id, Dashboard.class);
		this.datatableService.delete(dashboard);
		ModelData.listDashboardDTOs = this.datatableService.findAll(new TableParameterDTO(), DashboardDTO.class);

	}

	@Override
	public List<DashboardRoleDTO> getRoles(Long dashboardId) {
		List<RoleDTO> rolesDTOs = this.datatableService.findAll(new TableParameterDTO(), RoleDTO.class);

		List<DashboardRoleDTO> dashboardRoleDTOs = rolesDTOs.stream()
				.map(a -> modelMapper.map(a, DashboardRoleDTO.class))
				.sorted(Comparator.comparing(DashboardRoleDTO::getCode)).toList();
		TableParameterDTO parameterDTO = new TableParameterDTO();
		parameterDTO.getTableFilters()
				.add(new TableFilter("dashboard", SearchOperation.equals, String.valueOf(dashboardId), null));
		List<DashboardRole> dashboardRoles = this.datatableService.findAll(parameterDTO, DashboardRole.class);

		List<Double> hasRole = dashboardRoles.stream().map(a -> a.getRole().getId().doubleValue()).toList();
		for (DashboardRoleDTO dashboardRoleDTO : dashboardRoleDTOs) {
			if (!hasRole.contains(dashboardRoleDTO.getId().doubleValue())) {
				continue;
			}
			dashboardRoleDTO.setHasRight(true);
		}
		return dashboardRoleDTOs;
	}

	@Override
	public void changeRoleToDashboard(Long dashboardId, DashboardRoleDTO dashboardRoleDTO) {
		Dashboard dashboard = this.datatableService.findByExistingId(dashboardId, Dashboard.class);

		TableParameterDTO parameterDTO = new TableParameterDTO();
		parameterDTO.getTableFilters()
				.add(new TableFilter("dashboard", SearchOperation.equals, String.valueOf(dashboard.getId()), null));
		parameterDTO.getTableFilters()
				.add(new TableFilter("role", SearchOperation.equals, String.valueOf(dashboardRoleDTO.getId()), null));

		List<DashboardRole> dashboardRoles = this.datatableService.findAll(parameterDTO, DashboardRole.class);

		if (dashboardRoleDTO.getHasRight()) {
			if (!dashboardRoles.isEmpty()) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "dashboardAlreadyHasRole", null);
			}
			DashboardRole dashboardRole = new DashboardRole();
			dashboardRole.setId(0L);
			dashboardRole.setRole(new Role(dashboardRoleDTO.getId()));
			dashboardRole.setDashboard(dashboard);
			this.datatableService.save(dashboardRole);
		} else {
			if (dashboardRoles.isEmpty()) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "dashboardHasNoRole", null);
			}

			this.datatableService.delete(dashboardRoles.get(0));
		}

		ModelData.dashboardRoleDtos = this.datatableService.findAll(new TableParameterDTO(),
				DashboardRoleInfoDTO.class);
	}

	private List<ReportDTO> findReportForDashboard(TableParameterDTO tableParameterDTO) {
		tableParameterDTO.getTableFilters().add(new TableFilter("type", SearchOperation.notEquals, "EXECUTE", null));
		List<ReportDTO> reports = this.datatableService.findAll(tableParameterDTO, ReportDTO.class);

		return reports;
	}

	@Override
	public List<ComboboxDTO> getReportsCombobox() {
		List<ReportDTO> reports = findReportForDashboard(new TableParameterDTO());
		List<ComboboxDTO> comboboxDTOs = new ArrayList<>();
		for (ReportDTO reportDTO : reports) {
			comboboxDTOs.add(new ComboboxDTO(reportDTO.getId(), reportDTO.getCode() + " - " + reportDTO.getName()));
		}
		return comboboxDTOs;
	}

	@Override
	public TableDataDTO<ReportDTO> getReportsTable(TableParameterDTO tableParameterDTO) {
		tableParameterDTO.getTableFilters().add(new TableFilter("type", SearchOperation.notEquals, "EXECUTE", null));
		return this.datatableService.getTableDataDTO(tableParameterDTO, ReportDTO.class);
	}

	@Override
	public void getUpdateReports(List<DashboardItemDTO> dashboardItemDTOs, Long dashboardId) {

		UpdateDashboardReports updateDashboardReports = new UpdateDashboardReports(httpServletRequest, dashboardId,
				dashboardItemDTOs);
		this.datatableService.executeMethod(updateDashboardReports);
	}

	@Override
	public List<DashboardItemDTO> getReportList(Long dashboardid) {
		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("dashboardId", SearchOperation.equals, String.valueOf(dashboardid), null));

		return this.datatableService.findAll(tableParameterDTO, DashboardItemDTO.class);
	}

}
