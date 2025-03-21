package rs.irm.administration.entity;

import jakarta.persistence.CascadeType;
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

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="dashboard_role",
	uniqueConstraints = @UniqueConstraint(columnNames = { "dashboard","role" },name = "dashboard_role_unique" )
		)
public class DashboardRole {

	@Id
	private Long id;
	
	@JoinColumn(nullable = false,foreignKey = @ForeignKey(name="fk_dashboard_role_dashboard"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private Dashboard dashboard;
	
	@JoinColumn(nullable = false,foreignKey = @ForeignKey(name="fk_dashboard_role_role"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private Role role;
}
