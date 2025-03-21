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
@NoArgsConstructor
@AllArgsConstructor
@Table(name="dashboard",
	uniqueConstraints = @UniqueConstraint(columnNames = { "name" },name = "dashboard_name_unique")
		)
public class Dashboard {

	public Dashboard(Long id) {
		this.id = id;
	}

	@Id
	private Long id;
	
	@Column(nullable = false)
	private String name;
	
	@Column(nullable = false)
	private Integer rownumber;
	
	@Column(nullable = false)
	private Integer columnnumber;
}
