package rs.irm.administration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Table(name="reportgroup",
	uniqueConstraints = {
			 @UniqueConstraint(columnNames = { "code" },name="reportgroup_unique1"),
			 @UniqueConstraint(columnNames = { "name" },name="reportgroup_unique2")
	}
		)
public class ReportGroup {
	
	@Id
	private Long id;
	
	@Column(nullable = false)
	private String code;
	
	@Column(nullable = false)
	private String name;

}
