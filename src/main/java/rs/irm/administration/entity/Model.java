package rs.irm.administration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.irm.administration.enums.ModelType;

@Table(name="model",
	uniqueConstraints = {@UniqueConstraint(columnNames = { "code" },name = "model_code_unique"),
			@UniqueConstraint(columnNames = { "name" },name = "model_name_unique")
	}
		)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Model {
	public Model(Long id) {
		this.id = id;
	}

	@Id
	private Long id;
	
	@Column(nullable = false)
	private String code;
	
	@Column(nullable = false)
	private String name;
	
	@Column(nullable = false)
	private ModelType type;
	
	@Column(name="numericcode",nullable = false)
	private Boolean numericCode;
	
	@Column(nullable = false)
	private String icon;
	
	@JoinColumn(foreignKey = @ForeignKey(name="fk_model_parent"))
	private Model parent;
	
	@JoinColumn(name="previewrole",nullable = false,foreignKey = @ForeignKey(name="fk_model_previewrole"))
	private Role previewRole;
	
	@JoinColumn(name="updaterole",nullable = false,foreignKey = @ForeignKey(name="fk_model_updaterole"))
	private Role updateRole;
	
	@JoinColumn(name="lockrole",nullable = false,foreignKey = @ForeignKey(name="fk_model_lockrole"))
	private Role lockRole;
	
	@JoinColumn(name="unlockrole",nullable = false,foreignKey = @ForeignKey(name="fk_model_unlockrole"))
	private Role unlockRole;
	
	@Column(name="dialogwidth",nullable = true)
	private Integer dialogWidth;
	
	@Column(name="columnsnumber",nullable = true)
	private Integer columnsNumber;
	
	@Column(name="tablewidth",nullable = true)
	private Integer tableWidth;
}
