package rs.irm.common.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.irm.database.enums.Text;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="app_logs")
public class AppLogs {
	
	@Id
	private Long id;
	
	@Column(name="event_date",nullable = false)
	private LocalDateTime eventDate;
	
	@Column(nullable = false)
	private String level;
	
	@Column(nullable = false)
	private String logger;
	
	@Column(nullable = false)
	private String thread;
	
	@Column
	@Text
	private String message;
	
	@Column
	@Text
	private String trace;

}
