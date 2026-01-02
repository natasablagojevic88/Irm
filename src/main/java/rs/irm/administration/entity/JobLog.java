package rs.irm.administration.entity;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.irm.administration.enums.JobStatement;
import rs.irm.database.enums.Text;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="joblog")
public class JobLog {

	@Id
	private Long id=0L;
	
	@JoinColumn(name="reportjob",nullable = false,foreignKey = @ForeignKey(name="fk_joblog_reportjob"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private ReportJob reportJob;
	
	@Column(name="starttime",nullable = false)
	
	private LocalDateTime startTime=LocalDateTime.now();
	
	@Column(nullable = false)
	private JobStatement status;
	
	@Column(name="endtime")
	private LocalDateTime endTime;
	
	@Column
	@Text
	private String error;
	
}
