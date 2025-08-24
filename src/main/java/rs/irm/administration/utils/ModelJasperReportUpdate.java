package rs.irm.administration.utils;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.util.List;

import org.modelmapper.ModelMapper;

import jakarta.servlet.http.HttpServletRequest;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.administration.dto.ModelJasperReportDTO;
import rs.irm.administration.entity.ModelJasperReport;
import rs.irm.administration.enums.ModelType;
import rs.irm.common.entity.UploadFile;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.exceptions.FieldRequiredException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.impl.CommonServiceImpl;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.database.utils.TableFilter;

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
			
			TableParameterDTO tableParameterDTO=new TableParameterDTO();
			TableFilter tableFilter=new TableFilter();
			tableFilter.setField("uuid");
			tableFilter.setParameter1(modelJasperReportDTO.getFilePath());
			tableFilter.setSearchOperation(SearchOperation.equals);
			tableParameterDTO.getTableFilters().add(tableFilter);
			List<UploadFile> uploadFiles=this.datatableService.findAll(tableParameterDTO, UploadFile.class, connection);
			
			if(uploadFiles.isEmpty()) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "noFile", null);
			}
			
			UploadFile uploadFile=uploadFiles.get(0);

			if(commonService.getAppUser().getId().doubleValue()!=uploadFile.getAppUser().getId().doubleValue()) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "wrongUser", null);
			}

			modelJasperReport.setBytes(uploadFile.getBytes());
			
			this.datatableService.delete(uploadFile);

		}

		modelMapper.map(modelJasperReportDTO, modelJasperReport);
		modelJasperReport = this.datatableService.save(modelJasperReport, connection);

		return modelMapper.map(modelJasperReport, ModelJasperReportDTO.class);
	}

}
