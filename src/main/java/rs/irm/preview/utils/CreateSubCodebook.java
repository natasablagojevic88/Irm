package rs.irm.preview.utils;

import java.sql.Connection;

import jakarta.servlet.http.HttpServletRequest;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.preview.dto.SubCodebookInfoDTO;
import rs.irm.preview.service.ModelQueryCreatorService;
import rs.irm.preview.service.impl.ModelQueryCreatorServiceImpl;

public class CreateSubCodebook implements ExecuteMethodWithReturn<SubCodebookInfoDTO> {

	private HttpServletRequest httpServletRequest;
	private Long modelId;
	private String codebook;
	private Long codebookValue;
	private ModelQueryCreatorService modelQueryCreatorService;

	public CreateSubCodebook(HttpServletRequest httpServletRequest, Long modelId, String codebook, Long codebookValue) {
		this.httpServletRequest = httpServletRequest;
		this.modelId = modelId;
		this.codebook = codebook;
		this.codebookValue = codebookValue;
		this.modelQueryCreatorService = new ModelQueryCreatorServiceImpl(this.httpServletRequest);
	}

	@Override
	public SubCodebookInfoDTO execute(Connection connection) {
		
		return modelQueryCreatorService.getSubCodebooks(this.modelId, this.codebook,this.codebookValue, connection);
	}

}
