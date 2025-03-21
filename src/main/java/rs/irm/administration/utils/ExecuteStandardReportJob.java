package rs.irm.administration.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.ReportDTO;
import rs.irm.administration.dto.ReportJobDTO;
import rs.irm.administration.dto.SqlResultColumnDTO;
import rs.irm.administration.dto.SqlResultDTO;
import rs.irm.administration.entity.SmtpServer;
import rs.irm.administration.enums.ReportJobFileType;
import rs.irm.administration.enums.ReportJobMailType;
import rs.irm.administration.enums.ReportJobType;
import rs.irm.administration.service.SqlExecutorService;
import rs.irm.administration.service.impl.SqlExecutorServiceImpl;
import rs.irm.common.enums.Language;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.SendMailService;
import rs.irm.common.service.impl.CommonServiceImpl;
import rs.irm.common.service.impl.SendMailServiceImpl;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.preview.dto.TableReportParameterDTO;
import rs.irm.preview.utils.CreateDashboardData;
import rs.irm.preview.utils.PreviewStandardReport;
import rs.irm.utils.AppParameters;

public class ExecuteStandardReportJob implements ExecuteMethodWithReturn<File> {

	private ReportJobDTO reportJobDTO;
	private ReportDTO reportDTO;
	private DatatableService datatableService;
	private SendMailService sendMail = new SendMailServiceImpl();
	private SqlExecutorService sqlExecutorService = new SqlExecutorServiceImpl();
	private CommonService commonService = new CommonServiceImpl();

	public ExecuteStandardReportJob(ReportJobDTO reportJobDTO, ReportDTO reportDTO) {
		this.reportJobDTO = reportJobDTO;
		this.reportDTO = reportDTO;
		this.datatableService = new DatatableServiceImpl();
	}

	@Override
	public File execute(Connection connection) {
		boolean inline = false;
		if (reportJobDTO.getMailType() != null) {
			if (reportJobDTO.getMailType().equals(ReportJobMailType.INLINE.name())) {
				inline = true;
			}
		}
		TableReportParameterDTO tableReportParameterDTO = new TableReportParameterDTO();
		tableReportParameterDTO
				.setParameters(new CreateDashboardData(null, null).createParameter(reportDTO.getId(), connection));
		PreviewStandardReport previewStandardReportPreview = new PreviewStandardReport(null, tableReportParameterDTO,
				reportDTO.getId());
		SqlResultDTO sqlResultDTO = this.datatableService.executeMethodWithReturn(previewStandardReportPreview,
				connection);
		if(sqlResultDTO.getList().isEmpty()&&reportJobDTO.getConditional()) {
			return null;
		}
		if (!inline) {

			File file = null;

			if (reportJobDTO.getFileType().equals(ReportJobFileType.EXCEL.name())) {
				file = createExcelFile(sqlResultDTO);
			} else if (reportJobDTO.getFileType().equals(ReportJobFileType.CSV.name())) {
				file = createCsvFile(sqlResultDTO);
			}

			if (reportJobDTO.getType().equals(ReportJobType.MAIL.name())) {
				SmtpServer smtpServer = this.datatableService.findByExistingId(reportJobDTO.getSmtpServerId(),
						SmtpServer.class, connection);

				this.sendMail.sendMail(smtpServer, reportJobDTO.getMailTo(), reportJobDTO.getMailSubject(),
						reportJobDTO.getMailBody(), file);
			}

			return file;

		} else {
			String inlineText = createHtmlInline(sqlResultDTO);
			SmtpServer smtpServer = this.datatableService.findByExistingId(reportJobDTO.getSmtpServerId(),
					SmtpServer.class, connection);
			this.sendMail.sendMail(smtpServer, reportJobDTO.getMailTo(), reportJobDTO.getMailSubject(), inlineText,
					null);
			return null;
		}

	}

	public File createExcelFile(SqlResultDTO sqlResultDTO) {

		String fileName = createFileName(reportJobDTO);

		byte[] bytes = (byte[]) sqlExecutorService.createExcel(sqlResultDTO, sqlResultDTO.getSqlQuery(), null)
				.getEntity();

		File file = new File(createFilePath() + "/" + fileName);
		try {
			Files.copy(new ByteArrayInputStream(bytes), Paths.get(file.getAbsolutePath()),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new WebApplicationException(e);
		}

		return file;
	}

	public File createCsvFile(SqlResultDTO sqlResultDTO) {

		String fileName = createFileName(reportJobDTO);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(baos));

			if (this.reportJobDTO.getCsvHasHeader()) {
				int index = 0;
				for (SqlResultColumnDTO column : sqlResultDTO.getColumns()) {
					index++;
					bufferedWriter.write(column.getName());
					if (index != sqlResultDTO.getColumns().size()) {
						bufferedWriter.write(reportJobDTO.getCsvDelimiter());
					}
				}

				bufferedWriter.newLine();
			}

			for (LinkedHashMap<Integer, Object> item : sqlResultDTO.getList()) {
				int index = 0;
				for (SqlResultColumnDTO column : sqlResultDTO.getColumns()) {
					index++;
					Object value = item.get(column.getOrderNumber());
					if (value == null) {
						bufferedWriter.write("");
					} else {
						String type = column.getType().name();

						switch (type) {
						case "BigDecimal": {
							Locale locale = Language.valueOf(AppParameters.defaultlang).locale;
							NumberFormat numberFormat = NumberFormat.getInstance(locale);
							numberFormat.setMinimumFractionDigits(2);
							numberFormat.setMaximumFractionDigits(10);
							BigDecimal bigDecimal = (BigDecimal) value;
							bufferedWriter.write(numberFormat.format(bigDecimal.doubleValue()));
							break;
						}
						case "LocalDate": {
							DateTimeFormatter dateTimeFormatter = DateTimeFormatter
									.ofPattern(AppParameters.jobdateformat);
							LocalDate date = (LocalDate) value;
							bufferedWriter.write(dateTimeFormatter.format(date));
							break;
						}
						case "LocalDateTime": {
							DateTimeFormatter dateTimeFormatter = DateTimeFormatter
									.ofPattern(AppParameters.jobdatetimeformat);
							LocalDateTime date = (LocalDateTime) value;
							bufferedWriter.write(dateTimeFormatter.format(date));
							break;
						}
						default: {
							bufferedWriter.write(value.toString());
							break;
						}
						}
					}

					if (index != sqlResultDTO.getColumns().size()) {
						bufferedWriter.write(reportJobDTO.getCsvDelimiter());
					}
				}

				bufferedWriter.newLine();
			}

			bufferedWriter.close();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

		File file = new File(createFilePath() + "/" + fileName);
		try {
			Files.copy(new ByteArrayInputStream(baos.toByteArray()), Paths.get(file.getAbsolutePath()),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new WebApplicationException(e);
		}

		return file;
	}

	public String createHtmlInline(SqlResultDTO sqlResultDTO) {

		StringWriter stringWriter = new StringWriter();
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			if (commonService.hasText(reportJobDTO.getMailBody())) {
				String mailText = "<div>" + reportJobDTO.getMailBody() + "</div>";
				bufferedWriter.write(mailText);
				bufferedWriter.newLine();
			}

			bufferedWriter.write("<div>");
			bufferedWriter.newLine();
			bufferedWriter.write("<table style=\"border-spacing: 0;margin-top: 10px\">");
			bufferedWriter.newLine();

			int index = 0;
			List<String> styles = new ArrayList<>();
			for (SqlResultColumnDTO column : sqlResultDTO.getColumns()) {
				index++;
				styles = new ArrayList<>();
				styles.add("border: 0.5px solid black");
				styles.add("background-color: lightgray");
				styles.add("text-align: center");
				styles.add("font-weight: bold");
				if (index != 1) {
					styles.add("border-left:0px");
				}
				String stylesText = "";
				for (String style : styles) {
					stylesText += style + ";";
				}
				bufferedWriter.write("<th style=\"" + stylesText + "\">" + column.getName() + "</th>");

			}

			bufferedWriter.newLine();

			for (LinkedHashMap<Integer, Object> item : sqlResultDTO.getList()) {
				index = 0;
				bufferedWriter.write("<tr>");
				for (SqlResultColumnDTO column : sqlResultDTO.getColumns()) {
					index++;
					styles = new ArrayList<>();
					styles.add("border-right: 0.5px solid black;");
					styles.add("border-bottom: 0.5px solid black;");
					styles.add("font-weight: normal;");
					if (index == 1) {
						styles.add("border-left: 0.5px solid black;");
					}
					String cellValue = "";
					Object value = item.get(column.getOrderNumber());
					if (value == null) {
						cellValue = "";
					} else {
						String type = column.getType().name();

						switch (type) {
						case "BigDecimal": {
							Locale locale = Language.valueOf(AppParameters.defaultlang).locale;
							NumberFormat numberFormat = NumberFormat.getInstance(locale);
							numberFormat.setMinimumFractionDigits(2);
							numberFormat.setMaximumFractionDigits(10);
							BigDecimal bigDecimal = (BigDecimal) value;
							cellValue = numberFormat.format(bigDecimal.doubleValue());
							styles.add("text-align: right;");
							break;
						}
						case "LocalDate": {
							DateTimeFormatter dateTimeFormatter = DateTimeFormatter
									.ofPattern(AppParameters.jobdateformat);
							LocalDate date = (LocalDate) value;
							cellValue = dateTimeFormatter.format(date);
							styles.add("text-align: center;");
							break;
						}
						case "LocalDateTime": {
							DateTimeFormatter dateTimeFormatter = DateTimeFormatter
									.ofPattern(AppParameters.jobdatetimeformat);
							LocalDateTime date = (LocalDateTime) value;
							cellValue = dateTimeFormatter.format(date);
							styles.add("text-align: center;");
							break;
						}
						case "Integer", "Long": {
							cellValue = value.toString();
							styles.add("text-align: right;");
							break;
						}
						case "Boolean": {
							cellValue = value.toString();
							styles.add("text-align: center;");
							break;
						}
						default: {
							styles.add("text-align: left;");
							cellValue = value.toString();
							break;
						}
						}
					}

					String stylesText = "";
					for (String style : styles) {
						stylesText += style + ";";
					}
					bufferedWriter.write("<td style=\"" + stylesText + "\">" + cellValue + "</td>");
					bufferedWriter.newLine();

				}

				bufferedWriter.write("</tr>");
				bufferedWriter.newLine();
			}

			bufferedWriter.write("</table>");
			bufferedWriter.newLine();
			bufferedWriter.write("</div>");
			bufferedWriter.newLine();

			bufferedWriter.close();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

		return stringWriter.toString();
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
