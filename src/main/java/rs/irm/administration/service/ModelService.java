package rs.irm.administration.service;

import java.util.List;

import rs.irm.administration.dto.CheckParentDTO;
import rs.irm.administration.dto.ModelColumnDTO;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.administration.dto.ModelJasperReportDTO;
import rs.irm.administration.dto.ModelTriggerDTO;
import rs.irm.administration.dto.NextRowColumnDTO;
import rs.irm.administration.dto.ReportJobDTO;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

public interface ModelService {

	ModelDTO getTree();

	List<ComboboxDTO> getRoles();

	ModelDTO getUpdate(ModelDTO modelDTO);

	void getDelete(Long id);

	TableDataDTO<ModelColumnDTO> getColumns(TableParameterDTO tableParameterDTO, Long modelId);
	
	List<ComboboxDTO> getCodebookList();
	
	TableDataDTO<ModelDTO> getCodebookTable(TableParameterDTO tableParameterDTO);
	
	NextRowColumnDTO getNextRowColumn(Long modelId);
	
	CheckParentDTO getCheckParent(Long columnId, Long modelId, Long codebookId);
	
	ModelColumnDTO getUpdateColumn(ModelColumnDTO modelColumnDTO);
	
	void getColumnDelete(Long id);
	
	TableDataDTO<ModelTriggerDTO> getTriggerTable(TableParameterDTO tableParameterDTO, Long modelId);
	
	ModelTriggerDTO getTriggerUpdate(ModelTriggerDTO modelTriggerDTO);
	
	List<ComboboxDTO> getAllColumnsForModel(Long modelId);
	
	List<ComboboxDTO> getAllTriggerFunctions();
	
	void getTriggerDelete(Long id);
	
	void refreshModel();
	
	TableDataDTO<ModelJasperReportDTO> getJasperListTable(TableParameterDTO tableParameterDTO,Long modelId);
	
	ModelJasperReportDTO getJasperUpdate(ModelJasperReportDTO modelJasperReportDTO);
	
	void getJasperDelete(Long id);
	
	void refreshJasperFiles();
	
	List<ComboboxDTO> getAllJsonFunctions();
	
	TableDataDTO<ReportJobDTO> getJobs(TableParameterDTO tableParameterDTO,Long modelID);
}
