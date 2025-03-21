package rs.irm.administration.service;

import java.util.List;

import rs.irm.administration.dto.MenuDTO;
import rs.irm.preview.dto.DashboardResultDTO;

public interface MenuService {
	
	List<MenuDTO> getMenu();
	
	DashboardResultDTO getDefaultDashboard();

}
