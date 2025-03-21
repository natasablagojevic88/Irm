package rs.irm.preview.service;

import rs.irm.preview.dto.DashboardResultDTO;

public interface PreviewDashboardService {

	DashboardResultDTO getDashboardData(Long id);
	
	void setDefaultDashboard(Long dashboardid);
	
	void removeDefaultDashboard(Long dashboardid);
}
