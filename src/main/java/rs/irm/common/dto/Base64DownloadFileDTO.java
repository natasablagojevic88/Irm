package rs.irm.common.dto;

import lombok.Data;

@Data
public class Base64DownloadFileDTO {

	private String type;
	
	private String filename;
	
	private String base64String;
}
