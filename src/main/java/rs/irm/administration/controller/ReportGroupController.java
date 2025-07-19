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
import rs.irm.administration.dto.ReportGroupDTO;
import rs.irm.administration.dto.ReportGroupRoleDTO;
import rs.irm.administration.service.ReportGroupService;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

@Tag(name="rs.irm.administration.controller.ReportGroupController",description = "reportgroup")
@Path("/reportgroup")
public class ReportGroupController {
	
	@Inject
	private ReportGroupService reportGroupService;
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/table")
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	@RolesAllowed(value = { "admin" })
	public Response getTable(
			TableParameterDTO tableParameterDTO
			) {
		return Response.ok(reportGroupService.getTable(tableParameterDTO)).build();
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = ReportGroupDTO.class)))
	@RolesAllowed(value = { "admin" })
	public Response getUpdate(
			@Valid ReportGroupDTO reportGroupDTO
			) {
		return Response.ok(reportGroupService.getUpdate(reportGroupDTO)).build();
	}
	
	@DELETE
	@Path("/{id}")
	@ApiResponse(responseCode = "204",description = "No content")
	@RolesAllowed(value = { "admin" })
	public Response getDelete(
			@PathParam("id") Long id
			) {
		this.reportGroupService.getDelete(id);
		return Response.noContent().build();
	}
	
	@GET
	@Path("/roles/{reportgroupid}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReportGroupRoleDTO.class))))
	@RolesAllowed(value = { "admin" })
	public Response getRoles(
			@PathParam("reportgroupid") Long reportGroupId
			) {
		return Response.ok(reportGroupService.getRoles(reportGroupId)).build();
	}
	
	@POST
	@Path("/roles/{reportgroupid}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "204",description = "No content")
	@RolesAllowed(value = { "admin" })
	public Response getRolesUpdate(
			@Valid ReportGroupRoleDTO reportGroupRoleDTO,
			@PathParam("reportgroupid") Long reportGroupId
			) {
		reportGroupService.getRoleUpdate(reportGroupRoleDTO, reportGroupId);
		return Response.noContent().build();
	}

}
