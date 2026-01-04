package rs.irm.administration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.irm.administration.entity.ReportJob;
import rs.irm.administration.enums.ReportJobFileType;
import rs.irm.administration.enums.ReportJobMailType;
import rs.irm.administration.enums.ReportJobType;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.annotations.EnumField;
import rs.irm.database.annotations.TableHide;
import rs.irm.database.enums.InitSort;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityClass(ReportJob.class)
public class ReportJobDTO {

	@NotNull
	@TableHide
	private Long id;
	
	@InitSort
	@NotNull
	private String name;
	
	@TableHide
	private Long reportId;
	
	@TableHide
	private String reportName;
	
	@NotNull
	private String cron;
	
	@EnumField(ReportJobType.class)
	@NotNull
	private String type;
	
	@EnumField(ReportJobFileType.class)
	@TableHide
	private String fileType;
	
	@TableHide
	private String fileName;
	
	@NotNull
	@TableHide
	private Boolean addTimeStamp;
	
	@TableHide
	private Long smtpServerId;
	
	@TableHide
	@Email
	private String mailTo;
	
	@TableHide
	private String mailSubject;
	
	@TableHide
	private String mailBody;
	
	@EnumField(ReportJobMailType.class)
	@TableHide
	private String mailType;
	
	@TableHide
	private String csvDelimiter;
	
	@NotNull
	@TableHide
	private Boolean csvHasHeader;
	
	@TableHide
	private String filePath;
	
	@TableHide
	@Email
	private String mailError;
	
	@NotNull
	private Boolean active;
	
	@NotNull
	@TableHide
	private Boolean conditional;
	
	@TableHide
	private Long modelId;
	
	@TableHide
	private String parentQuery;
	
	@TableHide
	private Long javaClassId;
}
