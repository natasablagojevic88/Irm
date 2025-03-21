package rs.irm.preview.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartOptionsDTO {

	private LinkedHashMap<String, Object> title=new LinkedHashMap<>();
	
	private Boolean animationEnabled;
	
	private String theme;
	
	private LinkedHashMap<String, Object> axisY =new LinkedHashMap<>();
	
	private List<ChartOptionsDataDTO> data=new ArrayList<>();
}
