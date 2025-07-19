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
import rs.irm.administration.dto.ReportColumnInfoDTO;
import rs.irm.administration.dto.ReportDTO;
import rs.irm.administration.dto.ReportJobDTO;
import rs.irm.administration.service.ReportService;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

@Path("/report")
@Tag(name = "rs.irm.administration.controller.ReportController", description = "report")
public class ReportController {

	@Inject
	private ReportService reportService;

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/table/{reportgroupid}")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	@RolesAllowed(value = { "admin" })
	public Response getTable(TableParameterDTO tableParameterDTO, 
			@PathParam("reportgroupid") Long reportGroupId)
	{
		return Response.ok(reportService.getTable(tableParameterDTO, reportGroupId)).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/tree/{modelid}")
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReportColumnInfoDTO.class))))
	@RolesAllowed(value = { "admin" })
	public Response getTreeField(
			@PathParam("modelid") Long modelId)
	{
		return Response.ok(reportService.getTreeField(modelId)).build();
	}
	

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = ReportDTO.class)))
	@RolesAllowed(value = { "admin" })
	public Response getUpdate(
			@Valid ReportDTO reportDTO
			){
		return Response.ok(reportService.getUpdate(reportDTO)).build();
	}
	
	@GET
	@Path("/info/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response", content = @Content(schema = @Schema(implementation = ReportDTO.class)))
	@RolesAllowed(value = { "admin" })
	public Response getInfo(
			@PathParam("id") Long id
			){
		return Response.ok(reportService.getInfo(id)).build();
	}
	

	@DELETE
	@Path("/{id}")
	@ApiResponse(responseCode = "204", description = "No content")
	@RolesAllowed(value = { "admin" })
	public Response getDelete(
			@PathParam("id") Long id
			){
		this.reportService.getDelete(id);
		return Response.noContent().build();
	}
	
	@GET
	@Path("/jasperrefresh")
	@ApiResponse(responseCode = "204", description = "No content")
	@RolesAllowed(value = { "admin" })
	public Response getJasperFileRefresh(
			){
		this.reportService.getJasperFileRefresh();
		return Response.noContent().build();
	}
	
	@POST
	@Path("/jobs/table/{reportid}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response",content = @Content(schema = @Schema(implementation = TableDataDTO.class)))
	@RolesAllowed(value = { "admin" })
	public Response getJobList(
			TableParameterDTO tableParameterDTO,
			@PathParam("reportid") Long reportId
			){
		
		return Response.ok(this.reportService.getTableJobs(tableParameterDTO, reportId)).build();
	}
	
	@GET
	@Path("/jobs/smtpbox")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response",content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComboboxDTO.class))))
	@RolesAllowed(value = { "admin" })
	public Response getSmtpBox(
			){
		
		return Response.ok(this.reportService.getSmtpBox()).build();
	}
	
	@POST
	@Path("/report-job")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiResponse(responseCode = "200", description = "Response",content = @Content(schema = @Schema(implementation = ReportJobDTO.class)))
	@RolesAllowed(value = { "admin" })
	public Response getJobUpdate(
			@Valid ReportJobDTO reportJobDTO
			){
		
		return Response.ok(this.reportService.getJobUpdate(reportJobDTO)).build();
	}
	
	@DELETE
	@Path("/report-job/{id}")
	@ApiResponse(responseCode = "204", description = "No content")
	@RolesAllowed(value = { "admin" })
	public Response getJobDelete(
			@PathParam("id") Long id
			){
		this.reportService.getJobDelete(id);
		return Response.noContent().build();
	}

}
