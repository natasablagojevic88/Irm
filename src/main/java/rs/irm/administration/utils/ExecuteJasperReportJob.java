package rs.irm.administration.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.ReportDTO;
import rs.irm.administration.dto.ReportJobDTO;
import rs.irm.administration.entity.SmtpServer;
import rs.irm.administration.enums.ReportJobFileType;
import rs.irm.administration.enums.ReportJobType;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.service.SendMailService;
import rs.irm.common.service.impl.SendMailServiceImpl;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.preview.dto.TableReportParameterDTO;
import rs.irm.preview.utils.CreateDashboardData;
import rs.irm.preview.utils.PreviewJasperReport;
import rs.irm.utils.AppParameters;

public class ExecuteJasperReportJob implements ExecuteMethodWithReturn<File> {
	private ReportJobDTO reportJobDTO;
	private ReportDTO reportDTO;
	private DatatableService datatableService;
	private SendMailService sendMail = new SendMailServiceImpl();

	public ExecuteJasperReportJob(ReportJobDTO reportJobDTO, ReportDTO reportDTO) {
		this.reportJobDTO = reportJobDTO;
		this.reportDTO = reportDTO;
		this.datatableService = new DatatableServiceImpl();
	}

	@Override
	public File execute(Connection connection) {
		TableReportParameterDTO tableReportParameterDTO = new TableReportParameterDTO();
		tableReportParameterDTO
				.setParameters(new CreateDashboardData(null, null).createParameter(reportDTO.getId(), connection));
		PreviewJasperReport previewJasperReport = new PreviewJasperReport(reportDTO.getId(), tableReportParameterDTO,
				null);
		byte[] bytes = (byte[]) this.datatableService.executeMethodWithReturn(previewJasperReport, connection)
				.getEntity();
		File file = new File(createFilePath() + "/" + createFileName(reportJobDTO));
		try {
			Files.copy(new ByteArrayInputStream(bytes), Paths.get(file.getAbsolutePath()),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new WebApplicationException(e);
		}
		if (reportJobDTO.getType().equals(ReportJobType.MAIL.name())) {
			SmtpServer smtpServer = this.datatableService.findByExistingId(reportJobDTO.getSmtpServerId(),
					SmtpServer.class, connection);

			this.sendMail.sendMail(smtpServer, reportJobDTO.getMailTo(), reportJobDTO.getMailSubject(),
					reportJobDTO.getMailBody(), file);
		}
		
		return file;
	}
	
	private String createFilePath() {
		String filePath=System.getProperty("java.io.tmpdir");
		if(reportJobDTO.getFilePath()!=null) {
			File file=new File(reportJobDTO.getFilePath());
			if(!(file.isDirectory()&&file.exists())) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "noFolder", reportJobDTO.getFilePath());
			}
			
			filePath=file.getAbsolutePath();
		}
		return filePath;
	}

	private String createFileName(ReportJobDTO reportJobDTO) {
		String fileName = reportJobDTO.getFileName() == null ? "reportJob" : reportJobDTO.getFileName();
		if (reportJobDTO.getAddTimeStamp()) {
			fileName += new SimpleDateFormat(AppParameters.jobdatetimeformat).format(Calendar.getInstance().getTime());
		}

		ReportJobFileType fileType = ReportJobFileType.valueOf(reportJobDTO.getFileType());
		fileName += "." + fileType.extension;

		return fileName;
	}

}
