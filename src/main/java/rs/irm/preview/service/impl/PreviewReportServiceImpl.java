package rs.irm.preview.service.impl;

import java.net.HttpURLConnection;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import rs.irm.administration.dto.ReportDTO;
import rs.irm.administration.dto.SqlExecuteResultDTO;
import rs.irm.administration.dto.SqlResultDTO;
import rs.irm.administration.enums.ReportType;
import rs.irm.administration.service.SqlExecutorService;
import rs.irm.administration.utils.ModelData;
import rs.irm.common.dto.Base64DownloadFileDTO;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.common.utils.CheckAdmin;
import rs.irm.database.service.DatatableService;
import rs.irm.preview.dto.ReportParameterDTO;
import rs.irm.preview.dto.ReportPreviewInfoDTO;
import rs.irm.preview.dto.TableReportParameterDTO;
import rs.irm.preview.service.PreviewReportService;
import rs.irm.preview.utils.CreateStandardParameters;
import rs.irm.preview.utils.PreviewExecuteReport;
import rs.irm.preview.utils.PreviewGraphReport;
import rs.irm.preview.utils.PreviewJasperReport;
import rs.irm.preview.utils.PreviewKpiReport;
import rs.irm.preview.utils.PreviewSqlReport;
import rs.irm.preview.utils.PreviewStandardReport;

@Named
public class PreviewReportServiceImpl implements PreviewReportService {

	@Inject
	private DatatableService datatableService;

	@Inject
	private CommonService commonService;

	@Inject
	private ResourceBundleService resourceBundleService;

	@Inject
	private SqlExecutorService sqlExecutorService;

	@Context
	private HttpServletRequest httpServletRequest;

	@Override
	public ReportPreviewInfoDTO getParameters(Long reportId) {
		checkRight(reportId);
		ReportDTO reportDTO = findReportDTO(reportId);

		ReportPreviewInfoDTO reportPreviewInfoDTO = new ReportPreviewInfoDTO();

		reportPreviewInfoDTO.setName(resourceBundleService.getText(reportDTO.getName(), null));
		reportPreviewInfoDTO.setReportType(ReportType.valueOf(reportDTO.getType()));

		reportPreviewInfoDTO.setParameters(standardParameters(reportId));

		return reportPreviewInfoDTO;
	}

	private ReportDTO findReportDTO(Long reportId) {
		return ModelData.listReportDTOs.stream().filter(a -> a.getId().doubleValue() == reportId.doubleValue())
				.findFirst().get();
	}


	private List<ReportParameterDTO> standardParameters(Long reportId) {

		CreateStandardParameters createStandardParameters = new CreateStandardParameters(reportId, httpServletRequest);
		return this.datatableService.executeMethodWithReturn(createStandardParameters);
	}

	private void checkRight(Long reportId) {

		ReportDTO reportDTO = ModelData.listReportDTOs.stream()
				.filter(a -> a.getId().doubleValue() == reportId.doubleValue()).findFirst().get();

		List<String> reportRoles = ModelData.listReportGroupRolesDTOs.stream()
				.filter(a -> a.getReportGroupId().doubleValue() == reportDTO.getReportGroupId().doubleValue())
				.map(a -> a.getRoleCode()).toList();

		List<String> roles = commonService.getRoles();
		Boolean checkAdmin = roles.contains(CheckAdmin.roleAdmin);

		if (checkAdmin) {
			return;
		}

		boolean hasRight = false;

		for (String role : reportRoles) {
			if (roles.contains(role)) {
				hasRight = true;
				break;
			}
		}

		if (!hasRight) {
			throw new CommonException(HttpURLConnection.HTTP_FORBIDDEN, "noRight", null);
		}

	}

	@Override
	public SqlResultDTO getPreview(TableReportParameterDTO tableReportParameterDTO, Long reportId) {
		checkRight(reportId);
		ReportDTO reportDTO = findReportDTO(reportId);

		if (reportDTO.getType().equals(ReportType.STANDARD.name())) {
			return standardReportPreview(tableReportParameterDTO, reportId);
		} else if (reportDTO.getType().equals(ReportType.GRAPH.name())) {
			return graphReportPreview(tableReportParameterDTO, reportId);
		} else if (reportDTO.getType().equals(ReportType.SQL.name())) {
			return sqlReportPreview(tableReportParameterDTO, reportId);
		} else if (reportDTO.getType().equals(ReportType.KPI.name())) {
			return kpiReportPreview(tableReportParameterDTO, reportId);
		}

		return null;
	}

	private SqlResultDTO standardReportPreview(TableReportParameterDTO tableReportParameterDTO, Long reportId) {
		PreviewStandardReport previewStandardReportPreview = new PreviewStandardReport(httpServletRequest,
				tableReportParameterDTO, reportId);
		return this.datatableService.executeMethodWithReturn(previewStandardReportPreview);
	}

	private SqlResultDTO graphReportPreview(TableReportParameterDTO tableReportParameterDTO, Long reportId) {
		PreviewGraphReport previewGraphReport = new PreviewGraphReport(httpServletRequest, tableReportParameterDTO,
				reportId);
		return this.datatableService.executeMethodWithReturn(previewGraphReport);
	}

	private SqlResultDTO sqlReportPreview(TableReportParameterDTO tableReportParameterDTO, Long reportId) {
		PreviewSqlReport previewSqleport = new PreviewSqlReport(httpServletRequest, tableReportParameterDTO, reportId);
		return this.datatableService.executeMethodWithReturn(previewSqleport);
	}
	
	private SqlResultDTO kpiReportPreview(TableReportParameterDTO tableReportParameterDTO, Long reportId) {
		PreviewKpiReport previewSqleport = new PreviewKpiReport(httpServletRequest, tableReportParameterDTO, reportId);
		return this.datatableService.executeMethodWithReturn(previewSqleport);
	}

	@Override
	public Response getExcel(TableReportParameterDTO tableReportParameterDTO, Long reportId) {
		checkRight(reportId);
		ReportDTO reportDTO = findReportDTO(reportId);

		if (reportDTO.getType().equals(ReportType.STANDARD.name())) {
			SqlResultDTO resultDTO = standardReportPreview(tableReportParameterDTO, reportId);

			return sqlExecutorService.createExcel(resultDTO, resultDTO.getSqlQuery(), reportDTO.getCode());
		}else if (reportDTO.getType().equals(ReportType.SQL.name())) {
			SqlResultDTO resultDTO = sqlReportPreview(tableReportParameterDTO, reportId);

			return sqlExecutorService.createExcel(resultDTO, resultDTO.getSqlQuery(), reportDTO.getCode());
		}

		return null;
	}

	@Override
	public Base64DownloadFileDTO getExcelBase64(TableReportParameterDTO tableReportParameterDTO, Long reportId) {
		checkRight(reportId);
		Response response = getExcel(tableReportParameterDTO, reportId);
		return commonService.responseToBase64(response);
	}

	@Override
	public Response getJasperReport(TableReportParameterDTO tableReportParameterDTO, Long reportId) {
		checkRight(reportId);
		PreviewJasperReport previewJasperReport=new PreviewJasperReport(reportId,tableReportParameterDTO,httpServletRequest);
		return this.datatableService.executeMethodWithReturn(previewJasperReport);
	}

	@Override
	public Base64DownloadFileDTO getJasperReportBase64(TableReportParameterDTO tableReportParameterDTO, Long reportId) {
		Response response=getJasperReport(tableReportParameterDTO, reportId);
		
		return commonService.responseToBase64(response);
	}

	@Override
	public SqlExecuteResultDTO getExecuteReport(TableReportParameterDTO tableReportParameterDTO, Long reportId) {
		checkRight(reportId);
		PreviewExecuteReport previewExecuteReport=new PreviewExecuteReport(this.httpServletRequest,reportId,tableReportParameterDTO);
		return this.datatableService.executeMethodWithReturn(previewExecuteReport);
	}

}
