package rs.irm.administration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SqlColumnInfo {

	private String code;
	private String name;
	private String codebookName;
	private String codebookIcon;
}
