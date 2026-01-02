package rs.irm.administration.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.irm.administration.enums.ReportJobFileType;
import rs.irm.administration.enums.ReportJobMailType;
import rs.irm.administration.enums.ReportJobType;
import rs.irm.database.enums.Text;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="reportjob",
	uniqueConstraints = {
			@UniqueConstraint(columnNames = { "name" },name = "reportjob_unique1"),
			@UniqueConstraint(columnNames = { "report","cron" },name = "reportjob_unique2")
	}
		)
public class ReportJob {
	public ReportJob(Long id) {
		this.id = id;
	}

	@Id
	private Long id;
	
	@Column(nullable = false)
	private String name;
	
	@JoinColumn(foreignKey = @ForeignKey(name="fk_reportjob_report"))
	private Report report;
	
	@Column(nullable = false)
	private String cron;
	
	@Column(nullable = false)
	private ReportJobType type;
	
	@Column(name="filetype")
	private ReportJobFileType fileType;
	
	@Column(name="filename")
	private String fileName;
	
	@Column(name="addtimestamp",nullable = false)
	private Boolean addTimeStamp;
	
	@JoinColumn(name="smtpserver",foreignKey = @ForeignKey(name="fk_reportjob_smtpserver"))
	private SmtpServer smtpServer;
	
	@Column(name="mailto")
	private String mailTo;
	
	@Column(name="mailsubject")
	private String mailSubject;
	
	@Column(name="mailbody")
	@Text
	private String mailBody;
	
	@Column(name="mailtype")
	private ReportJobMailType mailType;
	
	@Column(name="csvdelimiter")
	private String csvDelimiter;
	
	@Column(name="csvhasheader",nullable = false)
	private Boolean csvHasHeader;
	
	@Column(name="filepath")
	private String filePath;
	
	@Column(name="mailerror")
	private String mailError;

	@Column(nullable = false)
	private Boolean active;

	@Column(nullable = false)
	private Boolean conditional;

	@JoinColumn(foreignKey = @ForeignKey(name="fk_reportjob_model"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private Model model;
	
	@Column(name="parentquery")
	private String parentQuery;
}
