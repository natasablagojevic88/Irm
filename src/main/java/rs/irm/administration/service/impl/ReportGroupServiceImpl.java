package rs.irm.administration.service.impl;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import rs.irm.administration.dto.ReportGroupDTO;
import rs.irm.administration.dto.ReportGroupRoleDTO;
import rs.irm.administration.dto.ReportGroupRolesDTO;
import rs.irm.administration.dto.RoleDTO;
import rs.irm.administration.entity.ReportGroup;
import rs.irm.administration.entity.ReportGroupRole;
import rs.irm.administration.entity.Role;
import rs.irm.administration.service.ReportGroupService;
import rs.irm.administration.utils.ModelData;
import rs.irm.common.exceptions.CommonException;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.utils.TableFilter;

@Named
public class ReportGroupServiceImpl implements ReportGroupService {

	@Inject
	private DatatableService datatableService;

	private ModelMapper modelMapper = new ModelMapper();

	@Override
	public TableDataDTO<ReportGroupDTO> getTable(TableParameterDTO tableParameterDTO) {

		return this.datatableService.getTableDataDTO(tableParameterDTO, ReportGroupDTO.class);
	}

	@Override
	public ReportGroupDTO getUpdate(ReportGroupDTO reportGroupDTO) {

		ReportGroup reportGroup = reportGroupDTO.getId() == 0 ? new ReportGroup()
				: this.datatableService.findByExistingId(reportGroupDTO.getId(), ReportGroup.class);

		modelMapper.map(reportGroupDTO, reportGroup);

		reportGroup = this.datatableService.save(reportGroup);
		
		ModelData.listReportGroupRolesDTOs = this.datatableService.findAll(new TableParameterDTO(),
				ReportGroupRolesDTO.class);
		ModelData.listReportGroupDTOs = this.datatableService.findAll(new TableParameterDTO(),
				ReportGroupDTO.class);

		return modelMapper.map(reportGroup, ReportGroupDTO.class);
	}

	@Override
	public void getDelete(Long id) {
		ReportGroup reportGroup = this.datatableService.findByExistingId(id, ReportGroup.class);
		this.datatableService.delete(reportGroup);
		ModelData.listReportGroupRolesDTOs = this.datatableService.findAll(new TableParameterDTO(),
				ReportGroupRolesDTO.class);
		ModelData.listReportGroupDTOs = this.datatableService.findAll(new TableParameterDTO(),
				ReportGroupDTO.class);

	}

	@Override
	public List<ReportGroupRoleDTO> getRoles(Long reportGroupId) {
		ReportGroup reportGroup = this.datatableService.findByExistingId(reportGroupId, ReportGroup.class);
		List<ReportGroupRoleDTO> list = new ArrayList<>();

		List<RoleDTO> roles = this.datatableService.findAll(new TableParameterDTO(), RoleDTO.class);

		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("reportGroup", SearchOperation.equals, String.valueOf(reportGroup.getId()), null));
		List<ReportGroupRole> rolesForGroupRoles = this.datatableService.findAll(tableParameterDTO,
				ReportGroupRole.class);
		List<Double> idsRoles = rolesForGroupRoles.stream().map(a -> a.getRole().getId().doubleValue()).toList();

		list = roles.stream().map(a -> modelMapper.map(a, ReportGroupRoleDTO.class)).toList();
		for (ReportGroupRoleDTO reportGroupRoleDTO : list.stream().filter(a -> idsRoles.contains(a.getId().doubleValue())).toList()) {
			reportGroupRoleDTO.setHasRight(true);
		}

		return list;
	}

	@Override
	public void getRoleUpdate(ReportGroupRoleDTO reportGroupRoleDTO, Long reportGroupId) {
		ReportGroup reportGroup = this.datatableService.findByExistingId(reportGroupId, ReportGroup.class);

		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("reportGroup", SearchOperation.equals, String.valueOf(reportGroup.getId()), null));
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("role", SearchOperation.equals, String.valueOf(reportGroupRoleDTO.getId()), null));

		List<ReportGroupRole> list = this.datatableService.findAll(tableParameterDTO, ReportGroupRole.class);

		if (reportGroupRoleDTO.getHasRight() && (!list.isEmpty())) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "groupAlredyHasRole", null);
		}

		if ((!reportGroupRoleDTO.getHasRight()) && list.isEmpty()) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "groupDoesntHaveRole", null);
		}

		if (reportGroupRoleDTO.getHasRight()) {

			Role role = new Role(reportGroupRoleDTO.getId());
			ReportGroupRole reportGroupRole = new ReportGroupRole();
			reportGroupRole.setId(0L);
			reportGroupRole.setReportGroup(reportGroup);
			reportGroupRole.setRole(role);

			reportGroupRole = this.datatableService.save(reportGroupRole);
		} else {
			this.datatableService.delete(list.get(0));
		}
		
		ModelData.listReportGroupRolesDTOs = this.datatableService.findAll(new TableParameterDTO(),
				ReportGroupRolesDTO.class);
		ModelData.listReportGroupDTOs = this.datatableService.findAll(new TableParameterDTO(),
				ReportGroupDTO.class);

	}

}
