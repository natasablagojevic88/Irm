package rs.irm.administration.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SqlEditorInfo {

	private String code;
	private String icon;
	private String name;
	
	private List<SqlColumnInfo> columns=new ArrayList<>();
}
