package rs.irm.common.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.common.dto.NotificationCountDTO;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.impl.CommonServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;

public class FindNotificationCount implements ExecuteMethodWithReturn<NotificationCountDTO> {

	private HttpServletRequest httpServletRequest;
	private CommonService commonService;

	public FindNotificationCount(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
		this.commonService = new CommonServiceImpl(this.httpServletRequest);
	}

	@Override
	public NotificationCountDTO execute(Connection connection) {
		Long count = 0L;
		String query = 
				"select count(*)\n" 
				+ "from\n" 
				+ "notification n \n" 
				+ "where \n" 
				+ "n.appuser ="+ commonService.getAppUser().getId() + "\n"
				+ "and n.unread =true";
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			resultSet.next();

			Number number = (Number) resultSet.getObject(1);
			count = number.longValue();

			resultSet.next();
			statement.close();
		} catch (SQLException e) {
			throw new WebApplicationException(e);
		}

		return new NotificationCountDTO(count);
	}
}
