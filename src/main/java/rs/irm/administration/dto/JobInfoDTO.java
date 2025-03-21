package rs.irm.administration.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.irm.administration.enums.JobStatement;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobInfoDTO {

	private Long id;
	
	private String name;
	
	private String reportName;
	
	private String type;
	
	private LocalDateTime previousTime;
	
	private LocalDateTime nextTime;
	
	private Boolean active;
	
	private JobStatement status;
	
	private String errorMessage;
	
	private String fileName;
	
	
}
