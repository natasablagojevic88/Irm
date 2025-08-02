package rs.irm.administration.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.Statement;

import org.modelmapper.ModelMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.administration.dto.ModelJasperReportDTO;
import rs.irm.administration.entity.ModelJasperReport;
import rs.irm.administration.enums.ModelType;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.exceptions.FieldRequiredException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.impl.CommonServiceImpl;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.utils.AppParameters;
import rs.irm.utils.DatabaseListenerJob;

public class ModelJasperReportUpdate implements ExecuteMethodWithReturn<ModelJasperReportDTO> {

	private HttpServletRequest httpServletRequest;
	private ModelJasperReportDTO modelJasperReportDTO;
	private DatatableService datatableService;
	private CommonService commonService;
	private ModelMapper modelMapper = new ModelMapper();

	public ModelJasperReportUpdate(HttpServletRequest httpServletRequest, ModelJasperReportDTO modelJasperReportDTO) {
		this.httpServletRequest = httpServletRequest;
		this.modelJasperReportDTO = modelJasperReportDTO;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
		this.commonService = new CommonServiceImpl(this.httpServletRequest);
	}

	@Override
	public ModelJasperReportDTO execute(Connection connection) {

		if (modelJasperReportDTO.getId() == 0 && (!commonService.hasText(modelJasperReportDTO.getFilePath()))) {
			throw new FieldRequiredException("ModelJasperReportDTO.jasperFileName");
		}

		ModelDTO modelDTO = ModelData.listModelDTOs.stream()
				.filter(a -> a.getId().doubleValue() == modelJasperReportDTO.getModelId().doubleValue()).findFirst()
				.get();
		if (!modelDTO.getType().equals(ModelType.TABLE.name())) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "noTableAllowed", null);
		}

		ModelJasperReport modelJasperReport = modelJasperReportDTO.getId() == 0 ? new ModelJasperReport()
				: this.datatableService.findByExistingId(modelJasperReportDTO.getId(), ModelJasperReport.class,
						connection);

		if (commonService.hasText(modelJasperReportDTO.getFilePath())) {
			File jasperFile = new File(modelJasperReportDTO.getFilePath());
			if (!jasperFile.exists()) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "noFile", jasperFile.getAbsolutePath());
			}

			try {
				modelJasperReport.setBytes(Files.readAllBytes(Paths.get(jasperFile.getAbsolutePath())));
			} catch (IOException e) {
				throw new WebApplicationException(e);
			}

		}

		modelMapper.map(modelJasperReportDTO, modelJasperReport);
		modelJasperReport = this.datatableService.save(modelJasperReport, connection);

		if (commonService.hasText(modelJasperReportDTO.getFilePath())) {

			File jasperFolder = new File(AppParameters.jasperfilepath);

			if (!(jasperFolder.exists()&&jasperFolder.isDirectory())) {
				jasperFolder.mkdirs();
			}
			File jasperFile = new File(modelJasperReportDTO.getFilePath());

			try {
				Files.copy(new ByteArrayInputStream(Files.readAllBytes(Paths.get(jasperFile.getAbsolutePath()))),
						Paths.get(jasperFolder.getAbsolutePath() + "/" + modelJasperReport.getJasperFileName()),
						StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				throw new WebApplicationException(e);
			}
		}

		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("NOTIFY " + DatabaseListenerJob.modeljasperreport_listener + ", 'Model jaspoer report changed';");
			statement.close();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

		return modelMapper.map(modelJasperReport, ModelJasperReportDTO.class);
	}

}
