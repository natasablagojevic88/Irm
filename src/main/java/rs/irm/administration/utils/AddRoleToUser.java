package rs.irm.administration.utils;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Table;
import jakarta.servlet.http.HttpServletRequest;
import rs.irm.administration.dto.AppUserRoleDTO;
import rs.irm.administration.entity.AppUser;
import rs.irm.administration.entity.AppUserRole;
import rs.irm.administration.entity.Role;
import rs.irm.common.entity.Track;
import rs.irm.common.enums.TrackAction;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.impl.CommonServiceImpl;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethod;
import rs.irm.database.utils.TableFilter;

public class AddRoleToUser implements ExecuteMethod {

	private HttpServletRequest request;
	private Long userId;
	private AppUserRoleDTO appUserRoleDTO;
	private DatatableService datatableService;
	private CommonService commonService;

	public AddRoleToUser(HttpServletRequest request, Long userId, AppUserRoleDTO appUserRoleDTO) {
		this.request = request;
		this.userId = userId;
		this.appUserRoleDTO = appUserRoleDTO;
		this.datatableService = new DatatableServiceImpl(this.request);
		this.commonService = new CommonServiceImpl(this.request);
	}

	@Override
	public void execute(Connection connection) {
		TableParameterDTO tableParameterDTO = new TableParameterDTO();

		TableFilter tableFilter = new TableFilter();
		tableFilter.setField("appUser");
		tableFilter.setParameter1(String.valueOf(this.userId));
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableParameterDTO.getTableFilters().add(tableFilter);

		tableFilter = new TableFilter();
		tableFilter.setField("role");
		tableFilter.setParameter1(String.valueOf(appUserRoleDTO.getId()));
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableParameterDTO.getTableFilters().add(tableFilter);

		List<AppUserRole> list = datatableService.findAll(tableParameterDTO, AppUserRole.class, connection);

		if (appUserRoleDTO.getHasRight()) {

			if (!list.isEmpty()) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "userAlreadyHasRight",
						appUserRoleDTO.getCode());
			}

			AppUserRole appUserRole = new AppUserRole();
			appUserRole.setId(Long.valueOf(0));
			appUserRole.setAppUser(new AppUser(userId));
			appUserRole.setRole(new Role(appUserRoleDTO.getId()));
			datatableService.save(appUserRole, connection);

		} else {
			if (list.isEmpty()) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "userDontHasRight",
						appUserRoleDTO.getCode());
			}

			datatableService.delete(list.get(0), connection);
		}

		Track track = new Track();
		track.setAction(TrackAction.UPDATE);
		track.setAddress(commonService.getIpAddress());
		track.setAppUser(commonService.getAppUser());
		track.setDataid(userId);
		track.setId(Long.valueOf(0));
		track.setTableName(AppUser.class.getAnnotation(Table.class).name());
		track.setTime(LocalDateTime.now());
		
		this.datatableService.save(track, connection);
		

	}

}
