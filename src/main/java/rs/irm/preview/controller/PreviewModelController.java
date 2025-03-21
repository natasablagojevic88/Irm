package rs.irm.preview.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.LinkedHashMap;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import rs.irm.common.dto.Base64DownloadFileDTO;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.preview.dto.SubCodebookInfoDTO;
import rs.irm.preview.service.PreviewModelService;

@Path("/preview/model")
@Tag(name = "rs.irm.preview.controller.PreviewModelController", description = "preview model")
public class PreviewModelController {

	@Inject
	private PreviewModelService previewModelService;

	@POST
	@Path("/table/{modelid}/{parentid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	@SecurityRequirement(name = "JWT")
	public Response getTable(TableParameterDTO tableParameterDTO, @PathParam("modelid") Long modelID,
			@PathParam("parentid") Long parentId) {
		return Response.ok(previewModelService.getTable(tableParameterDTO, modelID, parentId, true)).build();
	}

	@GET
	@Path("/defaultvalues/{modelid}/{parentid}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = LinkedHashMap.class)))
	@SecurityRequirement(name = "JWT")
	public Response getDefaultValues(@PathParam("modelid") Long modelID, @PathParam("parentid") Long parentId) {
		return Response.ok(previewModelService.getDefaultValues(modelID, parentId)).build();
	}

	@GET
	@Path("/codebooks/{modelid}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = LinkedHashMap.class)))
	@SecurityRequirement(name = "JWT")
	public Response getCodebooks(@PathParam("modelid") Long modelID) {
		return Response.ok(previewModelService.getCodebooks(modelID)).build();
	}

	@POST
	@Path("/codebooks/table/{modelId}/{codebook}/{parentid}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	@SecurityRequirement(name = "JWT")
	public Response getCodebooksTable(TableParameterDTO tableParameterDTO, @PathParam("modelId") Long modelId,
			@PathParam("codebook") String codebook, @PathParam("parentid") Long parentid) {
		return Response.ok(previewModelService.getCodebooksTable(tableParameterDTO, modelId, codebook, parentid))
				.build();
	}

	@GET
	@Path("/subcodebooks/{modelid}/{codebook}/{codebookvalue}")
	@Produces(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = SubCodebookInfoDTO.class)))
	public Response getSubCodebooks(@PathParam("modelid") Long modelID, @PathParam("codebook") String codebook,
			@PathParam("codebookvalue") Long codebookValue) {
		return Response.ok(previewModelService.getSubCodebooks(modelID, codebook, codebookValue)).build();
	}

	@POST
	@Path("/{modelid}/{parentid}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = LinkedHashMap.class)))
	public Response getUpdate(LinkedHashMap<String, Object> item, @PathParam("modelid") Long modelId,
			@PathParam("parentid") Long parentId) {
		return Response.ok(previewModelService.getUpdate(modelId, parentId, item)).build();
	}

	@DELETE
	@Path("/{modelid}/{id}")
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "204", description = "No content")
	public Response getDelete(@PathParam("modelid") Long modelId, @PathParam("id") Long id) {
		previewModelService.getDelete(modelId, id);
		return Response.noContent().build();
	}

	@GET
	@Path("/lock/{modelid}/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = LinkedHashMap.class)))
	public Response getLock(@PathParam("modelid") Long modelId, @PathParam("id") Long id) {

		return Response.ok(previewModelService.getLock(modelId, id)).build();
	}

	@GET
	@Path("/unlock/{modelid}/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = LinkedHashMap.class)))
	public Response getUnlock(@PathParam("modelid") Long modelId, @PathParam("id") Long id) {

		return Response.ok(previewModelService.getUnlock(modelId, id)).build();
	}
	
	@GET
	@Path("/preview/{modelid}/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = LinkedHashMap.class)))
	public Response getPreview(@PathParam("modelid") Long modelId, @PathParam("id") Long id) {

		return Response.ok(previewModelService.getPreview(modelId, id)).build();
	}
	
	@GET
	@Path("/excel/template/{modelid}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = ByteArrayOutputStream.class)))
	public Response getExcelTemplate(@PathParam("modelid") Long modelId) {

		return previewModelService.getExcelTemplate(modelId);
	}
	
	@GET
	@Path("/excel/template/{modelid}/base64")
	@Produces(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = Base64DownloadFileDTO.class)))
	public Response getExcelTemplateBase64(@PathParam("modelid") Long modelId) {

		return Response.ok(previewModelService.getExcelTemplateBase64(modelId)).build();
	}
	
	@POST
	@Path("/checktotal/{modelid}/{parentid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	public Response getCheckTotal(TableParameterDTO tableParameterDTO, @PathParam("modelid") Long modelID,
			@PathParam("parentid") Long parentId) {
		return Response.ok(previewModelService.getCheckTotal(tableParameterDTO, modelID, parentId)).build();
	}
	
	@POST
	@Path("/excel/import/{modelid}/{parentid}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = LinkedHashMap.class)))
	public Response getExcelImport(
			File excelFile,
			@PathParam("modelid") Long modelID,
			@PathParam("parentid") Long parentId
			) {

		return Response.ok(previewModelService.getImportExcel(excelFile, modelID, parentId)).build();
	}

	@GET
	@Path("/jasper/{jasperreportid}/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = ByteArrayOutputStream.class)))
	public Response getPrintJasper(
			@PathParam("jasperreportid") Long jasperReportId,
			@PathParam("id") Long id) {

		return previewModelService.getPrintJasper(jasperReportId, id);
		
	}
	
	@GET
	@Path("/jasper/{jasperreportid}/{id}/base64")
	@Produces(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = Base64DownloadFileDTO.class)))
	public Response getPrintJasperBase64(
			@PathParam("jasperreportid") Long jasperReportId,
			@PathParam("id") Long id) {

		return Response.ok(previewModelService.getPrintJasperBase64(jasperReportId, id)).build();
		
	}
	
	@POST
	@Path("/changeevent/{modelid}/{parentid}/{jsonfuntion}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = LinkedHashMap.class)))
	public Response getChangeEvent(
			LinkedHashMap<String,Object> value,
			@PathParam("modelid") Long modelId,
			@PathParam("parentid") Long parentId,
			@PathParam("jsonfuntion") String jsonFunction) {

		return Response.ok(previewModelService.getChangeEvent(value, modelId,parentId, jsonFunction)).build();
		
	}
	
	@POST
	@Path("/codebookdisabled/{modelid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = LinkedHashMap.class)))
	public Response getCodebookDisabled(
			LinkedHashMap<String,Object> value,
			@PathParam("modelid") Long modelId
			) {

		return Response.ok(previewModelService.getCodebookDisabled(value, modelId)).build();
		
	}
}
