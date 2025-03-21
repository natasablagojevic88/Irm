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
@Table(name="role",
	uniqueConstraints = @UniqueConstraint(columnNames = { "code" },name = "role_code_unique")
		)
@NoArgsConstructor
@AllArgsConstructor
public class Role {

	public Role(Long id) {
		this.id = id;
	}

	@Id
	private Long id;
	
	@Column(nullable = false)
	private String code;
	
	@Column
	private String description;

}
