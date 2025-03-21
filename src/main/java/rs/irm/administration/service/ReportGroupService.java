package rs.irm.administration.service;

import java.util.List;

import rs.irm.administration.dto.ReportGroupDTO;
import rs.irm.administration.dto.ReportGroupRoleDTO;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

public interface ReportGroupService {
	
	TableDataDTO<ReportGroupDTO> getTable(TableParameterDTO tableParameterDTO);
	
	ReportGroupDTO getUpdate(ReportGroupDTO reportGroupDTO);
	
	void getDelete(Long id);
	
	List<ReportGroupRoleDTO> getRoles(Long reportGroupId);
	
	void getRoleUpdate(ReportGroupRoleDTO reportGroupRoleDTO,Long reportGroupId);

}
