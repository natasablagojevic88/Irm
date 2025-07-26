package rs.irm.common.service;

public interface AppInitService {

	void initParameters();
	
	void initConnections();
	
	void closeConnections();
	
	void createTables();
	
	void checkNotificationListener();
	
	void loadIcons();
	
	void loadModel();
	
	void checkAdmin();
	
	void initQuart();
	
	void initJasperReports();
	
}
