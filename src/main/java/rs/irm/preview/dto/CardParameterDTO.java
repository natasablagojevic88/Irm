package rs.irm.preview.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import rs.irm.common.dto.ComboboxDTO;

@Data
public class CardParameterDTO {

	private String filter;
	
	private String name;
	
	private List<ComboboxDTO> values=new ArrayList<>();
	
	private String parameter;
}
