package rs.irm.administration.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Table(name="reportgroup_role",
	uniqueConstraints = @UniqueConstraint(columnNames = { "reportgroup","role" },name="reportgroup_role_unique")
		)
public class ReportGroupRole {
	
	@Id
	private Long id;
	
	@JoinColumn(name="reportgroup",nullable = false,foreignKey = @ForeignKey(name="fk_reportgroup_role_group"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private ReportGroup reportGroup;
	
	@JoinColumn(name="role",nullable = false,foreignKey = @ForeignKey(name="fk_reportgroup_role_role"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private Role role;

}
