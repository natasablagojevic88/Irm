package rs.irm.preview.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import rs.irm.administration.enums.ReportType;

@Data
public class ReportPreviewInfoDTO {

	private String name;
	
	private ReportType reportType;
	
	private List<ReportParameterDTO> parameters=new ArrayList<>();
}
