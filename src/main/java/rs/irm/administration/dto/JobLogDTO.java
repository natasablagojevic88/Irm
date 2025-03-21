package rs.irm.administration.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.irm.administration.entity.JobLog;
import rs.irm.administration.enums.JobStatement;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.annotations.EnumField;
import rs.irm.database.annotations.TableHide;
import rs.irm.database.enums.InitSort;
import rs.irm.database.enums.SortDirection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityClass(JobLog.class)
public class JobLogDTO {
	
	@TableHide
	private Long id;
	
	@TableHide
	private Long reportJobId;
	
	@InitSort(sortDirection = SortDirection.DESC)
	private LocalDateTime startTime;
	
	@EnumField(JobStatement.class)
	private String status;
	
	private LocalDateTime endTime;
	
	private String error;

}
