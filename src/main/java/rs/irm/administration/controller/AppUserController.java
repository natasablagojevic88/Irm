package rs.irm.administration.controller;

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
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import rs.irm.administration.dto.AppUserDTO;
import rs.irm.administration.dto.AppUserInfoDTO;
import rs.irm.administration.dto.AppUserRoleDTO;
import rs.irm.administration.dto.ChangePasswordDTO;
import rs.irm.administration.service.AppUserService;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

@Path("/appuser")
@Tag(name = "rs.irm.administration.controller.AppUserController", description = "appuser")
public class AppUserController {

	@Inject
	private AppUserService appUserService;

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	@RolesAllowed(value = { "admin" })
	@Path("/table")
	public Response getTable(TableParameterDTO tableParameterDTO) {
		return Response.ok(appUserService.getTable(tableParameterDTO)).build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = AppUserDTO.class)))
	@RolesAllowed(value = { "admin" })
	public Response getUpdate(@Valid AppUserDTO appUserDTO) {
		return Response.ok(appUserService.getUpdate(appUserDTO)).build();
	}

	@DELETE
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "204", description = "No response")
	@RolesAllowed(value = { "admin" })
	@Path("/{id}")
	public Response getDelete(@PathParam("id") Long id) {
		appUserService.getDelete(id);
		return Response.noContent().build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AppUserRoleDTO.class))))
	@RolesAllowed(value = { "admin" })
	@Path("/roles/{userid}")
	public Response getListRoleForUser(@PathParam("userid") Long userId) {
		return Response.ok(appUserService.getListRoleForUser(userId)).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "204", description = "No content")
	@RolesAllowed(value = { "admin" })
	@Path("/roles/{userid}")
	public Response getUpdateRoleForUser(@Valid AppUserRoleDTO appUserRoleDTO, @PathParam("userid") Long userId) {
		appUserService.getUpdateRoleForUser(appUserRoleDTO, userId);
		return Response.noContent().build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "204", description = "No content")
	@Path("/changepassword")
	public Response getChangePassword(
			@Valid ChangePasswordDTO changePasswordDTO) {
		this.appUserService.getChangePassword(changePasswordDTO);
		return Response.noContent().build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name = "JWT")
	@ApiResponse(responseCode = "200", description = "Response",content = @Content(schema = @Schema(implementation = AppUserInfoDTO.class)))
	@Path("/userinfo")
	public Response getUserInfo() {
		
		return Response.ok(this.appUserService.getUserInfo()).build();
	}

}
