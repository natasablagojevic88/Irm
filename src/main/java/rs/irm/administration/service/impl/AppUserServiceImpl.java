package rs.irm.administration.service.impl;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import rs.irm.administration.dto.AppUserDTO;
import rs.irm.administration.dto.AppUserInfoDTO;
import rs.irm.administration.dto.AppUserRoleDTO;
import rs.irm.administration.dto.ChangePasswordDTO;
import rs.irm.administration.entity.AppUser;
import rs.irm.administration.entity.AppUserRole;
import rs.irm.administration.entity.Role;
import rs.irm.administration.service.AppUserService;
import rs.irm.administration.utils.AddRoleToUser;
import rs.irm.common.exceptions.FieldRequiredException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.utils.TableFilter;

@Named
public class AppUserServiceImpl implements AppUserService {

	@Inject
	private DatatableService datatableService;

	private ModelMapper modelMapper = new ModelMapper();
	
	@Inject
	private CommonService commonService;
	
	@Inject
	private ResourceBundleService resourceBundleService;

	@Override
	public TableDataDTO<AppUserDTO> getTable(TableParameterDTO tableParameterDTO) {
		TableDataDTO<AppUserDTO> tableDataDTO = datatableService.getTableDataDTO(tableParameterDTO, AppUserDTO.class);
		tableDataDTO.getList().forEach(a -> {
			a.setPassword(null);
		});
		return tableDataDTO;
	}

	@Override
	public AppUserDTO getUpdate(AppUserDTO appUserDTO) {
		if (appUserDTO.getId() == 0 && StringUtils.isEmpty(appUserDTO.getPassword())) {
			throw new FieldRequiredException("AppUserDTO.password");
		}

		AppUser appUser = appUserDTO.getId() == 0 ? new AppUser()
				: datatableService.findByExistingId(appUserDTO.getId(), AppUser.class);

		if (!StringUtils.isEmpty(appUserDTO.getPassword())) {
			appUserDTO.setPassword(new BCryptPasswordEncoder().encode(appUserDTO.getPassword()));
		} else {
			appUserDTO.setPassword(appUser.getPassword());
		}

		modelMapper.map(appUserDTO, appUser);
		appUser = datatableService.save(appUser);
		appUserDTO = modelMapper.map(appUser, AppUserDTO.class);
		appUserDTO.setPassword(null);
		return appUserDTO;
	}

	@Override
	public void getDelete(Long id) {
		AppUser appUser = datatableService.findByExistingId(id, AppUser.class);

		datatableService.delete(appUser);

	}

	@Override
	public List<AppUserRoleDTO> getListRoleForUser(Long userID) {

		AppUser appUser = this.datatableService.findByExistingId(userID, AppUser.class);

		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		TableFilter tableFilter = new TableFilter();
		tableFilter.setField("appUser");
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableFilter.setParameter1(String.valueOf(appUser.getId()));
		tableParameterDTO.getTableFilters().add(tableFilter);
		List<AppUserRole> appUserRoles = this.datatableService.findAll(tableParameterDTO, AppUserRole.class);
		List<Double> hasRoleId = appUserRoles.stream().map(a -> a.getRole().getId().doubleValue()).toList();
		List<Role> roles = datatableService.findAll(new TableParameterDTO(), Role.class);

		List<AppUserRoleDTO> appUserRoleDTOs = roles.stream().map(a -> modelMapper.map(a, AppUserRoleDTO.class))
				.sorted(Comparator.comparing(AppUserRoleDTO::getCode)).toList();

		for (AppUserRoleDTO appUserRoleDTO : appUserRoleDTOs.stream().filter(a -> hasRoleId.contains(a.getId().doubleValue()))
				.toList()) {
			appUserRoleDTO.setHasRight(true);
		}

		return appUserRoleDTOs;
	}

	@Context
	private HttpServletRequest request;

	@Override
	public void getUpdateRoleForUser(AppUserRoleDTO appUserRoleDTO, Long userId) {

		AddRoleToUser addRoleToUser = new AddRoleToUser(request, userId, appUserRoleDTO);
		datatableService.executeMethod(addRoleToUser);

	}

	@Override
	public void getChangePassword(ChangePasswordDTO changePasswordDTO) {
		String passwordCrypt=new BCryptPasswordEncoder().encode(changePasswordDTO.getPassword());
		
		AppUser appUser=this.datatableService.findByExistingId(commonService.getAppUser().getId(), AppUser.class);
		appUser.setPassword(passwordCrypt);
		this.datatableService.save(appUser);
		
	}

	@Override
	public AppUserInfoDTO getUserInfo() {
		AppUser appUser=this.datatableService.findByExistingId(commonService.getAppUser().getId(), AppUser.class);
		AppUserInfoDTO appUserInfoDTO=new AppUserInfoDTO();
		appUserInfoDTO.setName(appUser.getName());
		appUserInfoDTO.setUsername(appUser.getUsername());
		appUserInfoDTO.setEmail(appUser.getEmail());
		
		appUserInfoDTO.getNames().put("username", this.resourceBundleService.getText("AppUserDTO.username", null));
		appUserInfoDTO.getNames().put("name", this.resourceBundleService.getText("AppUserDTO.name", null));
		appUserInfoDTO.getNames().put("email", this.resourceBundleService.getText("AppUserDTO.email", null));
		
		return appUserInfoDTO;
	}

}
