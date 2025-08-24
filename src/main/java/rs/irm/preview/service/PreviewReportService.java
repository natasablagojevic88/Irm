package rs.irm.preview.service;

import jakarta.ws.rs.core.Response;
import rs.irm.administration.dto.SqlExecuteResultDTO;
import rs.irm.administration.dto.SqlResultDTO;
import rs.irm.common.dto.Base64DownloadFileDTO;
import rs.irm.preview.dto.CardResultDTO;
import rs.irm.preview.dto.ReportPreviewInfoDTO;
import rs.irm.preview.dto.TableReportParameterDTO;

public interface PreviewReportService {

	ReportPreviewInfoDTO getParameters(Long reportId);

	SqlResultDTO getPreview(TableReportParameterDTO tableReportParameterDTO, Long reportId);

	Response getExcel(TableReportParameterDTO tableReportParameterDTO, Long reportId);

	Base64DownloadFileDTO getExcelBase64(TableReportParameterDTO tableReportParameterDTO, Long reportId);

	Response getJasperReport(TableReportParameterDTO tableReportParameterDTO, Long reportId);

	Base64DownloadFileDTO getJasperReportBase64(TableReportParameterDTO tableReportParameterDTO, Long reportId);
	
	SqlExecuteResultDTO getExecuteReport(TableReportParameterDTO tableReportParameterDTO, Long reportId);
	
	CardResultDTO getCardReportParameter(Long reportId);
	
	CardResultDTO getCardReportResult(Long reportId,CardResultDTO cardResultDTO);
}
