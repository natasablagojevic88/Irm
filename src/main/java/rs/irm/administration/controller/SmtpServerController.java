package rs.irm.administration.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import rs.irm.administration.dto.SmtpServerDTO;
import rs.irm.administration.dto.TestSmtpDTO;
import rs.irm.administration.service.SmtpServerService;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

@Path("/smtpserver")
@Tag(name="rs.irm.administration.controller.SmtpServerController",description = "smtpserver")
public class SmtpServerController {
	
	@Inject
	private SmtpServerService smtpServerService;

	@POST
	@Path("/table")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed(value = { "admin" })
	@SecurityRequirement(name="JWT")
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	public Response getTable(
			TableParameterDTO tableParameterDTO
			) {
		return Response.ok(smtpServerService.getTable(tableParameterDTO)).build();
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed(value = { "admin" })
	@SecurityRequirement(name="JWT")
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = SmtpServerDTO.class)))
	public Response getTable(
			@Valid SmtpServerDTO smtpServerDTO
			) {
		return Response.ok(smtpServerService.getUpdate(smtpServerDTO)).build();
	}
	
	@DELETE
	@Path("/{id}")
	@RolesAllowed(value = { "admin" })
	@SecurityRequirement(name="JWT")
	@ApiResponse(responseCode = "204",description = "No content")
	public Response getDelete(
			@PathParam("id") Long id
			) {
		this.smtpServerService.getDelete(id);
		return Response.noContent().build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/testsmtp/{id}")
	@RolesAllowed(value = { "admin" })
	@SecurityRequirement(name="JWT")
	@ApiResponse(responseCode = "204",description = "No content")
	public Response getTestMail(
			@Valid TestSmtpDTO testSmtpDTO,
			@PathParam("id") Long id
			) {
		this.smtpServerService.getTestMail(testSmtpDTO,id);
		return Response.noContent().build();
	}
}
