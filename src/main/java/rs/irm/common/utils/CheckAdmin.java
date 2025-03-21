package rs.irm.common.utils;

import java.sql.Connection;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import rs.irm.administration.entity.AppUser;
import rs.irm.administration.entity.AppUserRole;
import rs.irm.administration.entity.Role;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethod;
import rs.irm.database.utils.TableFilter;

public class CheckAdmin implements ExecuteMethod {
	
	private static String adminUsername="admin";
	private static String initAdminPassword="admin";
	public static String roleAdmin="admin";

	private DatatableService datatableService = new DatatableServiceImpl();

	@Override
	public void execute(Connection connection) {
		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		TableFilter tableFilter = new TableFilter();
		tableFilter.setField("code");
		tableFilter.setParameter1(roleAdmin);
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableParameterDTO.getTableFilters().add(tableFilter);

		Role role;
		List<Role> roles = datatableService.findAll(tableParameterDTO, Role.class, connection);
		if (roles.isEmpty()) {
			role = new Role(Long.valueOf(0), roleAdmin, null);
			role = datatableService.save(role, connection);
		} else {
			role = roles.get(0);
		}

		tableParameterDTO = new TableParameterDTO();
		tableFilter = new TableFilter();
		tableFilter.setField("username");
		tableFilter.setParameter1(adminUsername);
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableParameterDTO.getTableFilters().add(tableFilter);
		AppUser appUser;
		List<AppUser> userList=datatableService.findAll(tableParameterDTO, AppUser.class, connection);
		if (userList.isEmpty()) {
			appUser = new AppUser();
			appUser.setId(Long.valueOf(0));
			appUser.setActive(true);
			appUser.setName("Administrator");
			appUser.setPassword(new BCryptPasswordEncoder().encode(initAdminPassword));
			appUser.setUsername(adminUsername);
			appUser = this.datatableService.save(appUser, connection);

			AppUserRole appUserRole = new AppUserRole();
			appUserRole.setId(Long.valueOf(0));
			appUserRole.setRole(role);
			appUserRole.setAppUser(appUser);
			this.datatableService.save(appUserRole, connection);
		}else {
			appUser=userList.get(0);
		}
		
		tableParameterDTO=new TableParameterDTO();
		tableFilter = new TableFilter();
		tableFilter.setField("appUser");
		tableFilter.setParameter1(String.valueOf(appUser.getId()));
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableParameterDTO.getTableFilters().add(tableFilter);
		
		tableFilter = new TableFilter();
		tableFilter.setField("role");
		tableFilter.setParameter1(String.valueOf(role.getId()));
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableParameterDTO.getTableFilters().add(tableFilter);
		
		if(this.datatableService.findAll(tableParameterDTO, AppUserRole.class, connection).isEmpty()) {
			AppUserRole appUserRole = new AppUserRole();
			appUserRole.setId(Long.valueOf(0));
			appUserRole.setRole(role);
			appUserRole.setAppUser(appUser);
			this.datatableService.save(appUserRole, connection);
		}

	}

}
