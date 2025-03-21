package rs.irm.common.service;

public interface AppInitService {

	void initParameters();
	
	void initConnections();
	
	void closeConnections();
	
	void createTables();
	
	void generateKeys();
	
	void loadIcons();
	
	void loadModel();
	
	void checkAdmin();
	
	void initQuart();
}
