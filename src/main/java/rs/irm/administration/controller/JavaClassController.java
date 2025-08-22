package rs.irm.administration.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import rs.irm.administration.dto.JavaClassDTO;
import rs.irm.administration.service.JavaClassService;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

@Tag(name="rs.irm.administration.controller.JavaClassController",description = "javaclass")
@Path("/java-class")
public class JavaClassController {
	
	@Inject
	private JavaClassService javaClassService;
	
	@Path("/table")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	@RolesAllowed("admin")
	public Response getTable(
			TableParameterDTO tableParameterDTO
			) {
		return Response.ok(javaClassService.getTable(tableParameterDTO)).build();
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = JavaClassDTO.class)))
	@RolesAllowed("admin")
	public Response getUpdate(
			@Valid JavaClassDTO tableParameterDTO
			) {
		return Response.ok(javaClassService.getUpdate(tableParameterDTO)).build();
	}
	
	@DELETE
	@Path("/{id}")
	@ApiResponse(responseCode = "204",description = "No content")
	@RolesAllowed("admin")
	public Response getDelete(
			@PathParam("id") Long id
			) {
		this.javaClassService.getDelete(id);
		return Response.noContent().build();
	}

}
