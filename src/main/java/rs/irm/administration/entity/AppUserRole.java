package rs.irm.administration.entity;

import jakarta.persistence.CascadeType;
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
@Table(name="appuser_role",
	uniqueConstraints = @UniqueConstraint(columnNames = { "appuser","role" },name = "appuser_role_unique"),
	indexes = {
			@Index(columnList = "appuser",name = "appuser_role_index1"),
			@Index(columnList = "role",name = "appuser_role_index2")
	}
		)
@NoArgsConstructor
@AllArgsConstructor
public class AppUserRole {
	
	@Id
	private Long id;
	
	@JoinColumn(name = "appuser",nullable = false,foreignKey = @ForeignKey(name="fk_appuser_role_appuser"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private AppUser appUser;
	
	@JoinColumn(nullable = false,foreignKey = @ForeignKey(name="fk_appuser_role_role"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private Role role;

}
