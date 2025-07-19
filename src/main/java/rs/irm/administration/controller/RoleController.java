package rs.irm.administration.controller;

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
import rs.irm.administration.dto.RoleAppUserDTO;
import rs.irm.administration.dto.RoleDTO;
import rs.irm.administration.service.RoleService;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

@Path("/roles")
@Tag(name = "rs.irm.administration.controller.RoleController", description = "roles")
public class RoleController {

	@Inject
	private RoleService roleService;

	@POST
	@Path("/table")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	@RolesAllowed("admin")
	public Response getTable(TableParameterDTO tableParameterDTO) {

		return Response.ok(roleService.getTable(tableParameterDTO)).build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = RoleDTO.class)))
	@RolesAllowed("admin")
	public Response getUpdate(@Valid RoleDTO roleDTO) {

		return Response.ok(roleService.getUpdate(roleDTO)).build();
	}

	@DELETE
	@Path("/{id}")
	@ApiResponse(responseCode = "204", description = "No response")
	@RolesAllowed("admin")
	public Response getDelete(@PathParam("id") Long id) {

		roleService.getDelete(id);
		return Response.noContent().build();
	}

	@GET
	@Path("/users/{roleid}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(array = @ArraySchema(schema = @Schema(implementation = RoleAppUserDTO.class))))
	@RolesAllowed("admin")
	public Response getUsers(@PathParam("roleid") Long roleId) {

		return Response.ok(roleService.getUsersForRole(roleId)).build();
	}

}
