package rs.irm.administration.service;

import rs.irm.administration.dto.JavaClassDTO;
import rs.irm.administration.dto.ReportJobDTO;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

public interface JavaClassService {

	TableDataDTO<JavaClassDTO> getTable(TableParameterDTO tableParameterDTO);
	
	JavaClassDTO getUpdate(JavaClassDTO javaClassDTO);
	
	void getDelete(Long id);

	TableDataDTO<ReportJobDTO> getJobList(Long id, TableParameterDTO tableParameterDTO);
}
