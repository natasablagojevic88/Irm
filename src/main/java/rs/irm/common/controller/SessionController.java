package rs.irm.common.controller;

import java.util.LinkedHashMap;
import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.ResourceBundleService;

@Path("/session")
@Tag(name="rs.irm.common.controller.SessionController",description = "session")
public class SessionController {
	
	@Context
	private HttpServletRequest httpServletRequest;
	
	@Inject
	private ResourceBundleService resourceBundleService;
	
	@Inject 
	private CommonService commonService;

	@GET
	@ApiResponse(responseCode = "204",description = "No response")
	public Response checkSession() {
		return Response.noContent().build();
	}
	
	@Path("/resources/{text}")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponse(responseCode = "200",description = "No response",content = @Content(schema = @Schema(implementation = String.class)))
	public Response getResource(@PathParam("text") String text) {
		return Response.ok(resourceBundleService.getText(text, null)).build();
	}
	
	@SuppressWarnings("unchecked")
	@Path("/enumbox/{classpath}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200",description = "No response",content = @Content(array = @ArraySchema(schema=@Schema(implementation = ComboboxDTO.class))))
	public Response getEnumBox(@PathParam("classpath") String classpath) {
		
		try {
			List<ComboboxDTO> list=commonService.enumToCombobox((Class<? extends Enum<?>>) Class.forName(classpath));
			return Response.ok(list).build();
		} catch (Exception e) {
			throw new WebApplicationException(e);
			
		}
	}
	
	@Path("/names/{classpath}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200",description = "No response",content = @Content(array = @ArraySchema(schema=@Schema(implementation = LinkedHashMap.class))))
	public Response getNames(@PathParam("classpath") String classpath) {
		
		try {
			
			return Response.ok(commonService.classToNames(Class.forName(classpath))).build();
		} catch (Exception e) {
			throw new WebApplicationException(e);
			
		}
	}
	
}
