package rs.irm.administration.utils;

import java.sql.Connection;
import java.sql.Statement;

import org.modelmapper.ModelMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.ReportGroupDTO;
import rs.irm.administration.entity.ReportGroup;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.utils.DatabaseListenerJob;

public class ReportGroupUpdate implements ExecuteMethodWithReturn<ReportGroupDTO> {
	
	private HttpServletRequest httpServletRequest;
	private ReportGroupDTO reportGroupDTO;
	private DatatableService datatableService;
	private ModelMapper modelMapper=new ModelMapper();

	public ReportGroupUpdate(ReportGroupDTO reportGroupDTO,HttpServletRequest httpServletRequest) {
		this.httpServletRequest=httpServletRequest;
		this.reportGroupDTO = reportGroupDTO;
		this.datatableService=new DatatableServiceImpl(this.httpServletRequest);
	}

	@Override
	public ReportGroupDTO execute(Connection connection) {
		ReportGroup reportGroup = reportGroupDTO.getId() == 0 ? new ReportGroup()
				: this.datatableService.findByExistingId(reportGroupDTO.getId(), ReportGroup.class,connection);

		modelMapper.map(reportGroupDTO, reportGroup);

		reportGroup = this.datatableService.save(reportGroup,connection);
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("NOTIFY " + DatabaseListenerJob.reportgroup_listener + ", 'Report group changed';");
			statement.executeUpdate("NOTIFY " + DatabaseListenerJob.reportgrouprole_listener + ", 'Report group role changed';");
			statement.close();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

		return modelMapper.map(reportGroup, ReportGroupDTO.class);
	}

}
