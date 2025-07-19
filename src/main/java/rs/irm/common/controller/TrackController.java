package rs.irm.common.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import rs.irm.common.service.TrackService;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

@Path("/track")
@Tag(name="rs.irm.common.controller.TrackController",description = "track")
public class TrackController {
	
	@Inject
	private TrackService trackService;
	
	@POST
	@Path("/{table}/{dataid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	public Response getTrackData(
			TableParameterDTO tableParameterDTO,
			@PathParam("table") String table,
			@PathParam("dataid") Long dataid
			) {
		return Response.ok(trackService.getTrackData(tableParameterDTO, table, dataid)).build();
	}

}
