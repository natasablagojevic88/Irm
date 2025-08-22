package rs.irm.administration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.irm.database.enums.Text;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="javaclass",
	uniqueConstraints = @UniqueConstraint(columnNames = { "name" },name = "javaclass_name_unique")
		)
public class JavaClass {
	
	@Id
	private Long id;

	@Column(nullable = false)
	private String name;
	
	@Column(name="classtext",nullable = false)
	@Text
	private String classText;
	
	@Column(name="classname",nullable = false)
	private String className;
	
	@Column(name="methodname",nullable = false)
	private String methodName;
	
	
}
