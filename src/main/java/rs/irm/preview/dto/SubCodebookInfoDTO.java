package rs.irm.preview.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import rs.irm.common.dto.ComboboxDTO;

@Data
public class SubCodebookInfoDTO {
	
	private String codebook;
	
	private List<ComboboxDTO> list=new ArrayList<>();

}
