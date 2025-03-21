package rs.irm.administration.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Table(name="sqlexecutetrack")
public class SqlExecuteTrack {

	@Id
	private Long id=0L;
	
	@JoinColumn(name="appuser",nullable = true,foreignKey = @ForeignKey(name="fk_sqlexecutetrack_appuser"))
	private AppUser appUser;
	
	@Column
	private String ipaddress;
	
	@Column(name="sqlquery",nullable = false,length = 4000)
	private String sqlQuery;
	
	@Column(name="updatednumberofrows",nullable = false)
	private Long updatedNumberOfRows;
	
	@Column(name="executetime",nullable = false)
	private LocalDateTime executeTime=LocalDateTime.now();
}
