package rs.irm.common.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import rs.irm.administration.entity.AppUser;

@Table(name="tokendatabase",
   uniqueConstraints = {
		   @UniqueConstraint(columnNames = { "sessiontoken" },name="tokendatabase_unique_session"),
		   @UniqueConstraint(columnNames = { "refreshtoken" },name="tokendatabase_unique_refresh")
   }
		)
@Getter
@Setter
public class TokenDatabase {
	
	@Id
	private Long id;
	
	@Column(name="sessiontoken",nullable = false)
	private String sessionToken;
	
	@Column(name="sessionend",nullable = false)
	private LocalDateTime sessionEnd;
	
	@Column(name="refreshtoken",nullable = false)
	private String refreshToken;
	
	@Column(name="refreshend",nullable = false)
	private LocalDateTime refreshEnd;
	
	@Column(nullable = false)
	private Boolean active;
	
	@JoinColumn(name="appuser",nullable = false,foreignKey = @ForeignKey(name="fk_tokendatabase_appuser"))
	private AppUser appUser;
	

}
