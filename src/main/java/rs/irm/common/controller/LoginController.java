package rs.irm.common.controller;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.common.dto.LoginDTO;
import rs.irm.common.service.LoginService;

@Path("/login")
@Tag(name = "rs.irm.common.controller.LoginController", description = "login")
public class LoginController {
	@Inject
	private LoginService loginService;

	@ApiResponse(responseCode = "204", description = "No content")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(@Valid LoginDTO loginDTO) {
		this.loginService.login(loginDTO);
		return Response.noContent().build();
	}
	
	@ApiResponse(responseCode = "200", description = "Response",content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComboboxDTO.class))))
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	public Response language() {
		return Response.ok(loginService.language()).build();
	}
	
	@ApiResponse(responseCode = "204")
	@GET
	@Path("/logout")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response logout() {
		this.loginService.logout();
		return Response.noContent().build();
	}


}
