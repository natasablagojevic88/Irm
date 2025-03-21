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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.irm.administration.enums.SqlMetric;
import rs.irm.database.enums.SortDirection;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="reportcolumn",
	uniqueConstraints = @UniqueConstraint(columnNames = { "report","ordernum" },name="reportcolumn_unique1"),
	indexes = @Index(columnList = "report",name = "reportcolumn_report_index")
		)
public class ReportColumn {
	
	@Id
	private Long id;
	
	@JoinColumn(nullable = false, foreignKey = @ForeignKey(name="reportcolumn_report"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private Report report;
	
	@Column(name="code",nullable = false)
	private String code;
	
	@Column(name="customname")
	private String customName;
	
	@JoinColumn(name="modelcolumn",nullable = true,foreignKey = @ForeignKey(name="fk_reportcolumn_modelcolumn"))
	private ModelColumn modelColumn;
	
	@Column(name="sqlmetric")
	private SqlMetric sqlMetric;
	
	@Column
	private Integer ordernum;
	
	@Column(name="sortdirection")
	private SortDirection sortDirection;
	
	@Column(name="fieldname",nullable = false)
	private String fieldName;
	
	@Column(name="fieldtype",nullable = false)
	private String fieldType;
	
	@Column(name="leftjoinpath")
	private String leftJoinPath;
	
	@Column(name="lefttabledatas",length = 4000)
	private String leftTableDatas;
	
	@Column(name="columnlist",length = 4000)
	private String columnList;
	
	@Column(name="tablelist",length = 4000)
	private String tableList;

}
