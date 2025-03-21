package rs.irm.administration.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import lombok.Data;
import rs.irm.preview.dto.ChartOptionsDTO;

@Data
public class SqlResultDTO {

	private Long totalItems;
	private Integer numberOfPages;
	private Boolean hasTotal = false;
	private Boolean queryWithFetch = false;
	private String sqlQuery;
	
	private ChartOptionsDTO chartOptions =new ChartOptionsDTO();

	private List<SqlResultColumnDTO> columns = new ArrayList<>();
	private List<LinkedHashMap<Integer, Object>> list = new ArrayList<>();
	private LinkedHashMap<Integer, Object> totalsColumn = new LinkedHashMap<>();

}
