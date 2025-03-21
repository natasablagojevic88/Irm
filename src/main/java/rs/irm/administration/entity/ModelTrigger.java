package rs.irm.administration.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
import rs.irm.administration.enums.TriggerEvent;
import rs.irm.administration.enums.TriggerTime;

@Setter
@Getter
@Table(name="modeltrigger",
	uniqueConstraints = @UniqueConstraint(columnNames = { "model","code" },name = "modeltrigger_unique1_1" )
		)
@NoArgsConstructor
@AllArgsConstructor
public class ModelTrigger {
	
	@Id
	private Long id;
	
	@JoinColumn(nullable = false,foreignKey = @ForeignKey(name="fk_modeltrigger_model"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private Model model;
	
	@Column(nullable = false)
	private String code;
	
	@Column(nullable = false)
	private String name;
	
	@Column(name="triggertime", nullable = false)
	private TriggerTime triggerTime;
	
	@Column(name="triggerevent", nullable = false)
	private TriggerEvent triggerEvent;
	
	@Column
	private String condition;
	
	@Column(name = "triggerfunction",nullable = false)
	private String triggerFunction;
}
