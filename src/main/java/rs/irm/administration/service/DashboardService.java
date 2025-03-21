package rs.irm.administration.service;

import java.util.List;

import rs.irm.administration.dto.DashboardDTO;
import rs.irm.administration.dto.DashboardItemDTO;
import rs.irm.administration.dto.DashboardRoleDTO;
import rs.irm.administration.dto.ReportDTO;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

public interface DashboardService {

	TableDataDTO<DashboardDTO> getTable(TableParameterDTO tableParameterDTO);
	
	DashboardDTO getUpdate(DashboardDTO dashboardDTO);
	
	void getDelete(Long id);
	
	List<DashboardRoleDTO> getRoles(Long dashboardId);
	
	void changeRoleToDashboard(Long dashboardId,DashboardRoleDTO dashboardRoleDTO);
	
	List<ComboboxDTO> getReportsCombobox();
	
	TableDataDTO<ReportDTO> getReportsTable(TableParameterDTO tableParameterDTO);
	
	void getUpdateReports(List<DashboardItemDTO> dashboardItemDTOs,Long dashboardId);
	
	List<DashboardItemDTO> getReportList(Long dashboardid);
}
