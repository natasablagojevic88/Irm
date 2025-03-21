package rs.irm.common.dto;

import java.time.LocalDateTime;

import lombok.Data;
import rs.irm.common.entity.Track;
import rs.irm.common.enums.TrackAction;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.annotations.EnumField;
import rs.irm.database.annotations.TableHide;
import rs.irm.database.enums.InitSort;
import rs.irm.database.enums.SortDirection;

@Data
@EntityClass(Track.class)
public class TrackDTO {
	
	@TableHide
	private String tableName;
	
	@TableHide
	private Long dataid;
	
	@EnumField(TrackAction.class)
	private String action;
	
	private String appUserUsername;
	
	private String appUserName;

	@InitSort(sortDirection = SortDirection.DESC)
	private LocalDateTime time;
	
}
