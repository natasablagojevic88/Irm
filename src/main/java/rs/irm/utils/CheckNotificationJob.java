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

public class CheckNotificationJob implements Job {
	Logger logger = LogManager.getLogger(CheckConnectionJob.class);

	final String listener = "notification_listen";
	ExecutorService websocketSender = Executors.newFixedThreadPool(10);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			try {
				Statement statement = AppConnections.checkNotification.createStatement();
				statement.execute(AppParameters.checkconnection);
				statement.close();
			} catch (Exception e) {
				Context initContext = new InitialContext();
				Context envContext = (Context) initContext.lookup("java:/comp/env");
				DataSource dataSource = (DataSource) envContext.lookup("jdbc/postgres");
				AppConnections.checkNotification = dataSource.getConnection();

				Statement statement = AppConnections.checkNotification.createStatement();
				statement.execute("LISTEN " + listener);
				statement.close();
			}
			PGConnection pgConnection = AppConnections.checkNotification.unwrap(PGConnection.class);

			PGNotification[] notifications = pgConnection.getNotifications();
			if (notifications != null) {
				for (PGNotification notification : notifications) {
					JSONObject jsonObject = (JSONObject) new JSONParser().parse(notification.getParameter());
					
					Long userid=((Number) jsonObject.get("userid")).longValue();
					Long count=((Number) jsonObject.get("count")).longValue();

					websocketSender.submit(()->{
						new NotificationSocket().sendMessage(userid, count);
					});
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
