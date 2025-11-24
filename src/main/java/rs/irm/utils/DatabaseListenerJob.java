package rs.irm.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Statement;
import java.time.LocalDateTime;
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

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.DashboardDTO;
import rs.irm.administration.dto.DashboardRoleInfoDTO;
import rs.irm.administration.dto.JavaClassDTO;
import rs.irm.administration.dto.ModelColumnDTO;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.administration.dto.ModelJasperReportDTO;
import rs.irm.administration.dto.ModelJavaClassInfo;
import rs.irm.administration.dto.ModelProcedureDTO;
import rs.irm.administration.dto.ReportDTO;
import rs.irm.administration.dto.ReportGroupDTO;
import rs.irm.administration.dto.ReportGroupRolesDTO;
import rs.irm.administration.dto.ReportJobDTO;
import rs.irm.administration.entity.AppUser;
import rs.irm.administration.entity.AppUserRole;
import rs.irm.administration.entity.ModelJasperReport;
import rs.irm.administration.entity.ReportJasper;
import rs.irm.administration.entity.Role;
import rs.irm.administration.utils.ModelData;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.impl.CommonServiceImpl;
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
	public final static String appuser_listener = "appuser_listen";
	public final static String role_listener = "role_listen";
	public final static String appuserrole_listener = "appuserrole_listen";
	public final static String reportjasper_listener = "reportjasper_listen";
	public final static String javaclass_listen = "javaclass_listen";
	public final static String modeljavaclass_listen = "modeljavaclass_listen";

	ExecutorService websocketSender = Executors.newFixedThreadPool(10);

	DatatableService datatableService = new DatatableServiceImpl();
	CommonService commonService = new CommonServiceImpl();

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
				statement.execute("LISTEN " + appuser_listener);
				statement.execute("LISTEN " + role_listener);
				statement.execute("LISTEN " + appuserrole_listener);
				statement.execute("LISTEN " + reportjasper_listener);
				statement.execute("LISTEN " + javaclass_listen);
				statement.execute("LISTEN " + modeljavaclass_listen);
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
						JSONObject jsonObject = (JSONObject) new JSONParser().parse(notification.getParameter());
						Long id = ((Number) jsonObject.get("id")).longValue();

						if (jsonObject.get("action").equals("INSERT") || jsonObject.get("action").equals("UPDATE")) {
							try {
								ModelJasperReport jasperReport = this.datatableService.findByExistingId(id,
										ModelJasperReport.class);

								File jasperFolder = new File(AppParameters.jasperfilepath);

								if (!(jasperFolder.exists() && jasperFolder.isDirectory())) {
									jasperFolder.mkdirs();
								}
								try {
									Files.copy(new ByteArrayInputStream(jasperReport.getBytes()),
											Paths.get(jasperFolder.getAbsolutePath() + "/"
													+ jasperReport.getJasperFileName()),
											StandardCopyOption.REPLACE_EXISTING);
								} catch (IOException e) {
									throw new WebApplicationException(e);
								}
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
							}
						}

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
					case javaclass_listen: {
						ModelData.javaClasses = this.datatableService.findAll(new TableParameterDTO(),
								JavaClassDTO.class);
						ModelData.modelJavaClassInfo = this.datatableService.findAll(new TableParameterDTO(),
								ModelJavaClassInfo.class);
						break;
					}
					case modeljavaclass_listen: {
						ModelData.modelJavaClassInfo = this.datatableService.findAll(new TableParameterDTO(),
								ModelJavaClassInfo.class);
						break;
					}
					case appuser_listener: {
						JSONObject jsonObject = (JSONObject) new JSONParser().parse(notification.getParameter());
						Long id = ((Number) jsonObject.get("id")).longValue();
						if (jsonObject.get("action").equals("DELETE")) {
							AppUser appUser = ModelData.appUsers.stream()
									.filter(a -> a.getId().doubleValue() == id.doubleValue()).findFirst().orElse(null);
							if (appUser != null) {
								ModelData.appUsers.remove(appUser);
							}
						} else {
							AppUser appUser = jsonToObject(jsonObject, AppUser.class);

							if (ModelData.appUsers.stream().filter(a -> a.getId().doubleValue() == id.doubleValue())
									.toList().isEmpty()) {
								ModelData.appUsers.add(appUser);
							} else {
								AppUser appUserCurrent = ModelData.appUsers.stream()
										.filter(a -> a.getId().doubleValue() == id.doubleValue()).toList().get(0);
								int index = ModelData.appUsers.indexOf(appUserCurrent);
								ModelData.appUsers.set(index, appUser);

							}
						}
						break;
					}
					case role_listener: {
						JSONObject jsonObject = (JSONObject) new JSONParser().parse(notification.getParameter());
						Long id = ((Number) jsonObject.get("id")).longValue();
						if (jsonObject.get("action").equals("DELETE")) {
							Role role = ModelData.roles.stream()
									.filter(a -> a.getId().doubleValue() == id.doubleValue()).findFirst().orElse(null);
							if (role != null) {
								ModelData.roles.remove(role);
							}
						} else {
							Role role = jsonToObject(jsonObject, Role.class);

							if (ModelData.roles.stream().filter(a -> a.getId().doubleValue() == id.doubleValue())
									.toList().isEmpty()) {
								ModelData.roles.add(role);
							} else {
								Role roleCurrent = ModelData.roles.stream()
										.filter(a -> a.getId().doubleValue() == id.doubleValue()).toList().get(0);
								int index = ModelData.roles.indexOf(roleCurrent);
								ModelData.roles.set(index, role);

							}
						}
						break;
					}
					case appuserrole_listener: {
						JSONObject jsonObject = (JSONObject) new JSONParser().parse(notification.getParameter());
						Long id = ((Number) jsonObject.get("id")).longValue();
						if (jsonObject.get("action").equals("DELETE")) {
							AppUserRole appUserRole = ModelData.appUserRoles.stream()
									.filter(a -> a.getId().doubleValue() == id.doubleValue()).findFirst().orElse(null);
							if (appUserRole != null) {
								ModelData.appUserRoles.remove(appUserRole);
							}
						} else {
							AppUserRole appUserRole = jsonToObject(jsonObject, AppUserRole.class);

							if (ModelData.appUserRoles.stream().filter(a -> a.getId().doubleValue() == id.doubleValue())
									.toList().isEmpty()) {
								ModelData.appUserRoles.add(appUserRole);
							} else {
								AppUserRole appUserRoleCurrent = ModelData.appUserRoles.stream()
										.filter(a -> a.getId().doubleValue() == id.doubleValue()).toList().get(0);
								int index = ModelData.appUserRoles.indexOf(appUserRoleCurrent);
								ModelData.appUserRoles.set(index, appUserRole);

							}
						}
						break;
					}
					case reportjasper_listener: {
						JSONObject jsonObject = (JSONObject) new JSONParser().parse(notification.getParameter());
						Long id = ((Number) jsonObject.get("id")).longValue();

						if (jsonObject.get("action").equals("UPDATE") || jsonObject.get("action").equals("INSERT")) {
							ReportJasper reportJasper = this.datatableService.findByExistingId(id, ReportJasper.class);
							File jasperPath = new File(AppParameters.jasperreportpath);
							if (!(jasperPath.isDirectory() && jasperPath.exists())) {
								jasperPath.mkdirs();
							}

							File createdJasperFile = new File(
									jasperPath.getAbsolutePath() + "/" + reportJasper.getName());
							try {
								Files.copy(new ByteArrayInputStream(reportJasper.getBytes()),
										Paths.get(createdJasperFile.getAbsolutePath()),
										StandardCopyOption.REPLACE_EXISTING);
							} catch (IOException e) {
								throw new WebApplicationException(e);
							}
						}
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

	private <C> C jsonToObject(JSONObject jsonObject, Class<C> inClass) {
		try {
			C object = inClass.getConstructor().newInstance();

			for (Field field : inClass.getDeclaredFields()) {
				field.setAccessible(true);
				String fieldName = field.getName();
				String fieldType = field.getType().getSimpleName();

				if (!(field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(Column.class)
						|| field.isAnnotationPresent(JoinColumn.class))) {
					continue;
				}

				String columnName = fieldName;

				if (field.isAnnotationPresent(Column.class)) {
					Column column = field.getAnnotation(Column.class);
					if (this.commonService.hasText(column.name())) {
						columnName = column.name();
					}
				}

				if (field.isAnnotationPresent(JoinColumn.class)) {
					JoinColumn column = field.getAnnotation(JoinColumn.class);
					if (this.commonService.hasText(column.name())) {
						columnName = column.name();
					}
				}

				if (jsonObject.get(columnName) == null) {
					field.set(object, null);
					continue;
				}

				Object value = jsonObject.get(columnName);

				switch (fieldType) {
				case "String": {
					field.set(object, value.toString());
					break;
				}
				case "Long": {
					Number number = (Number) value;
					field.set(object, number.longValue());
					break;
				}
				case "LocalDateTime": {
					field.set(object, LocalDateTime.parse(value.toString()));
					break;
				}
				case "Boolean": {
					field.set(object, Boolean.valueOf(value.toString()));
					break;
				}
				default: {
					Number number = (Number) value;
					Object subObject = field.getType().getConstructor().newInstance();
					Field idField = field.getType().getDeclaredField("id");
					idField.setAccessible(true);
					idField.set(subObject, number.longValue());
					field.set(object, subObject);

				}
				}

			}

			return object;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

}
