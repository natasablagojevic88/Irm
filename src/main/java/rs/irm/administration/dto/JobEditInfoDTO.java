package rs.irm.administration.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.irm.common.dto.ComboboxDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobEditInfoDTO {

	private ReportJobDTO reportJobDTO;
	
	private String reportType;
	
	private LinkedHashMap<String, String> names=new LinkedHashMap<>();
	
	private List<ComboboxDTO> reportJobTypeList=new ArrayList<>();
	
	private List<ComboboxDTO> reportJobFileTypeList=new ArrayList<>();
	
	private List<ComboboxDTO> listSmtp=new ArrayList<>();
	
	private List<ComboboxDTO> reportJobMailType=new ArrayList<>();
}
