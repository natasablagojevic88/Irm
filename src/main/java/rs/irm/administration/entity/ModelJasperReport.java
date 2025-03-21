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
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Table(name="modeljasperreport",
	uniqueConstraints = {@UniqueConstraint(columnNames = { "jasperfilename" },name="jasperreport_jaspername_unique"),
			@UniqueConstraint(columnNames = { "model","name" },name = "modeljasperreport_unque1")
	},
	indexes = @Index(columnList = "model",name = "modeljasperreport_index1")
		)
public class ModelJasperReport {

	@Id
	private Long id;
	
	@JoinColumn(name="model",nullable = false,foreignKey = @ForeignKey(name="fk_modeljasperreport_model"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private Model model;
	
	@Column(nullable = false)
	private String name;
	
	@Column(name="jasperfilename",nullable = false)
	private String jasperFileName;
	
	@Column(nullable = false)
	private byte[] bytes;
}
