package rs.irm.administration.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import rs.irm.common.dto.ComboboxDTO;

@Data
public class CheckParentDTO {

	private Boolean needParent;
	
	private List<ComboboxDTO> list=new ArrayList<>();
}
