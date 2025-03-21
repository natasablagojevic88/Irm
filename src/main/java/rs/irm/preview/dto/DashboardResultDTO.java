package rs.irm.preview.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class DashboardResultDTO {

	private Long id;
	private String title;
	private Integer numberOfColumns;
	private Integer numberOfRows;
	private Boolean setDefault;
	
	private List<DashboardResultItemDTO> items=new ArrayList<>();
}
