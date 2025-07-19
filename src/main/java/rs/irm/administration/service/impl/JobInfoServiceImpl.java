package rs.irm.administration.service.impl;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import rs.irm.administration.dto.JobEditInfoDTO;
import rs.irm.administration.dto.JobInfoDTO;
import rs.irm.administration.dto.JobLogDTO;
import rs.irm.administration.dto.ReportJobDTO;
import rs.irm.administration.dto.ReportJobSessionDTO;
import rs.irm.administration.entity.ReportJob;
import rs.irm.administration.enums.ReportJobFileType;
import rs.irm.administration.enums.ReportJobMailType;
import rs.irm.administration.enums.ReportJobType;
import rs.irm.administration.service.JobInfoService;
import rs.irm.administration.service.ReportService;
import rs.irm.administration.utils.ModelData;
import rs.irm.common.dto.Base64DownloadFileDTO;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.common.service.impl.AppInitServiceImpl;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.ColumnType;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.QueryWriterServiceImpl;
import rs.irm.database.utils.ColumnData;
import rs.irm.database.utils.TableFilter;

@Named
public class JobInfoServiceImpl implements JobInfoService {

	@Inject
	private ResourceBundleService resourceBundleService;

	@Inject
	private CommonService commonService;
	
	@Inject
	private ReportService reportService;
	
	@Inject
	private DatatableService datatableService;
	
	private ModelMapper modelMapper=new ModelMapper();

	@Override
	public TableDataDTO<JobInfoDTO> getList() {
		TableDataDTO<JobInfoDTO> tableDataDTO = new TableDataDTO<>();
		tableDataDTO.setTitle(resourceBundleService.getText("JobInfoDTO.title", null));
		try {
			for (Field field : Arrays.asList(JobInfoDTO.class.getDeclaredFields())) {
				field.setAccessible(true);

				if (field.getName().equals("id")) {
					continue;
				}

				if (field.getName().equals("reportName")) {
					continue;
				}

				ColumnData columnData = new ColumnData();
				columnData.setCode(field.getName());
				String typeName = field.getType().getSimpleName();
				if (QueryWriterServiceImpl.fieldTypes.contains(typeName)) {
					columnData.setColumnType(ColumnType.valueOf(typeName));
				} else {
					columnData.setColumnType(ColumnType.String);
				}
				columnData.setName(
						resourceBundleService.getText(JobInfoDTO.class.getSimpleName() + "." + field.getName(), null));
				tableDataDTO.getColumns().add(columnData);
			}
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

		List<JobInfoDTO> jobInfoDTOs = new ArrayList<>();
		try {
			if(AppInitServiceImpl.scheduler!=null) {
				Set<JobKey> setOfKeys = AppInitServiceImpl.scheduler
						.getJobKeys(GroupMatcher.groupEquals(LoadReportJobServiceImpl.jobGroup));

				Iterator<JobKey> iterator = setOfKeys.iterator();

				while (iterator.hasNext()) {
					JobKey jobKey = iterator.next();

					jobInfoDTOs.add(createJobInfoDTO(jobKey));
				}
			}

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
		jobInfoDTOs = jobInfoDTOs.stream().sorted(Comparator.comparing(JobInfoDTO::getName)).toList();
		tableDataDTO.setList(jobInfoDTOs);
		return tableDataDTO;
	}

	private JobInfoDTO createJobInfoDTO(JobKey jobKey) {
		JobInfoDTO jobInfoDTO = new JobInfoDTO();
		
		if(AppInitServiceImpl.scheduler==null) {
			return jobInfoDTO;
		}

		ReportJobDTO reportJobDTO = ModelData.listReportJobDTOs.stream()
				.filter(a -> a.getName().equals(jobKey.getName())).findFirst().get();

		jobInfoDTO.setId(reportJobDTO.getId());
		jobInfoDTO.setName(reportJobDTO.getName());
		ReportJobType reportJobType=ReportJobType.valueOf(reportJobDTO.getType());
		jobInfoDTO.setType(resourceBundleService.getText("ReportJobType."+reportJobType.name(), null));
		jobInfoDTO.setReportName(reportJobDTO.getReportName());
		jobInfoDTO.setActive(reportJobDTO.getActive());

		List<? extends Trigger> triggers;
		try {
			triggers = AppInitServiceImpl.scheduler.getTriggersOfJob(jobKey);

			if (!triggers.isEmpty()) {
				Trigger trigger = triggers.get(0);
				String pattern = "yyyy-MM-dd HH:mm:ss";
				if (trigger.getPreviousFireTime() != null) {
					Date previousTime = trigger.getPreviousFireTime();

					jobInfoDTO.setPreviousTime(LocalDateTime.parse(new SimpleDateFormat(pattern).format(previousTime),
							DateTimeFormatter.ofPattern(pattern)));
				}

				if (trigger.getNextFireTime() != null) {
					Date nextTime = trigger.getNextFireTime();
					jobInfoDTO.setNextTime(LocalDateTime.parse(new SimpleDateFormat(pattern).format(nextTime),
							DateTimeFormatter.ofPattern(pattern)));
				}

			}

			if (ModelData.reportJobStates.containsKey(reportJobDTO.getId())) {
				ReportJobSessionDTO reportJobSession = ModelData.reportJobStates.get(reportJobDTO.getId());
				jobInfoDTO.setStatus(reportJobSession.getState());
				jobInfoDTO.setErrorMessage(reportJobSession.getErrorMessage());
				if (reportJobSession.getExecuteTime() != null) {
					LocalDateTime executeTime = reportJobSession.getExecuteTime();
					if (jobInfoDTO.getPreviousTime() == null) {
						jobInfoDTO.setPreviousTime(executeTime);
					} else {
						if (executeTime.isAfter(jobInfoDTO.getPreviousTime())) {
							jobInfoDTO.setPreviousTime(executeTime);
						}
					}
				}

				if (reportJobSession.getFileBytes() != null) {
					jobInfoDTO.setFileName(reportJobSession.getFileName());

				}
			}
		} catch (SchedulerException e) {
			throw new WebApplicationException(e);
		}

		return jobInfoDTO;
	}

	@Override
	public void getExecute(Long id) {
		ReportJobDTO reportJobDTO = ModelData.listReportJobDTOs.stream()
				.filter(a -> a.getId().doubleValue() == id.doubleValue()).findFirst().get();

		try {
			if(AppInitServiceImpl.scheduler!=null) {
				AppInitServiceImpl.scheduler
				.triggerJob(new JobKey(reportJobDTO.getName(), LoadReportJobServiceImpl.jobGroup));
			}
			
		} catch (SchedulerException e) {
			throw new WebApplicationException(e);
		}

	}

	@Override
	public Response getDownload(Long id) {

		ReportJobSessionDTO reportJobSessionDTO = ModelData.reportJobStates.get(id);

		Response response = Response.status(HttpURLConnection.HTTP_OK)
				.header("filename", reportJobSessionDTO.getFileName())
				.header("Content-Disposition", "attachment;filename=" + reportJobSessionDTO.getFileName())
				.header("Content-Type", reportJobSessionDTO.getFileType())
				.header("Access-Control-Expose-Headers", "filename").entity(reportJobSessionDTO.getFileBytes()).build();
		return response;

	}

	@Override
	public Base64DownloadFileDTO getDownloadBase64(Long id) {

		return commonService.responseToBase64(getDownload(id));
	}

	@Override
	public JobInfoDTO getJobInfo(Long id) {
		ReportJobDTO reportJobDTO = ModelData.listReportJobDTOs.stream()
				.filter(a -> a.getId().doubleValue() == id.doubleValue()).findFirst().get();
		
		JobKey jobKey=new JobKey(reportJobDTO.getName(), LoadReportJobServiceImpl.jobGroup);
		
		return createJobInfoDTO(jobKey);
	}

	@Override
	public JobEditInfoDTO getJobEditInfo(Long id) {
		ReportJob reportJob=this.datatableService.findByExistingId(id, ReportJob.class);
		JobEditInfoDTO jobEditInfoDTO=new JobEditInfoDTO();
		jobEditInfoDTO.setReportJobDTO(modelMapper.map(reportJob, ReportJobDTO.class));
		jobEditInfoDTO.setReportType(reportJob.getReport()==null?"MODEL":reportJob.getReport().getType().name());
		jobEditInfoDTO.setNames(commonService.classToNames(ReportJobDTO.class));
		jobEditInfoDTO.setReportJobTypeList(commonService.enumToCombobox(ReportJobType.class));
		jobEditInfoDTO.setReportJobFileTypeList(commonService.enumToCombobox(ReportJobFileType.class));
		jobEditInfoDTO.setListSmtp(reportService.getSmtpBox());
		jobEditInfoDTO.setReportJobMailType(commonService.enumToCombobox(ReportJobMailType.class));
		
		if(reportJob.getModel()!=null) {
			jobEditInfoDTO.getNames().put("fileName", resourceBundleService.getText("fileNameStartWith", null));
		}
		
		return jobEditInfoDTO;
	}

	@Override
	public TableDataDTO<JobLogDTO> getLogs(TableParameterDTO tableParameterDTO,Long reportJobId) {
		tableParameterDTO.getTableFilters().add(new TableFilter("reportJobId", SearchOperation.equals, String.valueOf(reportJobId), null));
		
		return this.datatableService.getTableDataDTO(tableParameterDTO, JobLogDTO.class);
	}
}
