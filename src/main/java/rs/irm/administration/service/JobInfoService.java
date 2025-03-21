package rs.irm.administration.service;

import jakarta.ws.rs.core.Response;
import rs.irm.administration.dto.JobEditInfoDTO;
import rs.irm.administration.dto.JobInfoDTO;
import rs.irm.administration.dto.JobLogDTO;
import rs.irm.common.dto.Base64DownloadFileDTO;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

public interface JobInfoService {

	TableDataDTO<JobInfoDTO> getList();
	
	void getExecute(Long id);
	
	Response getDownload(Long id);
	
	Base64DownloadFileDTO getDownloadBase64(Long id);
	
	JobInfoDTO getJobInfo(Long id);
	
	JobEditInfoDTO getJobEditInfo(Long id);
	
	TableDataDTO<JobLogDTO> getLogs(TableParameterDTO tableParameterDTO,Long reportJobId);
}
