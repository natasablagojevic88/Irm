package rs.irm.administration.service;

import java.util.List;

import rs.irm.administration.dto.AppUserDTO;
import rs.irm.administration.dto.AppUserInfoDTO;
import rs.irm.administration.dto.AppUserRoleDTO;
import rs.irm.administration.dto.ChangePasswordDTO;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

public interface AppUserService {

	TableDataDTO<AppUserDTO> getTable(TableParameterDTO tableParameterDTO);
	
	AppUserDTO getUpdate(AppUserDTO appUserDTO);
	
	void getDelete(Long id);
	
	List<AppUserRoleDTO> getListRoleForUser(Long userID);
	
	void getUpdateRoleForUser(AppUserRoleDTO appUserRoleDTO,Long userId);
	
	void getChangePassword(ChangePasswordDTO changePasswordDTO);
	
	AppUserInfoDTO getUserInfo();

}
