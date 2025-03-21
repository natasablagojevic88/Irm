package rs.irm.preview.dto;

import java.util.LinkedHashMap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.irm.administration.dto.SqlResultDTO;
import rs.irm.administration.enums.ReportType;
import rs.irm.common.dto.Base64DownloadFileDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResultItemDTO {

	private Integer row;
	
	private Integer column;
	
	private Integer colspan;
	
	private Integer rowspan;
	
	private String name;
	
	private Long reportId;
	
	private ReportType reportType;
	
	private LinkedHashMap<Integer,String[]> parameters=new LinkedHashMap<>();
	
	private SqlResultDTO sqlResultDTO;
	
	private Base64DownloadFileDTO jasperBase64;
}
