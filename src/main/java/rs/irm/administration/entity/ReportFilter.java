package rs.irm.administration.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.irm.administration.enums.SqlMetric;
import rs.irm.database.enums.SearchOperation;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="reportfilter",
	indexes = @Index(columnList = "report",name = "reportfilter_report_index")
		)
public class ReportFilter {
	
	@Id
	private Long id;
	
	@JoinColumn(nullable = false, foreignKey = @ForeignKey(name="fk_reportfilter_report"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private Report report;
	
	@Column(name="code",nullable = false)
	private String code;
	
	@Column(name="customname")
	private String customName;
	
	@JoinColumn(name="modelcolumn",nullable = true,foreignKey = @ForeignKey(name="fk_reportfilter_modelcolumn"))
	private ModelColumn modelColumn;
	
	@Column(name="searchoperation")
	private SearchOperation searchOperation;
	
	@Column(name="sqlmetric")
	private SqlMetric sqlMetric;
	
	@Column(name="defaultvalue1",length = 4000)
	private String defaultValue1;
	
	@Column(name="defaultvalue2",length = 4000)
	private String defaultValue2;
	
	
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
