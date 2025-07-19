package rs.irm.common.controller;


import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import rs.irm.common.dto.NotificationCountDTO;
import rs.irm.common.dto.NotificationDTO;
import rs.irm.common.service.NotificationService;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

@Tag(name="rs.irm.common.controller.NotificationController",description = "notification")
@Path("/notification")
public class NotificationController {

	@Inject
	private NotificationService notificationService;
	
	@GET
	@Path("/count")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = NotificationCountDTO.class)))
	public Response getCount() {
		return Response.ok(notificationService.getCount()).build();
	}
	
	@POST
	@Path("/table")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	public Response getTable(
			TableParameterDTO tableParameterDTO
			) {
		return Response.ok(notificationService.getTable(tableParameterDTO)).build();
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = NotificationDTO.class)))
	public Response getRead(
			@PathParam("id") Long id
			) {
		return Response.ok(notificationService.getRead(id)).build();
	}
	
	@GET
	@Path("/markunread/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = NotificationDTO.class)))
	public Response markAsUnread(
			@PathParam("id") Long id
			) {
		return Response.ok(notificationService.markAsUnread(id)).build();
	}
}
