package rs.irm.administration.controller;

import java.io.File;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import rs.irm.administration.dto.CheckParentDTO;
import rs.irm.administration.dto.FileUploadPathDTO;
import rs.irm.administration.dto.ModelColumnDTO;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.administration.dto.ModelJasperReportDTO;
import rs.irm.administration.dto.ModelProcedureDTO;
import rs.irm.administration.dto.ModelTriggerDTO;
import rs.irm.administration.dto.NextRowColumnDTO;
import rs.irm.administration.service.ModelService;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.impl.AppInitServiceImpl;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

@Path("/model")
@Tag(name = "rs.irm.administration.controller.ModelController", description = "model")
public class ModelController {

	@Inject
	private ModelService modelService;
	
	@Inject
	private CommonService commonService;

	@GET
	@Path("/tree")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = ModelDTO.class)))
	@RolesAllowed("admin")
	public Response getTree() {
		return Response.ok(modelService.getTree()).build();
	}

	@GET
	@Path("/icons")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComboboxDTO.class))))
	@RolesAllowed("admin")
	public Response getIcons() {
		return Response.ok(AppInitServiceImpl.icons).build();
	}

	@GET
	@Path("/roles")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComboboxDTO.class))))
	@RolesAllowed("admin")
	public Response getRoles() {
		return Response.ok(modelService.getRoles()).build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = ModelDTO.class)))
	@RolesAllowed("admin")
	public Response getUpdate(@Valid ModelDTO modelDTO) {
		return Response.ok(modelService.getUpdate(modelDTO)).build();
	}

	@DELETE
	@Path("/{id}")
	@ApiResponse(responseCode = "204", description = "No content")
	@RolesAllowed("admin")
	public Response getDelete(@PathParam("id") Long id) {
		modelService.getDelete(id);
		return Response.noContent().build();
	}

	@POST
	@Path("/column/table/{modelid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	@RolesAllowed("admin")
	public Response getColumnTable(TableParameterDTO tableParameterDTO, @PathParam("modelid") Long modelId) {
		return Response.ok(modelService.getColumns(tableParameterDTO, modelId)).build();
	}

	@GET
	@Path("/codebook/list")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComboboxDTO.class))))
	@RolesAllowed("admin")
	public Response getCodebookList() {
		return Response.ok(modelService.getCodebookList()).build();
	}

	@POST
	@Path("/codebook/table")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	@RolesAllowed("admin")
	public Response getCodebookTable(TableParameterDTO tableParameterDTO) {
		return Response.ok(modelService.getCodebookTable(tableParameterDTO)).build();
	}

	@GET
	@Path("/nextrowcolumn/{modelid}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = NextRowColumnDTO.class)))
	@RolesAllowed("admin")
	public Response getNextRowColumn(@PathParam("modelid") Long modelId) {
		return Response.ok(modelService.getNextRowColumn(modelId)).build();
	}

	@GET
	@Path("/checkparent/{columnid}/{modelid}/{codebookid}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = CheckParentDTO.class)))
	@RolesAllowed("admin")
	public Response getCheckParent(
			@PathParam("columnid") Long columnId, 
			@PathParam("modelid") Long modelId, 
			@PathParam("codebookid") Long codebookId) {
		return Response.ok(modelService.getCheckParent(columnId, modelId, codebookId)).build();
	}

	@POST
	@Path("/column")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = ModelColumnDTO.class)))
	@RolesAllowed("admin")
	public Response getColumnUpdate(@Valid ModelColumnDTO modelColumnDTO) {
		return Response.ok(modelService.getUpdateColumn(modelColumnDTO)).build();
	}

	@DELETE
	@Path("/column/{id}")
	@ApiResponse(responseCode = "204", description = "No content")
	@RolesAllowed("admin")
	public Response getColumnDelete(@PathParam("id") Long id) {
		modelService.getColumnDelete(id);
		return Response.noContent().build();
	}
	
	@POST
	@Path("/trigger/table/{modelid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	@RolesAllowed("admin")
	public Response getTriggerTable(
			TableParameterDTO tableParameterDTO, 
			@PathParam("modelid") Long modelId
		) {
		return Response.ok(modelService.getTriggerTable(tableParameterDTO, modelId)).build();
	}
	
	@POST
	@Path("/trigger")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = ModelTriggerDTO.class)))
	@RolesAllowed("admin")
	public Response getTriggerUpdate(
			@Valid ModelTriggerDTO modelTriggerDTO
		) {
		return Response.ok(modelService.getTriggerUpdate(modelTriggerDTO)).build();
	}
	
	@GET
	@Path("/allcolumns/{modelid}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComboboxDTO.class))))
	@RolesAllowed("admin")
	public Response getAllColumnsForModel(
			@PathParam("modelid") Long modelId
		) {
		return Response.ok(modelService.getAllColumnsForModel(modelId)).build();
	}
	
	@GET
	@Path("/functions")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComboboxDTO.class))))
	@RolesAllowed("admin")
	public Response getAllTriggerFunctions(
		) {
		return Response.ok(modelService.getAllTriggerFunctions()).build();
	}
	
	@DELETE
	@Path("/trigger/{id}")
	@ApiResponse(responseCode = "204", description = "No content")
	@RolesAllowed("admin")
	public Response getTriggerDelete(
			@PathParam("id") Long id
		) {
		modelService.getTriggerDelete(id);
		return Response.noContent().build();
	}
	
	@GET
	@Path("/refresh")
	@ApiResponse(responseCode = "204", description = "No content")
	@RolesAllowed("admin")
	public Response getRefreshModel(
		) {
		this.modelService.refreshModel();
		return Response.noContent().build();
	}
	
	@POST
	@Path("/jasper/table/{modelid}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response",content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	@RolesAllowed("admin")
	public Response getJasperListTable(
			TableParameterDTO tableParameterDTO,
			@PathParam("modelid") Long modelId
		) {
		
		return Response.ok(modelService.getJasperListTable(tableParameterDTO,modelId)).build();
	}
	
	@POST
	@Path("/uploadfile")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@ApiResponse(responseCode = "200", description = "Response",content = @Content(schema = @Schema(implementation = FileUploadPathDTO.class)))
	@RolesAllowed("admin")
	public Response getUploadFile(
			File file
		) {
		
		return Response.ok(commonService.uploadFile(file)).build();
	}
	
	@POST
	@Path("/jasper")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response",content = @Content(schema = @Schema(implementation = ModelJasperReportDTO.class)))
	@RolesAllowed("admin")
	public Response getJasperUpdate(
			ModelJasperReportDTO modelJasperReportDTO,
			@PathParam("modelid") Long modelId
		) {
		
		return Response.ok(modelService.getJasperUpdate(modelJasperReportDTO)).build();
	}
	
	@DELETE
	@Path("/jasper/{id}")
	@ApiResponse(responseCode = "204", description = "No content")
	@RolesAllowed("admin")
	public Response getJasperDelete(
			@PathParam("id") Long id
		) {
		this.modelService.getJasperDelete(id);
		return Response.noContent().build();
	}
	
	@GET
	@Path("/refreshjasperfiles")
	@ApiResponse(responseCode = "204", description = "No content")
	@RolesAllowed("admin")
	public Response getRefreshJasperFiles(
		) {
		this.modelService.refreshJasperFiles();
		return Response.noContent().build();
	}
	
	@GET
	@Path("/jsonfunctions")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComboboxDTO.class))))
	@RolesAllowed("admin")
	public Response getAllJsonFunctions(
		) {
		return Response.ok(modelService.getAllJsonFunctions()).build();
	}
	
	@POST
	@Path("/jobs/{modelid}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	@RolesAllowed("admin")
	public Response getJobList(
			TableParameterDTO tableParameterDTO,
			@PathParam("modelid") Long modelID
		) {
		return Response.ok(modelService.getJobs(tableParameterDTO, modelID)).build();
	}
	
	@POST
	@Path("/procedure/table/{modelid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	@RolesAllowed("admin")
	public Response getProcedureTable(
			TableParameterDTO tableParameterDTO, 
			@PathParam("modelid") Long modelId
		) {
		return Response.ok(modelService.getProceduresTable(tableParameterDTO, modelId)).build();
	}
	
	@GET
	@Path("/procedures")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComboboxDTO.class))))
	@RolesAllowed("admin")
	public Response getProcedures(
		) {
		return Response.ok(modelService.getProcedures()).build();
	}
	
	@POST
	@Path("/procedure/update")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = ComboboxDTO.class)))
	@RolesAllowed("admin")
	public Response getProcedureUpdate(
			@Valid ModelProcedureDTO modelProcedureDTO
		) {
		return Response.ok(modelService.getUpdateProcedure(modelProcedureDTO)).build();
	}
	
	@DELETE
	@Path("/procedure/delete/{id}")
	@ApiResponse(responseCode = "204", description = "No content")
	@RolesAllowed("admin")
	public Response getProcedureDelete(
			@PathParam("id") Long id
		) {
		this.modelService.getProcedureDelete(id);
		return Response.noContent().build();
	}

}
