package rs.irm.administration.service;

import java.util.List;

import rs.irm.administration.dto.RoleAppUserDTO;
import rs.irm.administration.dto.RoleDTO;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

public interface RoleService {

	TableDataDTO<RoleDTO> getTable(TableParameterDTO tableParameterDTO);
	
	RoleDTO getUpdate(RoleDTO roleDTO);
	
	void getDelete(Long id);
	
	List<RoleAppUserDTO> getUsersForRole(Long roleId);
}
