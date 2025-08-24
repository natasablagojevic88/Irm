package rs.irm.preview.utils;

import java.sql.Connection;

import jakarta.servlet.http.HttpServletRequest;
import rs.irm.administration.entity.Report;
import rs.irm.administration.enums.ReportType;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.impl.CommonServiceImpl;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.preview.dto.CardParameterDTO;
import rs.irm.preview.dto.CardResultDTO;

public class CreateCardReportResult implements ExecuteMethodWithReturn<CardResultDTO> {

	private Long reportId;
	private CardResultDTO inCardResultDTO;
	private HttpServletRequest httpServletRequest;

	private DatatableService datatableService;
	private CommonService commonService = new CommonServiceImpl();

	public CreateCardReportResult(Long reportId, CardResultDTO inCardResultDTO, HttpServletRequest httpServletRequest) {
		this.reportId = reportId;
		this.inCardResultDTO = inCardResultDTO;
		this.httpServletRequest = httpServletRequest;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
	}

	@Override
	public CardResultDTO execute(Connection connection) {
		Report report = this.datatableService.findByExistingId(reportId, Report.class, connection);

		if (!report.getType().equals(ReportType.CARD)) {
			return new CardResultDTO();
		}

		String query = report.getSqlQuery();

		for (CardParameterDTO parameters : this.inCardResultDTO.getParameters()) {
			query=query.replace("{"+parameters.getFilter()+"}",commonService.hasText(parameters.getParameter())?"'"+parameters.getParameter()+"'":"''");
		}

		CardResultDTO result = new CardResultDTO();
		result.setParameters(inCardResultDTO.getParameters());
		result.setResult(new CreateCardReport().createListOfResult(connection, query));

		return result;
	}

}
