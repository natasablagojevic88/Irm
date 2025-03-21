package rs.irm.preview.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import rs.irm.administration.dto.ModelJasperReportDTO;
import rs.irm.administration.utils.ModelData;
import rs.irm.common.enums.Language;
import rs.irm.common.exceptions.CommonException;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.utils.AppParameters;

public class PrintJasperReport implements ExecuteMethodWithReturn<ByteArrayOutputStream> {
	private Long jasperReportId;
	private Long id;
	private HttpServletRequest request;

	public PrintJasperReport(Long jasperReportId, Long id, HttpServletRequest request) {
		this.jasperReportId = jasperReportId;
		this.id = id;
		this.request = request;
	}

	@Override
	public ByteArrayOutputStream execute(Connection connection) {
		Language language = Language.valueOf(AppParameters.defaultlang);
		if (request != null) {
			if (request.getAttribute("language") != null) {
				language = Language.valueOf(request.getAttribute("language").toString());
			}
		}

		Locale locale = language.locale;
		ModelJasperReportDTO jasperReportDTO = ModelData.listModelJasperReportDTOs.stream()
				.filter(a -> a.getId().doubleValue() == jasperReportId.doubleValue()).findFirst().get();
		File jasperFileBase=new File(AppParameters.jasperfilepath);
		if(!(jasperFileBase.isDirectory()&&jasperFileBase.exists())) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "noFolder", AppParameters.jasperfilepath);
		}
		String reportPath = jasperFileBase.getAbsolutePath() + "/" + jasperReportDTO.getJasperFileName();
		File file=new File(reportPath);
		if(!file.exists()) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "noJasperFile", jasperReportDTO.getJasperFileName());
		}
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("id", id);
		parameters.put(JRParameter.REPORT_LOCALE, locale);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			JasperPrint jasperPrint = JasperFillManager.fillReport(file.getAbsolutePath(), parameters, connection);

			JasperExportManager.exportReportToPdfStream(jasperPrint, stream);
		} catch (JRException e) {
			throw new WebApplicationException(e);
		}
		return stream;
	}

}
