package rs.irm.administration.controller;

import java.io.ByteArrayOutputStream;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import rs.irm.administration.dto.JobEditInfoDTO;
import rs.irm.administration.dto.JobInfoDTO;
import rs.irm.administration.service.JobInfoService;
import rs.irm.common.dto.Base64DownloadFileDTO;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

@Path("/jobs")
@Tag(name="rs.irm.administration.controller.JobInfoController",description = "job")
public class JobInfoController {
	
	@Inject 
	private JobInfoService jobInfoService;

	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(value = { "admin" })
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	public Response getList() {
		return Response.ok(jobInfoService.getList()).build();
	}
	
	@GET
	@Path("/execute/{id}")
	@RolesAllowed(value = { "admin" })
	@ApiResponse(responseCode = "204",description = "No content")
	public Response getExecute(
			@PathParam("id") Long id
			) {
		this.jobInfoService.getExecute(id);
		return Response.noContent().build();
	}
	
	@GET
	@Path("/download/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@RolesAllowed(value = { "admin" })
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = ByteArrayOutputStream.class)))
	public Response getDownload(
			@PathParam("id") Long id
			) {
		
		return this.jobInfoService.getDownload(id);
	}
	
	@GET
	@Path("/download/{id}/base64")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(value = { "admin" })
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = Base64DownloadFileDTO.class)))
	public Response getDownloadBase64(
			@PathParam("id") Long id
			) {
		
		return Response.ok(this.jobInfoService.getDownloadBase64(id)).build();
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(value = { "admin" })
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = JobInfoDTO.class)))
	public Response getJobInfo(
			@PathParam("id") Long id
			) {
		
		return Response.ok(this.jobInfoService.getJobInfo(id)).build();
	}
	
	@GET
	@Path("/jobeditinfo/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(value = { "admin" })
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = JobEditInfoDTO.class)))
	public Response getJobEditInfo(
			@PathParam("id") Long id
			) {
		
		return Response.ok(this.jobInfoService.getJobEditInfo(id)).build();
	}
	
	@POST
	@Path("/logs/{reportjobid}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed(value = { "admin" })
	@ApiResponse(responseCode = "200",description = "Response",content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	public Response getLogs(
			TableParameterDTO tableParameterDTO,
			@PathParam("reportjobid") Long reportJobId
			) {
		return Response.ok(jobInfoService.getLogs(tableParameterDTO, reportJobId)).build();
	}
}
