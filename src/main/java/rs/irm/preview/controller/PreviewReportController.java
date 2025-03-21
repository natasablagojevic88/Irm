package rs.irm.preview.controller;

import java.io.ByteArrayOutputStream;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import rs.irm.administration.dto.SqlExecuteResultDTO;
import rs.irm.administration.dto.SqlResultDTO;
import rs.irm.common.dto.Base64DownloadFileDTO;
import rs.irm.preview.dto.ReportPreviewInfoDTO;
import rs.irm.preview.dto.TableReportParameterDTO;
import rs.irm.preview.service.PreviewReportService;

@Path("/preview/report")
@Tag(name = "rs.irm.preview.controller.PreviewReportController", description = "preview report")
public class PreviewReportController {

	@Inject
	private PreviewReportService previewReportService;

	@GET
	@Path("/parameters/{reportid}")
	@Produces(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = ReportPreviewInfoDTO.class)))
	public Response getParameters(@PathParam("reportid") Long reportId) {

		return Response.ok(this.previewReportService.getParameters(reportId)).build();
	}

	@POST
	@Path("/{reportid}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = SqlResultDTO.class)))
	public Response getPreviewReport(TableReportParameterDTO tableReportParameterDTO,
			@PathParam("reportid") Long reportId) {

		return Response.ok(this.previewReportService.getPreview(tableReportParameterDTO, reportId)).build();
	}

	@POST
	@Path("/excel/{reportid}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Consumes(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = ByteArrayOutputStream.class)))
	public Response getPreviewExcel(TableReportParameterDTO tableReportParameterDTO,
			@PathParam("reportid") Long reportId) {

		return this.previewReportService.getExcel(tableReportParameterDTO, reportId);
	}

	@POST
	@Path("/excel/base64/{reportid}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = Base64DownloadFileDTO.class)))
	public Response getPreviewExcelBase64(TableReportParameterDTO tableReportParameterDTO,
			@PathParam("reportid") Long reportId) {

		return Response.ok(this.previewReportService.getExcelBase64(tableReportParameterDTO, reportId)).build();
	}

	@POST
	@Path("/jasper/{reportid}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Consumes(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = ByteArrayOutputStream.class)))
	public Response getJasperReport(TableReportParameterDTO tableReportParameterDTO,
			@PathParam("reportid") Long reportId) {

		return this.previewReportService.getJasperReport(tableReportParameterDTO, reportId);
	}

	@POST
	@Path("/jasper/base64/{reportid}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = Base64DownloadFileDTO.class)))
	public Response getJasperReportBase64(TableReportParameterDTO tableReportParameterDTO,
			@PathParam("reportid") Long reportId) {

		return Response.ok(this.previewReportService.getJasperReportBase64(tableReportParameterDTO, reportId)).build();
	}

	@POST
	@Path("/execute/{reportid}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = SqlExecuteResultDTO.class)))
	public Response getExecuteReport(TableReportParameterDTO tableReportParameterDTO,
			@PathParam("reportid") Long reportId) {

		return Response.ok(this.previewReportService.getExecuteReport(tableReportParameterDTO, reportId)).build();
	}

}
