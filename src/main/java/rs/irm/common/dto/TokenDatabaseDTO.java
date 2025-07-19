package rs.irm.common.dto;

import java.time.LocalDateTime;

import lombok.Data;
import rs.irm.common.entity.TokenDatabase;
import rs.irm.database.annotations.EntityClass;

@Data
@EntityClass(TokenDatabase.class)
public class TokenDatabaseDTO {

	private Long id;
	
	private String sessionToken;
	
	private LocalDateTime sessionEnd;
	
	private String refreshToken;
	
	private LocalDateTime refreshEnd;
	
	private Boolean active;
	
	private Long appUserId;
	
	private String appUserUsername;
	
	private Boolean appUserActive;
	
}
