package rs.irm.administration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Table(name="appuser",
	uniqueConstraints = @UniqueConstraint(columnNames = { "username" },name = "appuser_username_unique")
		)
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

	public AppUser(Long id) {
		this.id = id;
	}

	@Id
	private Long id;
	
	@Column(nullable = false)
	private String username;
	
	@Column(nullable = false)
	private String password;
	
	@Column(nullable = false)
	private String name;
	
	@Column(nullable = false)
	private Boolean active;
	
	@Column
	private String email;
	
	
}
