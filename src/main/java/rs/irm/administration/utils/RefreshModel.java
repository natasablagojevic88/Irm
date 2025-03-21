package rs.irm.administration.utils;

import java.sql.Connection;

import jakarta.servlet.http.HttpServletRequest;
import rs.irm.administration.dto.ModelColumnDTO;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.administration.dto.ModelJasperReportDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethod;

public class RefreshModel implements ExecuteMethod {
	private HttpServletRequest httpServletRequest;

	private DatatableService datatableService;

	public RefreshModel(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
	}

	@Override
	public void execute(Connection connection) {
		ModelData.listModelDTOs = this.datatableService.findAll(new TableParameterDTO(), ModelDTO.class, connection);
		ModelData.listColumnDTOs = this.datatableService.findAll(new TableParameterDTO(), ModelColumnDTO.class,
				connection);
		ModelData.listModelJasperReportDTOs = this.datatableService.findAll(new TableParameterDTO(),
				ModelJasperReportDTO.class, connection);

	}

}
