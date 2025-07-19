package rs.irm.administration.controller;

import java.util.List;

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
import rs.irm.administration.dto.DashboardDTO;
import rs.irm.administration.dto.DashboardItemDTO;
import rs.irm.administration.dto.DashboardRoleDTO;
import rs.irm.administration.service.DashboardService;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

@Path("/dashboard")
@Tag(name="rs.irm.administration.controller.DashboardController",description = "dashboard")
public class DashboardController {
	
	@Inject
	private DashboardService dashboardService;

	@POST
	@Path("/table")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed("admin")
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	public Response getTable(
			TableParameterDTO tableParameterDTO
			) {
		return Response.ok(dashboardService.getTable(tableParameterDTO)).build();
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed("admin")
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = DashboardDTO.class)))
	public Response getUpdate(
			@Valid DashboardDTO dashboardDTO
			) {
		return Response.ok(dashboardService.getUpdate(dashboardDTO)).build();
	}
	
	@DELETE
	@Path("/{id}")
	@RolesAllowed("admin")
	@ApiResponse(responseCode = "204",description = "No content")
	public Response getDelete(
			@PathParam("id") Long id
			) {
		this.dashboardService.getDelete(id);
		return Response.noContent().build();
	}
	
	@GET
	@Path("/roles/{dashboardid}")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("admin")
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(array = @ArraySchema(schema = @Schema(implementation = DashboardRoleDTO.class))))
	public Response getRoles(
			@PathParam("dashboardid") Long dashboardId
			) {
		return Response.ok(this.dashboardService.getRoles(dashboardId)).build();
	}
	
	@POST
	@Path("/roles/{dashboardid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed("admin")
	@ApiResponse(responseCode = "204",description = "No content")
	public Response changeRoleToDashboard(
			@Valid DashboardRoleDTO dashboardRoleDTO,
			@PathParam("dashboardid") Long dashboardId
			) {
		this.dashboardService.changeRoleToDashboard(dashboardId, dashboardRoleDTO);
		return Response.noContent().build();
	}
	
	@GET
	@Path("/reports/combobox")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("admin")
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComboboxDTO.class))))
	public Response getReportsCombobox(
			) {
		
		return Response.ok(this.dashboardService.getReportsCombobox()).build();
	}
	
	@POST
	@Path("/reports/table")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed("admin")
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	public Response getReportsTable(
			TableParameterDTO tableParameterDTO
			) {
		
		return Response.ok(this.dashboardService.getReportsTable(tableParameterDTO)).build();
	}
	
	@POST
	@Path("/reports/{dashboardid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed("admin")
	@ApiResponse(responseCode = "204",description = "No content")
	public Response getUpdateReports(
			List<DashboardItemDTO> dashboardItemDTOs,
			@PathParam("dashboardid") Long dashboardid
			) {
		this.dashboardService.getUpdateReports(dashboardItemDTOs, dashboardid);
		return Response.noContent().build();
	}
	
	@GET
	@Path("/reports/{dashboardid}")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("admin")
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(array = @ArraySchema(schema = @Schema(implementation = DashboardItemDTO.class))))
	public Response getReportList(
			@PathParam("dashboardid") Long dashboardid
			) {
		
		return Response.ok(this.dashboardService.getReportList(dashboardid)).build();
	}
}
