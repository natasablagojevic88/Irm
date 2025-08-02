package rs.irm.common.service;

public interface AppInitService {

	void initParameters();
	
	void initConnections();
	
	void closeConnections();
	
	void createTables();
	
	void initListener();
	
	void loadIcons();
	
	void loadModel();
	
	void checkAdmin();
	
	void initQuart();
	
	void initJasperReports();
	
}
