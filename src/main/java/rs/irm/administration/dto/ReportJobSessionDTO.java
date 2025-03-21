package rs.irm.administration.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.irm.administration.enums.JobStatement;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportJobSessionDTO {

	private JobStatement state;
	
	private String errorMessage;
	
	private LocalDateTime executeTime;
	
	private String fileName;
	
	private String fileType;
	
	private byte[] fileBytes;
}
