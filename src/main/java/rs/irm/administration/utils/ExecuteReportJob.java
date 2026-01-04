package rs.irm.administration.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.janino.SimpleCompiler;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.JavaClassDTO;
import rs.irm.administration.dto.ReportDTO;
import rs.irm.administration.dto.ReportJobDTO;
import rs.irm.administration.dto.ReportJobSessionDTO;
import rs.irm.administration.entity.JobLog;
import rs.irm.administration.entity.ReportJob;
import rs.irm.administration.entity.SmtpServer;
import rs.irm.administration.enums.JobStatement;
import rs.irm.common.service.SendMailService;
import rs.irm.common.service.impl.SendMailServiceImpl;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;

public class ExecuteReportJob implements Job {
	private DatatableService datatableService = new DatatableServiceImpl();
	private SendMailService sendMailService = new SendMailServiceImpl();
	Logger logger = LogManager.getLogger(ExecuteReportJob.class);

	@Override
	public void execute(JobExecutionContext arguments) throws JobExecutionException {

		ReportJobDTO reportJobDTO = ModelData.listReportJobDTOs.stream()
				.filter(a -> a.getName().equals(arguments.getJobDetail().getKey().getName())).findFirst().get();
		ReportDTO reportDTO = null;

		if (reportJobDTO.getReportId() != null) {
			reportDTO = ModelData.listReportDTOs.stream()
					.filter(a -> a.getId().doubleValue() == reportJobDTO.getReportId().doubleValue()).findFirst().get();
		}

		JobLog jobLog = new JobLog();
		jobLog.setReportJob(new ReportJob(reportJobDTO.getId()));
		jobLog.setStatus(JobStatement.INPROGRESS);
		jobLog = this.datatableService.save(jobLog);

		ReportJobSessionDTO reportJobSessionDTO = new ReportJobSessionDTO();
		reportJobSessionDTO.setExecuteTime(LocalDateTime.now());
		reportJobSessionDTO.setState(JobStatement.INPROGRESS);
		ModelData.reportJobStates.put(reportJobDTO.getId(), reportJobSessionDTO);

		try {

			File file = runJob(reportJobDTO, reportDTO);

			jobLog.setEndTime(LocalDateTime.now());
			jobLog.setStatus(JobStatement.SUCCESSFUL);
			this.datatableService.save(jobLog);

			reportJobSessionDTO.setState(JobStatement.SUCCESSFUL);

			if (file != null) {
				reportJobSessionDTO.setFileName(file.getName());
				String fileType = reportJobDTO.getFileType();
				String fileTypeBase = "";
				switch (fileType) {
				case "PDF": {
					fileTypeBase = "application/pdf";
					break;
				}
				case "EXCEL": {
					fileTypeBase = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
					break;
				}
				case "CSV": {
					fileTypeBase = "text/csv";
					break;
				}
				}
				reportJobSessionDTO.setFileType(fileTypeBase);
				reportJobSessionDTO.setFileBytes(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
			}

			ModelData.reportJobStates.put(reportJobDTO.getId(), reportJobSessionDTO);
		} catch (Exception e) {
			Exception lastException = findLastException(e);
			logger.error(lastException.getMessage(), lastException);
			jobLog.setEndTime(LocalDateTime.now());
			jobLog.setError(lastException.getMessage());
			jobLog.setStatus(JobStatement.ERROR);
			this.datatableService.save(jobLog);

			reportJobSessionDTO.setState(JobStatement.ERROR);
			reportJobSessionDTO.setErrorMessage(lastException.getMessage());
			ModelData.reportJobStates.put(reportJobDTO.getId(), reportJobSessionDTO);

			if (reportJobDTO.getMailError() != null && reportJobDTO.getSmtpServerId() != null) {
				SmtpServer smtpServer = this.datatableService.findByExistingId(reportJobDTO.getSmtpServerId(),
						SmtpServer.class);
				sendMailService.sendMail(smtpServer, reportJobDTO.getMailError(), "Error - " + reportJobDTO.getName(),
						lastException.getMessage(), null);
			}

			throw new WebApplicationException(lastException);
		}

	}

	private Exception findLastException(Exception exception) {
		Exception lastException = exception;
		while (lastException.getCause() != null) {
			lastException = (Exception) lastException.getCause();
		}
		return lastException;
	}

	private File runJob(ReportJobDTO reportJobDTO, ReportDTO reportDTO) {
		String reportType = reportDTO == null ? (reportJobDTO.getJavaClassId()==null?"MODEL" :"JAVACLASS") : reportDTO.getType();

		switch (reportType) {
		case "STANDARD": {
			ExecuteStandardReportJob executeStandardReportJob = new ExecuteStandardReportJob(reportJobDTO, reportDTO);
			File file = datatableService.executeMethodWithReturn(executeStandardReportJob);
			return file;
		}
		case "SQL": {
			ExecuteSqlReportJob executeSqlReportJob = new ExecuteSqlReportJob(reportJobDTO, reportDTO);
			File file = datatableService.executeMethodWithReturn(executeSqlReportJob);
			return file;
		}
		case "JASPER": {
			ExecuteJasperReportJob executeJasperReportJob = new ExecuteJasperReportJob(reportJobDTO, reportDTO);
			File file = datatableService.executeMethodWithReturn(executeJasperReportJob);
			return file;
		}
		case "EXECUTE": {
			ExecuteExecuteReportJob executeExecuteReportJob = new ExecuteExecuteReportJob(reportDTO);
			datatableService.executeMethod(executeExecuteReportJob);
			return null;
		}
		case "MODEL": {
			ExecuteImportReportJob executeImportReportJob = new ExecuteImportReportJob(reportJobDTO);
			datatableService.executeMethod(executeImportReportJob);
			return null;
		}
		case "JAVACLASS": {
			SimpleCompiler compiler = new SimpleCompiler();
			JavaClassDTO javaClassDTO=ModelData.javaClasses.stream().filter(a->a.getId().doubleValue()==reportJobDTO.getJavaClassId().doubleValue())
					.findFirst().orElse(null);
	        try {
				compiler.cook(javaClassDTO.getClassText());

				Class<?> clazz = compiler.getClassLoader().loadClass(javaClassDTO.getClassName());
				Object obj = clazz.getDeclaredConstructor().newInstance(); 
				clazz.getMethod(javaClassDTO.getMethodName()).invoke(obj);
			} catch (Exception e) {
				throw new WebApplicationException(e);
			} 
			return null;
		}
		}
		return null;
	}

}
