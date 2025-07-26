package rs.irm.common.service.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.ws.rs.WebApplicationException;
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
import rs.irm.administration.entity.ModelJasperReport;
import rs.irm.administration.entity.ReportJasper;
import rs.irm.administration.entity.ReportJob;
import rs.irm.administration.service.LoadReportJobService;
import rs.irm.administration.service.impl.LoadReportJobServiceImpl;
import rs.irm.administration.utils.ModelData;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.common.service.AppInitService;
import rs.irm.common.utils.CheckAdmin;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.BaseType;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.CreateColumnData;
import rs.irm.database.utils.CreateTableData;
import rs.irm.database.utils.ForeignKeyData;
import rs.irm.database.utils.IndexData;
import rs.irm.database.utils.TableData;
import rs.irm.database.utils.UniqueData;
import rs.irm.utils.AppConnections;
import rs.irm.utils.AppParameters;
import rs.irm.utils.CheckConnectionJob;
import rs.irm.utils.CheckNotificationJob;
import rs.irm.utils.RemoveInactiveTokenJob;

public class AppInitServiceImpl implements AppInitService {
	Logger logger = LogManager.getLogger(AppInitServiceImpl.class);
	public static String contextPath;

	private DatatableService datatableService = new DatatableServiceImpl();
	private LoadReportJobService loadReportJobService = new LoadReportJobServiceImpl();
	public static List<ComboboxDTO> icons = new ArrayList<>();

	@Override
	public void initParameters() {
		try {

			AppParameters appParameters = new AppParameters();
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:comp/env");

			for (Field field : Arrays.asList(AppParameters.class.getDeclaredFields())) {
				field.setAccessible(true);
				field.set(appParameters, envContext.lookup(field.getName()));
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	@Override
	public void initConnections() {
		try {
			AppConnections.allConnections = AppConnections.allConnections == null ? new ArrayList<>()
					: AppConnections.allConnections;
			AppConnections.freeConnections = AppConnections.freeConnections == null ? new ArrayList<>()
					: AppConnections.freeConnections;

			Context initContext = new InitialContext();

			for (int i = AppConnections.allConnections.size(); i < AppParameters.numofconnections; i++) {
				Context envContext = (Context) initContext.lookup("java:/comp/env");
				DataSource dataSource = (DataSource) envContext.lookup("jdbc/postgres");
				Connection connection = dataSource.getConnection();
				connection.setAutoCommit(false);
				AppConnections.allConnections.add(connection);
				AppConnections.freeConnections.add(connection);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	@Override
	public void closeConnections() {
		try {
			for (Connection connection : AppConnections.allConnections) {
				connection.close();
			}

			AppConnections.allConnections = new ArrayList<>();
			AppConnections.freeConnections = new ArrayList<>();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	@Override
	public void createTables() {
		try {

			Reflections reflections = new Reflections(this.getClass().getPackage().getName().split("\\.")[0],
					Scanners.SubTypes.filterResultsBy(a -> true));
			Set<Class<? extends Object>> listClass = reflections.getSubTypesOf(Object.class);

			List<TableData> tableDatas = new ArrayList<>();

			for (Class<? extends Object> inClass : listClass) {
				if (!inClass.isAnnotationPresent(Table.class)) {
					continue;
				}

				Class<?> tableClass = Class.forName(inClass.getTypeName());

				Table table = tableClass.getAnnotation(Table.class);
				TableData tableData = new TableData();
				tableData.setName(table.name());

				for (Field field : Arrays.asList(tableClass.getDeclaredFields())) {
					if (!(field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(JoinColumn.class)
							|| field.isAnnotationPresent(Id.class))) {
						continue;
					}

					field.setAccessible(true);
					CreateColumnData columnData = new CreateColumnData();
					columnData.setBaseType(columnMapper(field));
					columnData.setId(field.isAnnotationPresent(Id.class));
					if (columnData.getId()) {
						columnData.setBaseType(BaseType.bigserial);
						columnData.setName(field.getName());
						columnData.setLength(255);
						columnData.setNullable(false);
						columnData.setPrecision(0);

					}

					if (field.isAnnotationPresent(Column.class)) {
						Column column = field.getAnnotation(Column.class);
						columnData.setName(column.name().length() == 0 ? field.getName() : column.name());
						columnData.setLength(column.length());
						columnData.setNullable(column.nullable());
						columnData.setPrecision(column.precision());
						if (column.precision() == 0 && field.getType().getSimpleName().equals("BigDecimal")) {
							columnData.setPrecision(2);
						}
					}

					if (field.isAnnotationPresent(JoinColumn.class)) {
						JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
						columnData.setBaseType(BaseType.int8);
						columnData.setName(joinColumn.name().length() == 0 ? field.getName() : joinColumn.name());
						columnData.setLength(255);
						columnData.setNullable(joinColumn.nullable());
						columnData.setPrecision(0);

						ForeignKeyData foreignKeyData = new ForeignKeyData();
						foreignKeyData.setName(joinColumn.foreignKey().name());
						foreignKeyData.setColumn(joinColumn.name().length() == 0 ? field.getName() : joinColumn.name());
						foreignKeyData.setTable(field.getType().getAnnotation(Table.class).name());
						foreignKeyData.setCascade(false);
						if (field.isAnnotationPresent(ManyToOne.class)) {
							ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
							if (manyToOne.cascade().length != 0) {
								CascadeType cascadeType = manyToOne.cascade()[0];
								if (cascadeType.equals(CascadeType.REMOVE)) {
									foreignKeyData.setCascade(true);
								}
							}
						}

						tableData.getForeignKeyDatas().add(foreignKeyData);
					}

					tableData.getColumnDataList().add(columnData);
				}

				for (UniqueConstraint uniqueConstraint : Arrays.asList(table.uniqueConstraints())) {
					UniqueData uniqeData = new UniqueData();
					uniqeData.setName(uniqueConstraint.name());
					uniqeData.setColumns(Arrays.asList(uniqueConstraint.columnNames()));
					tableData.getUniqueDatas().add(uniqeData);
				}

				for (Index indexConstraint : Arrays.asList(table.indexes())) {
					IndexData indexData = new IndexData();
					indexData.setName(indexConstraint.name());
					indexData.setColumns(indexConstraint.columnList());
					tableData.getIndexDatas().add(indexData);
				}

				tableDatas.add(tableData);

			}

			CreateTableData createTableData = new CreateTableData(tableDatas);
			datatableService.executeMethod(createTableData);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	private BaseType columnMapper(Field field) {

		if (field.getType().isEnum()) {
			return BaseType.varchar;
		}

		String type = field.getType().getSimpleName();
		switch (type) {
		case "Long":
			return BaseType.numeric;
		case "String":
			return BaseType.varchar;
		case "Integer":
			return BaseType.int4;
		case "BigDecimal":
			return BaseType.numeric;
		case "Boolean":
			return BaseType.bool;
		case "LocalDate":
			return BaseType.date;
		case "LocalDateTime":
			return BaseType.timestamp;
		case "byte[]":
			return BaseType.bytea;
		default:
			return BaseType.int8;
		}
	}

	@Override
	public void loadIcons() {
		try {
			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("icons.csv");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

			String row;

			while ((row = bufferedReader.readLine()) != null) {
				String splits[] = row.split("\\|");
				ComboboxDTO comboboxDTO = new ComboboxDTO(splits[0], splits[1]);
				icons.add(comboboxDTO);
			}

			bufferedReader.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	@Override
	public void loadModel() {
		ModelData.listModelDTOs = this.datatableService.findAll(new TableParameterDTO(), ModelDTO.class);
		ModelData.listColumnDTOs = this.datatableService.findAll(new TableParameterDTO(), ModelColumnDTO.class);
		ModelData.listModelJasperReportDTOs = this.datatableService.findAll(new TableParameterDTO(),
				ModelJasperReportDTO.class);
		ModelData.listReportGroupDTOs = this.datatableService.findAll(new TableParameterDTO(), ReportGroupDTO.class);
		ModelData.listReportGroupRolesDTOs = this.datatableService.findAll(new TableParameterDTO(),
				ReportGroupRolesDTO.class);
		ModelData.listReportDTOs = this.datatableService.findAll(new TableParameterDTO(), ReportDTO.class);
		ModelData.listDashboardDTOs = this.datatableService.findAll(new TableParameterDTO(), DashboardDTO.class);
		ModelData.dashboardRoleDtos = this.datatableService.findAll(new TableParameterDTO(),
				DashboardRoleInfoDTO.class);
		ModelData.listReportJobDTOs = this.datatableService.findAll(new TableParameterDTO(), ReportJobDTO.class);
		ModelData.reportJobStates = new LinkedHashMap<>();
		ModelData.modelProcedureDTOs = this.datatableService.findAll(new TableParameterDTO(), ModelProcedureDTO.class);

	}

	@Override
	public void checkAdmin() {
		CheckAdmin checkAdmin = new CheckAdmin();
		this.datatableService.executeMethod(checkAdmin);

	}

	public static Scheduler scheduler;

	@Override
	public void initQuart() {

		try {
			
			SchedulerFactory schedulerFactory = new StdSchedulerFactory();
			scheduler = schedulerFactory.getScheduler();

			Trigger triggerCheckConnection = TriggerBuilder.newTrigger()
					.withIdentity("checkConnection", "checkConnection")
					.withSchedule(CronScheduleBuilder.cronSchedule(AppParameters.checkconnectioncron)).build();

			JobDetail jobCheckConnection = JobBuilder.newJob(CheckConnectionJob.class)
					.withIdentity("checkConnection", "checkConnection")
					.build();

			scheduler.scheduleJob(jobCheckConnection, triggerCheckConnection);
			scheduler.start();

			if(!AppParameters.loadjobs) {
				return;
			}
			triggerCheckConnection = TriggerBuilder.newTrigger()
					.withIdentity("checkToken", "checkToken")
					.withSchedule(CronScheduleBuilder.cronSchedule(AppParameters.removeinactivetokencron)).build();

			jobCheckConnection = JobBuilder.newJob(RemoveInactiveTokenJob.class)
					.withIdentity("checkToken", "checkToken")
					.build();

			scheduler.scheduleJob(jobCheckConnection, triggerCheckConnection);
			
			triggerCheckConnection = TriggerBuilder.newTrigger()
					.withIdentity("checkNotification", "checkNotification")
					.withSchedule(CronScheduleBuilder.cronSchedule(AppParameters.checknotificationcron)).build();

			jobCheckConnection = JobBuilder.newJob(CheckNotificationJob.class)
					.withIdentity("checkNotification", "checkNotification")
					.build();

			scheduler.scheduleJob(jobCheckConnection, triggerCheckConnection);
			
			List<ReportJob> reportJobs = this.datatableService.findAll(new TableParameterDTO(), ReportJob.class);

			for (ReportJob reportJob : reportJobs) {
				this.loadReportJobService.loadJob(reportJob);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	@Override
	public void initJasperReports() {

		File jasperModelFolder = new File(AppParameters.jasperfilepath);

		if (!(jasperModelFolder.exists() && jasperModelFolder.isDirectory())) {
			jasperModelFolder.mkdirs();
		}

		List<ModelJasperReport> modelJasperReports = this.datatableService.findAll(new TableParameterDTO(),
				ModelJasperReport.class);
		for (ModelJasperReport modelJasperReport : modelJasperReports) {
			File jasperFile = new File(
					jasperModelFolder.getAbsolutePath() + "/" + modelJasperReport.getJasperFileName());

			if (jasperFile.exists() && jasperFile.isFile()) {
				continue;
			}

			try {
				Files.copy(new ByteArrayInputStream(modelJasperReport.getBytes()),
						Paths.get(jasperFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new WebApplicationException(e);
			}
		}
		
		File jasperReportFolder = new File(AppParameters.jasperreportpath);

		if (!(jasperReportFolder.exists() && jasperReportFolder.isDirectory())) {
			jasperReportFolder.mkdirs();
		}

		List<ReportJasper> reportJaspers=this.datatableService.findAll(new TableParameterDTO(), ReportJasper.class);
		
		for(ReportJasper reportJasper:reportJaspers) {
			File jasperFile = new File(
					jasperReportFolder.getAbsolutePath() + "/" + reportJasper.getName());

			if (jasperFile.exists() && jasperFile.isFile()) {
				continue;
			}

			try {
				Files.copy(new ByteArrayInputStream(reportJasper.getBytes()),
						Paths.get(jasperFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new WebApplicationException(e);
			}
		}
	}

}
