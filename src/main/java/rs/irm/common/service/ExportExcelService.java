package rs.irm.common.service;

import jakarta.ws.rs.core.Response;
import rs.irm.common.dto.Base64DownloadFileDTO;
import rs.irm.database.dto.TableDataDTO;

public interface ExportExcelService {
	
	Response getExportExcel(TableDataDTO<?> tableDataDTO);
	
	Base64DownloadFileDTO getBase64DownloadFileDTO(TableDataDTO<?> tableDataDTO);
}

