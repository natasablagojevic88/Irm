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
@Table(name="defaultdashobard",
	uniqueConstraints = @UniqueConstraint(columnNames = { "appuser" },name = "defaultdashobard_appuser_unique")
		)
public class DefaultDashboard {

	@Id
	private Long id;
	
	@JoinColumn(name="appuser",nullable = false,foreignKey = @ForeignKey(name="fk_defaultdashobard_user"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private AppUser appUser;
	
	@JoinColumn(name="dashboard",nullable = false,foreignKey = @ForeignKey(name="fk_defaultdashobard_dashboard"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private Dashboard dashboard;
}
