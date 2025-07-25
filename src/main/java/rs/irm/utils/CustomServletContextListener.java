package rs.irm.utils;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import rs.irm.common.service.AppInitService;
import rs.irm.common.service.impl.AppInitServiceImpl;

public class CustomServletContextListener implements ServletContextListener {
	Logger logger = LogManager.getLogger(CustomServletContextListener.class);

	private AppInitService appInitService = new AppInitServiceImpl();

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		appInitService.initParameters();
		appInitService.initConnections();
		appInitService.createTables();
		appInitService.checkNotificationListener();
		appInitService.loadIcons();
		appInitService.loadModel();
		appInitService.checkAdmin();
		appInitService.initQuart();
		appInitService.initJasperReports();
		new NotificationSocket().sendMessageToUser();

		AppInitServiceImpl.contextPath = sce.getServletContext().getContextPath();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		try {
			Enumeration<Driver> drivers = DriverManager.getDrivers();
			while (drivers.hasMoreElements()) {
				DriverManager.deregisterDriver(drivers.nextElement());
			}
		} catch (Exception e) {
			logger.error(e);
		}
		appInitService.closeConnections();

		try {
			if (AppInitServiceImpl.scheduler != null) {
				AppInitServiceImpl.scheduler.shutdown(true);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		NotificationSocket.senderPool.shutdown();
		NotificationSocket.sessionQueues.clear();
	}
}
