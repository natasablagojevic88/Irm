package rs.irm.administration.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import rs.irm.administration.dto.ReportGroupDTO;
import rs.irm.administration.dto.ReportGroupRoleDTO;
import rs.irm.administration.dto.RoleDTO;
import rs.irm.administration.entity.ReportGroup;
import rs.irm.administration.entity.ReportGroupRole;
import rs.irm.administration.service.ReportGroupService;
import rs.irm.administration.utils.ReportGroupDelete;
import rs.irm.administration.utils.ReportGroupRoleUpdate;
import rs.irm.administration.utils.ReportGroupUpdate;
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

	@Context
	private HttpServletRequest httpServletRequest;

	@Override
	public TableDataDTO<ReportGroupDTO> getTable(TableParameterDTO tableParameterDTO) {

		return this.datatableService.getTableDataDTO(tableParameterDTO, ReportGroupDTO.class);
	}

	@Override
	public ReportGroupDTO getUpdate(ReportGroupDTO reportGroupDTO) {
		ReportGroupUpdate reportGroupUpdate = new ReportGroupUpdate(reportGroupDTO, httpServletRequest);
		return this.datatableService.executeMethodWithReturn(reportGroupUpdate);

	}

	@Override
	public void getDelete(Long id) {
		ReportGroupDelete reportGroupDelete = new ReportGroupDelete(this.httpServletRequest, id);
		this.datatableService.executeMethod(reportGroupDelete);

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
		for (ReportGroupRoleDTO reportGroupRoleDTO : list.stream()
				.filter(a -> idsRoles.contains(a.getId().doubleValue())).toList()) {
			reportGroupRoleDTO.setHasRight(true);
		}

		return list;
	}

	@Override
	public void getRoleUpdate(ReportGroupRoleDTO reportGroupRoleDTO, Long reportGroupId) {
		ReportGroupRoleUpdate reportGroupUpdate = new ReportGroupRoleUpdate(reportGroupRoleDTO, reportGroupId,
				httpServletRequest);
		this.datatableService.executeMethod(reportGroupUpdate);

	}

}
