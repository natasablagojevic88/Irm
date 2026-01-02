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
import rs.irm.administration.enums.ModelColumnType;
import rs.irm.database.enums.Text;

@Setter
@Getter
@Table(name="modelcolumn",
	uniqueConstraints = {@UniqueConstraint(columnNames = { "model","code" },name = "modelcolumn_code"),
						@UniqueConstraint(columnNames = { "parent" },name = "modelcolumn_parent")
		},
	indexes = {@Index(columnList = "model",name = "modelcolumn_model_index")}
		)
@NoArgsConstructor
@AllArgsConstructor
public class ModelColumn {

	public ModelColumn(Long id) {
		this.id = id;
	}

	@Id
	private Long id;
	
	@JoinColumn(nullable = false,foreignKey = @ForeignKey(name="fk_modelcolumn_model"))
	@ManyToOne(cascade = CascadeType.REMOVE)
	private Model model;
	
	@Column(nullable = false)
	private String code;
	
	@Column(nullable = false)
	private String name;
	
	@Column(name="columntype",nullable = false)
	private ModelColumnType columnType;
	
	@JoinColumn(name="codebookmodel",nullable = true,foreignKey = @ForeignKey(name="fk_modelcolumn_codebookmodel"))
	private Model codebookModel;
	
	@Column
	private Integer length;
	
	@Column
	private Integer precision;
	
	@Column(nullable = false)
	private Boolean nullable;
	
	@Column(name="rownumber",nullable = false)
	private Integer rowNumber;
	
	@Column(name="columnnumber",nullable = false)
	private Integer columnNumber;
	
	@Column(nullable = false)
	private Integer colspan;
	
	@JoinColumn(nullable = true, foreignKey = @ForeignKey(name="fk_modelcolumn_parent"))
	private ModelColumn parent;
	
	@Column(name="defaultvalue",nullable = true)
	@Text
	private String defaultValue;
	
	@Column(name="listofvalues",nullable = true)
	@Text
	private String listOfValues;
	
	@Column(name="showintable",nullable = false)
	private Boolean showInTable;
	
	@Column(nullable = false)
	private Boolean disabled;
	
	@Column(name="eventfunction")
	private String eventFunction;
}
