package rs.irm.preview.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import rs.irm.administration.entity.ReportFilter;
import rs.irm.administration.entity.ReportJasper;
import rs.irm.common.enums.Language;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.impl.CommonServiceImpl;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.database.utils.TableFilter;
import rs.irm.preview.dto.TableReportParameterDTO;
import rs.irm.utils.AppParameters;

public class PreviewJasperReport implements ExecuteMethodWithReturn<Response> {

	private Long reportId;
	private TableReportParameterDTO tableReportParameterDTO;
	private HttpServletRequest httpServletRequest;
	private DatatableService datatableSevice;
	private CommonService commonService;

	public PreviewJasperReport(Long reportId, TableReportParameterDTO tableReportParameterDTO,
			HttpServletRequest httpServletRequest) {
		this.reportId = reportId;
		this.tableReportParameterDTO = tableReportParameterDTO;
		this.httpServletRequest = httpServletRequest;
		this.datatableSevice = new DatatableServiceImpl(this.httpServletRequest);
		this.commonService = new CommonServiceImpl(this.httpServletRequest);
	}

	@Override
	public Response execute(Connection connection) {

		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("report", SearchOperation.equals, String.valueOf(reportId), null));
		ReportJasper reportJasper = this.datatableSevice.findAll(tableParameterDTO, ReportJasper.class, connection)
				.get(0);

		Map<String, Object> parameters = createParameters(connection);

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		File reportBasePath=new File(AppParameters.jasperreportpath);
		
		if(!(reportBasePath.isDirectory()&&reportBasePath.exists())) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "noDirectory", AppParameters.jasperreportpath);
		}

		File reportPath = new File(reportBasePath.getAbsolutePath() + "/" + reportJasper.getName());
		if (!reportPath.exists()) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "noJasperFile", reportJasper.getName());
		}
		try {
			JasperPrint jasperPrint = JasperFillManager.fillReport(reportPath.getAbsolutePath(), parameters,
					connection);

			JasperExportManager.exportReportToPdfStream(jasperPrint, stream);
		} catch (JRException e) {
			throw new WebApplicationException(e);
		}

		Response response = Response.status(HttpURLConnection.HTTP_OK)
				.header("Content-Disposition", "attachment;filename=print.pdf")
				.header("Content-Type", "application/pdf").header("filename", "print.pdf")
				.header("Access-Control-Expose-Headers", "filename").entity(stream.toByteArray()).build();
		return response;
	}

	private Map<String, Object> createParameters(Connection connection) {
		Map<String, Object> map = new HashMap<>();

		Language language = Language.valueOf(AppParameters.defaultlang);
		if (httpServletRequest != null) {
			if (httpServletRequest.getAttribute("language") != null) {
				language = Language.valueOf(httpServletRequest.getAttribute("language").toString());
			}
		}

		Locale locale = language.locale;
		map.put(JRParameter.REPORT_LOCALE, locale);

		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("report", SearchOperation.equals, String.valueOf(reportId), null));
		List<ReportFilter> reportFilters = this.datatableSevice.findAll(tableParameterDTO, ReportFilter.class,
				connection);

		for (ReportFilter reportFilter : reportFilters) {
			if (!tableReportParameterDTO.getParameters().containsKey(reportFilter.getId().intValue())) {
				continue;
			}

			String parameter = this.tableReportParameterDTO.getParameters().get(reportFilter.getId().intValue())[0];

			if (!commonService.hasText(parameter)) {
				continue;
			}

			String type = reportFilter.getFieldType();

			switch (type) {
			case "Integer": {
				map.put(reportFilter.getCode(), Integer.valueOf(parameter));
				break;
			}
			case "Long": {
				map.put(reportFilter.getCode(), Long.valueOf(parameter));
				break;
			}
			case "BigDecimal": {
				NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
				numberFormat.setGroupingUsed(true);
				numberFormat.setMaximumFractionDigits(10);
				Number number = null;
				try {
					number = numberFormat.parse(parameter);
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}
				map.put(reportFilter.getCode(), BigDecimal.valueOf(number.doubleValue()));
				break;
			}
			case "LocalDate": {
				LocalDate localDate=LocalDate.parse(parameter);
				;
				map.put(reportFilter.getCode(), java.sql.Date.valueOf(localDate));
				break;
			}
			case "LocalDateTime": {
				LocalDateTime localDate=LocalDateTime.parse(parameter);
				;
				map.put(reportFilter.getCode(), java.sql.Timestamp.valueOf(localDate));
				break;
			}
			case "Boolean": {
				
				map.put(reportFilter.getCode(), Boolean.valueOf(parameter));
				break;
			}
			default: {
				map.put(reportFilter.getCode(), parameter);
			}
			}
		}

		return map;
	}

}
