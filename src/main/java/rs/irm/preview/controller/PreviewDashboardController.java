package rs.irm.preview.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import rs.irm.preview.dto.DashboardResultDTO;
import rs.irm.preview.service.PreviewDashboardService;

@Path("/preview/previewdashboard")
@Tag(name="rs.irm.preview.controller.PreviewDashboardController",description = "prewview dashboard")
public class PreviewDashboardController {
	
	@Inject
	private PreviewDashboardService previewDashboardService;
	
	@Path("/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@SecurityRequirement(name="JWT")
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = DashboardResultDTO.class)))
	public Response getDashboardData(
			@PathParam("id") Long id
			) {
		return Response.ok(previewDashboardService.getDashboardData(id)).build();
	}
	
	
	@GET
	@Path("/setdefault/{dashboardid}")
	@SecurityRequirement(name="JWT")
	@ApiResponse(responseCode = "204",description = "No content")
	public Response setDefaultDashboard(
			@PathParam("dashboardid") Long dashboardid
			) {
		this.previewDashboardService.setDefaultDashboard(dashboardid);
		return Response.noContent().build();
	}
	
	@GET
	@Path("/removedefault/{dashboardid}")
	@SecurityRequirement(name="JWT")
	@ApiResponse(responseCode = "204",description = "No content")
	public Response setRemoveDashboard(
			@PathParam("dashboardid") Long dashboardid
			) {
		this.previewDashboardService.removeDefaultDashboard(dashboardid);
		return Response.noContent().build();
	}

}
