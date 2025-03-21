package rs.irm.common.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.irm.administration.entity.AppUser;
import rs.irm.common.enums.TrackAction;

@Setter
@Getter
@Table(name="track",
	indexes = @Index(columnList = "tablename,dataid",name="track_index1")
		)
@AllArgsConstructor
@NoArgsConstructor
public class Track {

	@Id
	private Long id;
	
	@Column(name="tablename", nullable = false)
	private String tableName;
	
	@Column(nullable = false)
	private Long dataid;
	
	@Column(nullable = false)
	private TrackAction action;
	
	@JoinColumn(name="appuser",nullable = true,foreignKey = @ForeignKey(name="fk_track_appuser"))
	private AppUser appUser;
	
	@Column
	private String address;
	
	@Column(nullable = false)
	private LocalDateTime time;
}
