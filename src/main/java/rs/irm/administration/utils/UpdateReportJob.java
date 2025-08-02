package rs.irm.administration.utils;

import java.sql.Connection;
import java.sql.Statement;

import org.modelmapper.ModelMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.ReportJobDTO;
import rs.irm.administration.entity.ReportJob;
import rs.irm.administration.enums.ReportJobFileType;
import rs.irm.administration.enums.ReportJobMailType;
import rs.irm.administration.enums.ReportJobType;
import rs.irm.administration.service.LoadReportJobService;
import rs.irm.administration.service.impl.LoadReportJobServiceImpl;
import rs.irm.common.exceptions.FieldRequiredException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.impl.CommonServiceImpl;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.utils.DatabaseListenerJob;

public class UpdateReportJob implements ExecuteMethodWithReturn<ReportJobDTO> {
	private HttpServletRequest httpServletRequest;
	private ReportJobDTO reportJobDTO;

	private DatatableService datatableService;
	private LoadReportJobService loadReportJobService;
	private ModelMapper modelMapper = new ModelMapper();
	private CommonService commonService;

	public UpdateReportJob(HttpServletRequest httpServletRequest, ReportJobDTO reportJobDTO) {
		this.httpServletRequest = httpServletRequest;
		this.reportJobDTO = reportJobDTO;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
		this.commonService = new CommonServiceImpl(this.httpServletRequest);
		this.loadReportJobService = new LoadReportJobServiceImpl();
	}

	@Override
	public ReportJobDTO execute(Connection connection) {
		ReportJob reportJob = reportJobDTO.getId() != 0
				? this.datatableService.findByExistingId(reportJobDTO.getId(), ReportJob.class, connection)
				: new ReportJob();

		if (reportJobDTO.getType().equals(ReportJobType.MAIL.name())) {
			if (!commonService.hasText(reportJobDTO.getFileType())&&reportJobDTO.getMailType().equals(ReportJobMailType.ATTACHMENT.name())) {
				throw new FieldRequiredException("ReportJobDTO.fileType");
			}

			if (reportJobDTO.getSmtpServerId() == null) {
				throw new FieldRequiredException("ReportJobDTO.smtpServerId");
			}

			if (!commonService.hasText(reportJobDTO.getMailTo())) {
				throw new FieldRequiredException("ReportJobDTO.mailTo");
			}

			if (!commonService.hasText(reportJobDTO.getMailSubject())) {
				throw new FieldRequiredException("ReportJobDTO.mailSubject");
			}

			if (!commonService.hasText(reportJobDTO.getMailType())) {
				throw new FieldRequiredException("ReportJobDTO.mailType");
			}
			reportJobDTO.setFilePath(null);

			if (reportJobDTO.getMailType().equals(ReportJobMailType.INLINE.name())) {
				reportJobDTO.setFileName(null);
			}
		} else if (reportJobDTO.getType().equals(ReportJobType.TRANSFER.name())) {
			if (!commonService.hasText(reportJobDTO.getFilePath())) {
				throw new FieldRequiredException("ReportJobDTO.filePath");
			}
		} else if (reportJobDTO.getType().equals(ReportJobType.IMPORT.name())) {
			if (!commonService.hasText(reportJobDTO.getFilePath())) {
				throw new FieldRequiredException("ReportJobDTO.filePath");
			}
			
			if(commonService.hasText(reportJobDTO.getParentQuery())) {
				commonService.checkDefaultParameter(reportJobDTO.getParentQuery());
			}
		}

		if (!reportJobDTO.getType().equals(ReportJobType.MAIL.name())) {
			reportJobDTO.setMailTo(null);
			reportJobDTO.setMailSubject(null);
			reportJobDTO.setMailType(null);
			reportJobDTO.setMailBody(null);

		}

		if (!(reportJobDTO.getType().equals(ReportJobType.TRANSFER.name())||
				reportJobDTO.getType().equals(ReportJobType.IMPORT.name())
				)) {
			reportJobDTO.setFilePath(null);
		}

		if (reportJobDTO.getType().equals(ReportJobType.EXECUTE.name())) {
			reportJobDTO.setFileName(null);
			reportJobDTO.setMailTo(null);
			reportJobDTO.setMailSubject(null);
			reportJobDTO.setMailType(null);
			reportJobDTO.setMailBody(null);
			reportJobDTO.setFilePath(null);
		}
		
		if(reportJobDTO.getMailType()!=null) {
			if(reportJobDTO.getMailType().equals(ReportJobMailType.INLINE.name())) {
				reportJobDTO.setFileType(null);
				reportJobDTO.setCsvDelimiter(null);
			}
		}
		
		if(!reportJobDTO.getType().equals(ReportJobType.IMPORT.name())) {
			reportJob.setParentQuery(null);
		}

		if (reportJobDTO.getFileType() != null) {
			if (reportJobDTO.getFileType().equals(ReportJobFileType.CSV.name())) {
				if (!commonService.hasText(reportJobDTO.getCsvDelimiter())) {
					throw new FieldRequiredException("ReportJobDTO.csvDelimiter");
				}

			} else {
				reportJobDTO.setCsvDelimiter(null);
			}
		} else {
			reportJobDTO.setCsvDelimiter(null);
		}
		
		if(commonService.hasText(reportJobDTO.getMailError())&&reportJobDTO.getSmtpServerId()==null){
			throw new FieldRequiredException("ReportJobDTO.smtpServerId");
		}

		modelMapper.map(reportJobDTO, reportJob);

		reportJob = this.datatableService.save(reportJob, connection);
		this.loadReportJobService.loadJob(reportJob);
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("NOTIFY " + DatabaseListenerJob.reportjob_listener + ", 'Report job changed';");
			statement.close();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

		return modelMapper.map(reportJob, ReportJobDTO.class);
	}

}
