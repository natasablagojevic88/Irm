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

import rs.irm.administration.dto.DashboardDTO;
import rs.irm.administration.dto.DashboardRoleInfoDTO;
import rs.irm.administration.dto.ModelColumnDTO;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.administration.dto.ModelJasperReportDTO;
import rs.irm.administration.dto.ModelProcedureDTO;
import rs.irm.administration.dto.ReportDTO;
import rs.irm.administration.dto.ReportGroupDTO;
import rs.irm.administration.dto.ReportGroupRolesDTO;
import rs.irm.administration.dto.ReportJobDTO;
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
	public final static String reportgroup_listener = "reportgroup_listener";
	public final static String reportgrouprole_listener = "reportgrouprole_listener";
	public final static String report_listener = "report_listener";
	public final static String dashboard_listener = "dashboard_listener";
	public final static String dashboardrole_listener = "dashboardrole_listener";
	public final static String reportjob_listener = "reportjob_listener";
	public final static String modelprocedure_listener = "modelprocedure_listener";
	ExecutorService websocketSender = Executors.newFixedThreadPool(10);

	DatatableService datatableService = new DatatableServiceImpl();

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
				statement.execute("LISTEN " + reportgroup_listener);
				statement.execute("LISTEN " + reportgrouprole_listener);
				statement.execute("LISTEN " + report_listener);
				statement.execute("LISTEN " + dashboard_listener);
				statement.execute("LISTEN " + dashboardrole_listener);
				statement.execute("LISTEN " + reportjob_listener);
				statement.execute("LISTEN " + modelprocedure_listener);
				statement.close();
			}
			PGConnection pgConnection = AppConnections.datatabeListener.unwrap(PGConnection.class);

			PGNotification[] notifications = pgConnection.getNotifications();
			if (notifications != null) {
				for (PGNotification notification : notifications) {
					String listener = notification.getName();

					switch (listener) {
					case notificationListener: {
						JSONObject jsonObject = (JSONObject) new JSONParser().parse(notification.getParameter());

						Long userid = ((Number) jsonObject.get("userid")).longValue();
						Long count = ((Number) jsonObject.get("count")).longValue();

						websocketSender.submit(() -> {
							new NotificationSocket().sendMessage(userid, count);
						});
						break;
					}
					case model_listener: {
						ModelData.listModelDTOs = this.datatableService.findAll(new TableParameterDTO(),
								ModelDTO.class);
						break;
					}
					case modelcolumn_listener: {
						ModelData.listColumnDTOs = this.datatableService.findAll(new TableParameterDTO(),
								ModelColumnDTO.class);
						break;
					}
					case modeljasperreport_listener: {
						ModelData.listModelJasperReportDTOs = this.datatableService.findAll(new TableParameterDTO(),
								ModelJasperReportDTO.class);
						break;
					}
					case reportgroup_listener: {
						ModelData.listReportGroupDTOs = this.datatableService.findAll(new TableParameterDTO(),
								ReportGroupDTO.class);
						break;
					}
					case reportgrouprole_listener: {
						ModelData.listReportGroupRolesDTOs = this.datatableService.findAll(new TableParameterDTO(),
								ReportGroupRolesDTO.class);
						break;
					}
					case report_listener: {
						ModelData.listReportDTOs = this.datatableService.findAll(new TableParameterDTO(),
								ReportDTO.class);
						break;
					}
					case dashboard_listener: {
						ModelData.listDashboardDTOs = this.datatableService.findAll(new TableParameterDTO(),
								DashboardDTO.class);
						break;
					}
					case dashboardrole_listener: {
						ModelData.dashboardRoleDtos = this.datatableService.findAll(new TableParameterDTO(),
								DashboardRoleInfoDTO.class);
						break;
					}
					case reportjob_listener: {
						ModelData.listReportJobDTOs = this.datatableService.findAll(new TableParameterDTO(),
								ReportJobDTO.class);
						break;
					}
					case modelprocedure_listener: {
						ModelData.modelProcedureDTOs = this.datatableService.findAll(new TableParameterDTO(),
								ModelProcedureDTO.class);
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
