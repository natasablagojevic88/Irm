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
import rs.irm.administration.enums.GraphType;
import rs.irm.administration.enums.ReportType;
import rs.irm.database.enums.Text;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="report",
	uniqueConstraints = {
			@UniqueConstraint(columnNames = { "reportgroup","code" },name = "report_unique1"),
			@UniqueConstraint(columnNames = { "reportgroup","name" },name = "report_unique2")
	}
		)
public class Report {

	public Report(Long id) {
		this.id = id;
	}

	@Id
	private Long id;
	
	@JoinColumn(name="reportgroup",nullable = false,foreignKey = @ForeignKey(name="fk_reportgroup_report"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private ReportGroup reportGroup;
	
	@Column(nullable = false)
	private String code;
	
	@Column(nullable = false)
	private String name;
	
	@Column(nullable = false)
	private ReportType type;
	
	@JoinColumn(foreignKey = @ForeignKey(name="fk_report_model"))
	private Model model;
	
	@Column(name="graphtype")
	private GraphType graphType;
	
	@Column(name="sqlquery")
	@Text
	private String sqlQuery;
	
}
