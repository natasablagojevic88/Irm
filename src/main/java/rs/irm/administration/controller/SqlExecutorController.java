package rs.irm.administration.controller;

import java.io.ByteArrayOutputStream;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import rs.irm.administration.dto.SqlEditorInfo;
import rs.irm.administration.dto.SqlExecuteResultDTO;
import rs.irm.administration.dto.SqlQueryParametersDTO;
import rs.irm.administration.dto.SqlResultDTO;
import rs.irm.administration.service.SqlExecutorService;
import rs.irm.common.dto.Base64DownloadFileDTO;

@Path("/sqlexecutor")
@Tag(name = "rs.irm.administration.controller.SqlExecutorController", description = "sqlexecutor")
public class SqlExecutorController {

	@Inject
	private SqlExecutorService sqlExecutorService;

	@GET
	@Path("/tables")
	@Produces(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@RolesAllowed(value = { "admin" })
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SqlEditorInfo.class))))
	public Response getTablesAndColumns() {
		return Response.ok(sqlExecutorService.getTablesAndColumns()).build();
	}

	@POST
	@Path("/sqlquery")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@RolesAllowed(value = { "admin" })
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = SqlResultDTO.class)))
	public Response getSqlQuery(@Valid SqlQueryParametersDTO sqlQueryParametersDTO) {

		return Response.ok(sqlExecutorService.getSqlQuery(sqlQueryParametersDTO)).build();
	}
	
	@POST
	@Path("/excel")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Consumes(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@RolesAllowed(value = { "admin" })
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = ByteArrayOutputStream.class)))
	public Response getExcel(@Valid SqlQueryParametersDTO sqlQueryParametersDTO) {

		return sqlExecutorService.getExcel(sqlQueryParametersDTO);
	}
	
	@POST
	@Path("/excel/base64")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@RolesAllowed(value = { "admin" })
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = Base64DownloadFileDTO.class)))
	public Response getExcelBase64(@Valid SqlQueryParametersDTO sqlQueryParametersDTO) {

		return Response.ok(sqlExecutorService.getExcelBase64(sqlQueryParametersDTO)).build();
	}
	
	@POST
	@Path("/execute")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@RolesAllowed(value = { "admin" })

	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = SqlExecuteResultDTO.class)))
	public Response getExecute(@Valid SqlQueryParametersDTO sqlQueryParametersDTO
			) {

		return Response.ok(sqlExecutorService.getExecute(sqlQueryParametersDTO)).build();
	}

}
