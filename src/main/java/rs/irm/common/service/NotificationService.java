package rs.irm.common.service;

import rs.irm.common.dto.NotificationCountDTO;
import rs.irm.common.dto.NotificationDTO;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

public interface NotificationService {
	
	NotificationCountDTO getCount();
	
	TableDataDTO<NotificationDTO> getTable(TableParameterDTO tableParameterDTO);
	
	NotificationDTO getRead(Long id);
	
	NotificationDTO markAsUnread(Long id);

}
