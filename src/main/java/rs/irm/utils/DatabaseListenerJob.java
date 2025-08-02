package rs.irm.utils;

import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import rs.irm.administration.dto.ModelColumnDTO;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.administration.dto.ModelJasperReportDTO;
import rs.irm.administration.utils.ModelData;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;

public class DatabaseListenerJob implements Job {
	Logger logger = LogManager.getLogger(DatabaseListenerJob.class);

	final String notificationListener = "notification_listen";
	public final static String model_listener = "model_listen";
	public final static String modelcolumn_listener = "modelcolumn_listen";
	public final static String modeljasperreport_listener = "modeljasperreport_listen";
	ExecutorService websocketSender = Executors.newFixedThreadPool(10);
	
	DatatableService datatableService=new DatatableServiceImpl();

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			try {
				Statement statement = AppConnections.datatabeListener.createStatement();
				statement.execute(AppParameters.checkconnection);
				statement.close();
			} catch (Exception e) {
				Context initContext = new InitialContext();
				Context envContext = (Context) initContext.lookup("java:/comp/env");
				DataSource dataSource = (DataSource) envContext.lookup("jdbc/postgres");
				AppConnections.datatabeListener = dataSource.getConnection();

				Statement statement = AppConnections.datatabeListener.createStatement();
				statement.execute("LISTEN " + notificationListener);
				statement.execute("LISTEN " + model_listener);
				statement.execute("LISTEN " + modelcolumn_listener);
				statement.execute("LISTEN " + modeljasperreport_listener);
				statement.close();
			}
			PGConnection pgConnection = AppConnections.datatabeListener.unwrap(PGConnection.class);

			PGNotification[] notifications = pgConnection.getNotifications();
			if (notifications != null) {
				for (PGNotification notification : notifications) {
					String listener=notification.getName();
					
					switch(listener) {
					case notificationListener:{
						JSONObject jsonObject = (JSONObject) new JSONParser().parse(notification.getParameter());
						
						Long userid=((Number) jsonObject.get("userid")).longValue();
						Long count=((Number) jsonObject.get("count")).longValue();

						websocketSender.submit(()->{
							new NotificationSocket().sendMessage(userid, count);
						});
						break;
					}
					case model_listener:{
						ModelData.listModelDTOs = this.datatableService.findAll(new TableParameterDTO(), ModelDTO.class);
						break;
					}
					case modelcolumn_listener:{
						ModelData.listColumnDTOs = this.datatableService.findAll(new TableParameterDTO(), ModelColumnDTO.class);
						break;
					}
					case modeljasperreport_listener:{
						ModelData.listModelJasperReportDTOs = this.datatableService.findAll(new TableParameterDTO(),
								ModelJasperReportDTO.class);
						break;
					}
					}
					
				}
			}
		} catch (Exception e) {
			Exception lastException = e;
			while (lastException.getCause() != null) {
				lastException = (Exception) lastException.getCause();
			}
			logger.error(e.getMessage(), e);

		}

	}

}
