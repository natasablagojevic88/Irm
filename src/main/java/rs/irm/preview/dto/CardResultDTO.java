package rs.irm.preview.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import lombok.Data;

@Data
public class CardResultDTO {
	
	private List<CardParameterDTO> parameters=new ArrayList<>();
	
	private List<LinkedHashMap<String, Object>> result = new ArrayList<>();

}
