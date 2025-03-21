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
public class ChartOptionsDataDTO {

	String type;
	
	Boolean showInLegend;
	
	String name;
	
	String indexLabelFontColor;
	
	String indexLabel;
	
	List<LinkedHashMap<Object, Object>> dataPoints=new ArrayList<>();
	
}
