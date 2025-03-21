package rs.irm.common.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javassist.bytecode.ByteArray;
import rs.irm.common.dto.Base64DownloadFileDTO;
import rs.irm.common.service.ExportExcelService;
import rs.irm.database.dto.TableDataDTO;

@Path("/excel")
@Tag(name="rs.irm.common.controller.ExportExcelController",description = "excel")
public class ExportExcelController {
	
	@Inject
	private ExportExcelService exportExcelService;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@SecurityRequirement(name="JWT")
	@ApiResponse(responseCode = "200", description = "Response",content = @Content(schema = @Schema(implementation = ByteArray.class)))
	public Response getExportExcel(
			@SuppressWarnings("rawtypes") TableDataDTO tableDataDTO
			) {
		return exportExcelService.getExportExcel(tableDataDTO);
	}
	
	@POST
	@Path("/base64")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name="JWT")
	@ApiResponse(responseCode = "200", description = "Response",content = @Content(schema = @Schema(implementation = Base64DownloadFileDTO.class)))
	public Response getExportExcelBase64(
			@SuppressWarnings("rawtypes") TableDataDTO tableDataDTO
			) {
		return Response.ok(exportExcelService.getBase64DownloadFileDTO(tableDataDTO)).build();
	}
}
