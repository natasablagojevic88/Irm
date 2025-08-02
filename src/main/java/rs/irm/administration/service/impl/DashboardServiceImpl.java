package rs.irm.administration.service.impl;

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
import rs.irm.administration.dto.ReportDTO;
import rs.irm.administration.dto.RoleDTO;
import rs.irm.administration.entity.DashboardRole;
import rs.irm.administration.service.DashboardService;
import rs.irm.administration.utils.DashboardDelete;
import rs.irm.administration.utils.DashboardRoleUpdate;
import rs.irm.administration.utils.DashboardUpdate;
import rs.irm.administration.utils.UpdateDashboardReports;
import rs.irm.common.dto.ComboboxDTO;
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
		DashboardUpdate dashboardUpdate=new DashboardUpdate(dashboardDTO,this.httpServletRequest);
		return this.datatableService.executeMethodWithReturn(dashboardUpdate);
	}

	@Override
	public void getDelete(Long id) {
		DashboardDelete dashboardDelete=new DashboardDelete(id,this.httpServletRequest);
		this.datatableService.executeMethod(dashboardDelete);

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
		DashboardRoleUpdate dashboardRoleUpdate=new DashboardRoleUpdate(dashboardId, dashboardRoleDTO, httpServletRequest);
		this.datatableService.executeMethod(dashboardRoleUpdate);
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
