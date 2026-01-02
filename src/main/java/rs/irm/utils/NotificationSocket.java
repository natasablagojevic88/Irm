package rs.irm.utils;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.irm.common.dto.TokenDatabaseDTO;
import rs.irm.common.exceptions.CommonException;

@ServerEndpoint(value = "/socket/notify", configurator = CustomSocketConfigurator.class)
public class NotificationSocket {

	private static final List<SessionUser> sessions = new ArrayList<>();
	public static final BlockingQueue<SessionUserMessage> sessionQueues = new LinkedBlockingQueue<>();
	public static ExecutorService senderPool = Executors.newFixedThreadPool(4);

	@Context
	private HttpServletRequest httpServletRequest;

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		Long userId = getUserId(session, config);
		sessions.add(new SessionUser(userId, session));
	}

	private Long getUserId(Session session, EndpointConfig config) {
		HandshakeRequest request = (HandshakeRequest) config.getUserProperties().get("handshakeRequest");

		Map<String, String> parameters = new HashMap<>();
		if (request != null) {
			Map<String, List<String>> headers = request.getHeaders();
			List<String> cookieHeaders = headers.get("Cookie");
			if (cookieHeaders != null) {
				for (String cookieHeader : cookieHeaders) {
					String[] splits = cookieHeader.split("\\;");
					for (String cookieText : splits) {
						String cookie[] = cookieText.split("\\=");
						if(cookie.length<2) {
							continue;
						}
						parameters.put(cookie[0].trim(), cookie[1].trim());
					}
				}
			}
		}

		if (parameters.get("session") == null || parameters.get("refresh_token") == null) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "noToken", null);
		}
		TokenDatabaseDTO tokenDatatableDTO = null;
		try {
			tokenDatatableDTO = new CustomContainerRequestFilter().checkToken(parameters.get("session"),
					parameters.get("refresh_token"));
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

		Long userid = Long.valueOf(session.getQueryString().substring(3));

		if (userid.doubleValue() != tokenDatatableDTO.getAppUserId().doubleValue()) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "wrongUser", userid);
		}

		return userid;
	}

	@OnClose
	public void onClose(Session session) {
		List<SessionUser> userSessions = NotificationSocket.sessions.stream().filter(a -> a != null)
				.filter(a -> a.getSession().getId().equals(session.getId())).toList();

		for (SessionUser sessionUser : userSessions) {
			NotificationSocket.sessions.remove(sessionUser);
		}

	}

	@SuppressWarnings("static-access")
	public void sendMessage(Long userid, Long count) {
		List<SessionUser> userSessions = NotificationSocket.sessions.stream().filter(a -> a != null)
				.filter(a -> a.getUserId().doubleValue() == userid.doubleValue()).toList();

		for (SessionUser sessionUser : userSessions) {
			this.sessionQueues.add(new SessionUserMessage(sessionUser,count));
		}
	}
	
	public void sendMessageToUser() {
		for(int i =0;i<4;i++) {
			senderPool.submit(()->{
				while(true) {
					@SuppressWarnings("static-access")
					SessionUserMessage message=this.sessionQueues.poll(3, TimeUnit.SECONDS);
					if(message!=null)   {
						if(message.getSessionUser().getSession().isOpen()) {
							message.getSessionUser().getSession().getBasicRemote().sendObject(message.getCount());
						}
					}
					
				}
			});
		}
	}

}

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
class SessionUser {

	private Long userId;

	private Session session;
}

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
class SessionUserMessage {

	private SessionUser sessionUser;

	private Long count;
}
