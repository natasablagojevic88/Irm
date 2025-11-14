package rs.irm.administration.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import rs.irm.administration.enums.TriggerEvent;

@Table(name="modeljavaclass")
@Setter
@Getter
public class ModelJavaClass {

	@Id
	private Long id;
	
	@JoinColumn(name="model",nullable = false,foreignKey = @ForeignKey(name="fk_modeljavaclass_model"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private Model model;
	
	@JoinColumn(name="javaclass",nullable = false,foreignKey = @ForeignKey(name="fk_modeljavaclass_javaclass"))
	private JavaClass javaClass;
	
	@Column(nullable = false)
	private TriggerEvent event;
}
