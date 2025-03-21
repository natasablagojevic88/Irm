package rs.irm.administration.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class MenuDTO {
	
	private String name;
	
	private String icon;
	
	private String url;
	
	private Long id;
	
	private List<MenuDTO> children=new ArrayList<>();

}
