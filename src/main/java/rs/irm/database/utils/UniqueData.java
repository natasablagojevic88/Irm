package rs.irm.database.utils;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class UniqueData {
	
	private String name;
	
	private List<String> columns=new ArrayList<>();

}
