package rs.irm.administration.controller;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import rs.irm.administration.dto.MenuDTO;
import rs.irm.administration.service.MenuService;
import rs.irm.preview.dto.DashboardResultDTO;

@Path("/menu")
@Tag(name="rs.irm.administration.controller.MenuController",description = "menu")
public class MenuController {
	
	@Inject
	private MenuService menuService;
	
	@GET
	@SecurityRequirement(name="JWT")
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(array = @ArraySchema(schema = @Schema(implementation = MenuDTO.class))))
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMenu() {
		return Response.ok(menuService.getMenu()).build();
	}
	
	@GET
	@Path("/dashboard")
	@SecurityRequirement(name="JWT")
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(array = @ArraySchema(schema = @Schema(implementation = DashboardResultDTO.class))))
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDefaultDashboard() {
		return Response.ok(menuService.getDefaultDashboard()).build();
	}

}
