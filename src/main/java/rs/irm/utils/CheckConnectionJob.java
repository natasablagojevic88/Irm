package rs.irm.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.PGConnection;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import rs.irm.common.service.impl.AppInitServiceImpl;

public class CheckConnectionJob implements Job {
	Logger logger = LogManager.getLogger(CheckConnectionJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		try {

			try {
				Statement statement = AppConnections.checkConnection.createStatement();
				ResultSet resultSet=statement.executeQuery(AppParameters.checkconnection);
				resultSet.close();
				statement.close();
				AppConnections.checkConnection.commit();
			} catch (Exception e) {
				Context initContext = new InitialContext();
				Context envContext = (Context) initContext.lookup("java:/comp/env");
				DataSource dataSource = (DataSource) envContext.lookup("jdbc/postgres");
				AppConnections.checkConnection = dataSource.getConnection();
				AppConnections.checkConnection.setAutoCommit(false);
			}

			for (Connection conn : new ArrayList<>(AppConnections.allConnections)) {
				try {
					PGConnection pgConnection = conn.unwrap(PGConnection.class);

					String checkQuery = "select * FROM pg_stat_activity where pid=" + pgConnection.getBackendPID();
					Statement st = AppConnections.checkConnection.createStatement();
					ResultSet resultSet = st.executeQuery(checkQuery);

					if (!resultSet.next()) {

						resultSet.close();
						st.close();
						throw new Exception("");
					}

					resultSet.close();
					st.close();

				} catch (Exception e) {
					AppConnections.allConnections.remove(conn);

					if (AppConnections.freeConnections.contains(conn)) {
						AppConnections.freeConnections.remove(conn);
					}
				}
			}
			for (Connection conn : new ArrayList<>(AppConnections.freeConnections)) {

				if (!AppConnections.allConnections.contains(conn)) {
					AppConnections.freeConnections.remove(conn);
				}
			}

			new AppInitServiceImpl().initConnections();

			AppConnections.checkConnection.commit();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

}
