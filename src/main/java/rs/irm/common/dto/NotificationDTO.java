package rs.irm.common.dto;

import java.time.LocalDateTime;

import lombok.Data;
import rs.irm.common.entity.Notification;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.annotations.TableHide;
import rs.irm.database.enums.InitSort;
import rs.irm.database.enums.SortDirection;

@Data
@EntityClass(Notification.class)
public class NotificationDTO {

	@TableHide
	private Long id;
	
	@TableHide
	private Long appUserId;
	
	private String name;
	
	@TableHide
	private String body;
	
	private Boolean unread;
	
	@InitSort(sortDirection = SortDirection.DESC)
	private LocalDateTime createationTime;
	
	private LocalDateTime readTime;
}
