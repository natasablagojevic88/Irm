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

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="dashboarditem",
	uniqueConstraints = @UniqueConstraint(columnNames = { "dashboard","rownum","columnnum" },name = "dashboarditem_unique1"),
	indexes = @Index(columnList = "dashboard",name = "dashboarditem_dashboard_index")
		)
public class DashboardItem {

	@Id
	private Long id;
	
	@JoinColumn(nullable = false,foreignKey = @ForeignKey(name="fk_dashboarditem_dashboard"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private Dashboard dashboard;
	
	@Column(name="rownum",nullable = false)
	private Integer row;
	
	@Column(name="columnnum", nullable = false)
	private Integer column;
	
	@Column(nullable = false)
	private Integer colspan;
	
	@Column(nullable = false)
	private Integer rowspan;
	
	@JoinColumn(nullable = false,foreignKey = @ForeignKey(name="fk_dashboarditem_report"))
	private Report report;
}
