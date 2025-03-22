package rs.irm.administration.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
@NoArgsConstructor
@AllArgsConstructor
@Table(name="modelprocedure",
	uniqueConstraints = @UniqueConstraint(columnNames = { "model","name" },name="modelprocedure_unique1"),
	indexes = @Index(columnList = "model",name="modelprocedure_model_index")
		)
public class ModelProcedure {

	@Id
	public Long id;
	
	@JoinColumn(nullable = false,foreignKey = @ForeignKey(name="fk_modelprocedure_model"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private Model model;
	
	@Column(nullable = false)
	private String name;
	
	@Column(name="sqlprocedure", nullable = false)
	private String sqlProcedure;
}
