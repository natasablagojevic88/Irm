package rs.irm.database.utils;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import rs.irm.common.dto.ComboboxDTO;

@Data
public class EnumList {

	private String code;
	private List<ComboboxDTO> list=new ArrayList<>();
}
