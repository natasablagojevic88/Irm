package rs.irm.administration.service;

import java.util.List;

import rs.irm.administration.dto.ReportColumnInfoDTO;
import rs.irm.administration.dto.ReportDTO;
import rs.irm.administration.dto.ReportJobDTO;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

public interface ReportService {

	TableDataDTO<ReportDTO> getTable(TableParameterDTO tableParameterDTO,Long reportGroupId);
	
	List<ReportColumnInfoDTO> getTreeField(Long modelId);
	
	ReportDTO getUpdate(ReportDTO reportDTO);
	
	ReportDTO getInfo(Long id);
	
	void getDelete(Long id);
	
	void getJasperFileRefresh();
	
	TableDataDTO<ReportJobDTO> getTableJobs(TableParameterDTO tableParameterDTO,Long reportId);
	
	List<ComboboxDTO> getSmtpBox();
	
	ReportJobDTO getJobUpdate(ReportJobDTO reportJobDTO);
	
	void getJobDelete(Long id);
}
