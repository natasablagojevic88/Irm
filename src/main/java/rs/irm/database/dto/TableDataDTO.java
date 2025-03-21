package rs.irm.database.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import lombok.Data;
import rs.irm.administration.dto.ModelColumnDTO;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.database.utils.ColumnData;
import rs.irm.database.utils.EnumList;
import rs.irm.preview.utils.TableButton;

@Data
public class TableDataDTO<C> {

	private String title;
	private String table;
	private Integer totalPages;
	private Long totalElements;
	private Integer tableWidth;
	private Boolean hasTotal=false;
	private ModelDTO model;
	
	private List<ColumnData> columns = new ArrayList<>();
	private LinkedHashMap<String, String> names=new LinkedHashMap<>();
	private LinkedHashMap<String, BigDecimal> totals=new LinkedHashMap<>();
	private List<EnumList> enums=new ArrayList<>();
	private List<ModelColumnDTO> fields=new ArrayList<>();
	private List<TableButton> subtables=new ArrayList<>();
	private LinkedHashMap<String, Boolean> rights;
	private List<C> list;
}
