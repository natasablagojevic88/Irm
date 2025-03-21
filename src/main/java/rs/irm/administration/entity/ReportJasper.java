package rs.irm.administration.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Table(name="reportjasper",
	uniqueConstraints = {@UniqueConstraint(columnNames = { "name" },name = "reportjasper_name_unqiue"),
			@UniqueConstraint(columnNames = { "report" },name = "reportjasper_report_unqiue")
	},
	indexes = @Index(columnList = "report",name = "reportjasper_report_unique" )
		)
public class ReportJasper {

	@Id
	private Long id;
	
	@JoinColumn(nullable = false,foreignKey = @ForeignKey(name="reportjasper_report"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private Report report;
	
	@Column(nullable = false)
	private String name;
	
	@Column(nullable = false)
	private byte[] bytes;
}
