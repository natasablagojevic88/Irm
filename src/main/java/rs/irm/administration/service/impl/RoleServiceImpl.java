package rs.irm.administration.service.impl;

import java.util.Comparator;
import java.util.List;

import org.modelmapper.ModelMapper;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import rs.irm.administration.dto.RoleAppUserDTO;
import rs.irm.administration.dto.RoleDTO;
import rs.irm.administration.entity.AppUser;
import rs.irm.administration.entity.AppUserRole;
import rs.irm.administration.entity.Role;
import rs.irm.administration.service.RoleService;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.utils.TableFilter;

@Named
public class RoleServiceImpl implements RoleService {
	@Inject
	private DatatableService datatableService;

	private ModelMapper modelMapper = new ModelMapper();

	@Override
	public TableDataDTO<RoleDTO> getTable(TableParameterDTO tableParameterDTO) {

		return datatableService.getTableDataDTO(tableParameterDTO, RoleDTO.class);
	}

	@Override
	public RoleDTO getUpdate(RoleDTO roleDTO) {

		Role role = roleDTO.getId() == 0 ? new Role() : datatableService.findByExistingId(roleDTO.getId(), Role.class);

		modelMapper.map(roleDTO, role);

		role = datatableService.save(role);

		return modelMapper.map(role, RoleDTO.class);
	}

	@Override
	public void getDelete(Long id) {

		Role role = datatableService.findByExistingId(id, Role.class);

		datatableService.delete(role);

	}

	@Override
	public List<RoleAppUserDTO> getUsersForRole(Long roleId) {

		List<AppUser> list = this.datatableService.findAll(new TableParameterDTO(), AppUser.class);
		List<RoleAppUserDTO> listDTO = list.stream().map(a -> modelMapper.map(a, RoleAppUserDTO.class))
				.sorted(Comparator.comparing(RoleAppUserDTO::getUsername)).toList();

		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		TableFilter tableFilter = new TableFilter();
		tableFilter.setField("role");
		tableFilter.setParameter1(String.valueOf(roleId));
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableParameterDTO.getTableFilters().add(tableFilter);

		List<AppUserRole> usedUsers = this.datatableService.findAll(tableParameterDTO, AppUserRole.class);
		List<Long> usedUsersId = usedUsers.stream().map(a -> a.getAppUser().getId()).toList();

		for (RoleAppUserDTO roleAppUserDTO : listDTO.stream().filter(a -> usedUsersId.contains(a.getId())).toList()) {
			roleAppUserDTO.setHasRight(true);
		}

		return listDTO;
	}

}
