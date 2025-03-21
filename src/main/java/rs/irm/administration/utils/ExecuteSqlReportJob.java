package rs.irm.administration.utils;

import java.io.File;
import java.sql.Connection;

import rs.irm.administration.dto.ReportDTO;
import rs.irm.administration.dto.ReportJobDTO;
import rs.irm.administration.dto.SqlResultDTO;
import rs.irm.administration.entity.SmtpServer;
import rs.irm.administration.enums.ReportJobFileType;
import rs.irm.administration.enums.ReportJobMailType;
import rs.irm.administration.enums.ReportJobType;
import rs.irm.common.service.SendMailService;
import rs.irm.common.service.impl.SendMailServiceImpl;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.preview.dto.TableReportParameterDTO;
import rs.irm.preview.utils.CreateDashboardData;
import rs.irm.preview.utils.PreviewSqlReport;

public class ExecuteSqlReportJob implements ExecuteMethodWithReturn<File>{

	private ReportJobDTO reportJobDTO;
	private ReportDTO reportDTO;
	private DatatableService datatableService;
	private SendMailService sendMail=new SendMailServiceImpl();
	public ExecuteSqlReportJob(ReportJobDTO reportJobDTO, ReportDTO reportDTO) {
		this.reportJobDTO = reportJobDTO;
		this.reportDTO = reportDTO;
		this.datatableService=new DatatableServiceImpl();
	}
	@Override
	public File execute(Connection connection) {
		boolean inline=false;
		if(reportJobDTO.getMailType()!=null) {
			if(reportJobDTO.getMailType().equals(ReportJobMailType.INLINE.name())) {
				inline=true;
			}
		}
		TableReportParameterDTO tableReportParameterDTO=new TableReportParameterDTO();
		tableReportParameterDTO.setParameters(new CreateDashboardData(null, null).createParameter(reportDTO.getId(), connection));
		PreviewSqlReport previewSqlReport = new PreviewSqlReport(null,
				tableReportParameterDTO, reportDTO.getId());
		SqlResultDTO sqlResultDTO=this.datatableService.executeMethodWithReturn(previewSqlReport, connection);
		if(sqlResultDTO.getList().isEmpty()&&reportJobDTO.getConditional()) {
			return null;
		}
		if(!inline) {

			File file=null;
			
			if(reportJobDTO.getFileType().equals(ReportJobFileType.EXCEL.name())) {
				ExecuteStandardReportJob executestandardReportJob= new ExecuteStandardReportJob(reportJobDTO,reportDTO);
				file= executestandardReportJob.createExcelFile(sqlResultDTO);
			}else if(reportJobDTO.getFileType().equals(ReportJobFileType.CSV.name())) {
				ExecuteStandardReportJob executestandardReportJob= new ExecuteStandardReportJob(reportJobDTO,reportDTO);
				file=executestandardReportJob.createCsvFile(sqlResultDTO);
			}
			
			if(reportJobDTO.getType().equals(ReportJobType.MAIL.name())) {
				SmtpServer smtpServer=this.datatableService.findByExistingId(reportJobDTO.getSmtpServerId(), SmtpServer.class, connection);
				
				this.sendMail.sendMail(smtpServer, reportJobDTO.getMailTo(), reportJobDTO.getMailSubject(), reportJobDTO.getMailBody(), file);
			}
			
			return file;
			
		}else {
			ExecuteStandardReportJob executestandardReportJob= new ExecuteStandardReportJob(reportJobDTO,reportDTO);
			String inlineText=executestandardReportJob.createHtmlInline(sqlResultDTO);
			SmtpServer smtpServer=this.datatableService.findByExistingId(reportJobDTO.getSmtpServerId(), SmtpServer.class, connection);
			this.sendMail.sendMail(smtpServer, reportJobDTO.getMailTo(), reportJobDTO.getMailSubject(), inlineText, null);
			return null;
		}
	}
	
	
}
