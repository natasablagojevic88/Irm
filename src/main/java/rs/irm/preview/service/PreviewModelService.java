package rs.irm.preview.service;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

import jakarta.ws.rs.core.Response;
import rs.irm.common.dto.Base64DownloadFileDTO;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.preview.dto.SubCodebookInfoDTO;

public interface PreviewModelService {

	TableDataDTO<LinkedHashMap<String, Object>> getTable(TableParameterDTO tableParameterDTO, Long modelId,
			Long parentId, boolean checkRight);

	LinkedHashMap<String, Object> getDefaultValues(Long modelID, Long parentId);

	LinkedHashMap<String, List<ComboboxDTO>> getCodebooks(Long modelId);

	TableDataDTO<LinkedHashMap<String, Object>> getCodebooksTable(TableParameterDTO tableParameterDTO, Long modelId,
			String codebook, Long parentid);

	SubCodebookInfoDTO getSubCodebooks(Long modelId, String codebook, Long codebookValue);

	LinkedHashMap<String, Object> getUpdate(Long modelID, Long parentId, LinkedHashMap<String, Object> item);

	void getDelete(Long modelId, Long id);

	LinkedHashMap<String, Object> getLock(Long modelId, Long id);

	LinkedHashMap<String, Object> getUnlock(Long modelId, Long id);

	LinkedHashMap<String, Object> getPreview(Long modelId, Long id);

	Response getExcelTemplate(Long modelId);

	Base64DownloadFileDTO getExcelTemplateBase64(Long modelId);

	TableDataDTO<LinkedHashMap<String, Object>> getCheckTotal(TableParameterDTO tableParameterDTO, Long modelId,
			Long parentId);

	LinkedHashMap<String, Object> getImportExcel(File excelFile, Long modelId, Long parentId);

	Response getPrintJasper(Long jasperReportId, Long id);

	Base64DownloadFileDTO getPrintJasperBase64(Long jasperReportId, Long id);

	LinkedHashMap<String, Object> getChangeEvent(LinkedHashMap<String, Object> value, Long modelId, Long parentId,
			String jsonFunction);
	
	LinkedHashMap<String,List<ComboboxDTO>> getCodebookDisabled(LinkedHashMap<String,Object> value,Long modelId);
}
