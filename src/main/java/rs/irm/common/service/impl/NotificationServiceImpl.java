package rs.irm.common.service.impl;

import java.net.HttpURLConnection;
import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import rs.irm.common.dto.NotificationCountDTO;
import rs.irm.common.dto.NotificationDTO;
import rs.irm.common.entity.Notification;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.NotificationService;
import rs.irm.common.utils.FindNotificationCount;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.utils.TableFilter;

@Named
public class NotificationServiceImpl implements NotificationService {

	@Inject
	private DatatableService datatableService;

	@Context
	private HttpServletRequest httpServletRequest;

	@Inject
	private CommonService commonService;

	private ModelMapper modelMapper = new ModelMapper();

	@Override
	public NotificationCountDTO getCount() {
		FindNotificationCount findNotificationCount = new FindNotificationCount(httpServletRequest);
		return datatableService.executeMethodWithReturn(findNotificationCount);
	}

	@Override
	public TableDataDTO<NotificationDTO> getTable(TableParameterDTO tableParameterDTO) {
		tableParameterDTO.getTableFilters().add(new TableFilter("appUserId", SearchOperation.equals,
				String.valueOf(commonService.getAppUser().getId()), null));
		return datatableService.getTableDataDTO(tableParameterDTO, NotificationDTO.class);
	}

	@Override
	public NotificationDTO getRead(Long id) {
		Notification notification = this.datatableService.findByExistingId(id, Notification.class);
		if (notification.getAppUser().getId().doubleValue() != commonService.getAppUser().getId().doubleValue()) {
			throw new CommonException(HttpURLConnection.HTTP_FORBIDDEN, "noRight", null);
		}
		if (notification.getUnread()) {
			notification.setUnread(false);
			notification.setReadTime(LocalDateTime.now());
			notification = this.datatableService.save(notification);
		}
		return modelMapper.map(notification, NotificationDTO.class);
	}

	@Override
	public NotificationDTO markAsUnread(Long id) {
		Notification notification = this.datatableService.findByExistingId(id, Notification.class);
		if (notification.getAppUser().getId().doubleValue() != commonService.getAppUser().getId().doubleValue()) {
			throw new CommonException(HttpURLConnection.HTTP_FORBIDDEN, "noRight", null);
		}
		if (!notification.getUnread()) {
			notification.setUnread(true);
			notification.setReadTime(null);
			notification = this.datatableService.save(notification);
		}
		return modelMapper.map(notification, NotificationDTO.class);
		
	}

}
