package rs.irm.utils;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(category = Core.CATEGORY_NAME, name = "PgAppender", elementType=Appender.ELEMENT_TYPE)
public class PgAppender extends AbstractAppender{
	Logger logger = LogManager.getLogger(PgAppender.class);


	public PgAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions,
			Property[] properties) {
		super(name, filter, layout, ignoreExceptions, properties);
		this.checkConnection();
	}
	
	private void checkConnection() {
		try {
			try {
				Statement statement = AppConnections.logConnection.createStatement();
				statement.execute(AppParameters.checkconnection);
				statement.close();
			} catch(Exception e) {
				Context initContext = new InitialContext();
				Context envContext = (Context) initContext.lookup("java:/comp/env");
				DataSource dataSource = (DataSource) envContext.lookup("jdbc/postgres");
				AppConnections.logConnection = dataSource.getConnection();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} 
	}
	
	@PluginFactory
	public static PgAppender createAppender(
			@PluginAttribute("name") String name
			) {
		return new PgAppender(name, null, null, true, null);
	}

	@Override
	public void append(LogEvent event) {
		checkConnection();
		StackTraceElement[] stackTraceElements= event.getThrown().getStackTrace();
		StringBuilder stringBuilder=new StringBuilder();
		for(StackTraceElement stackTraceElement:stackTraceElements) {
			stringBuilder.append(stackTraceElement.toString()+"\n");
		}
		
		String insertToApplog="INSERT INTO app_logs\n"
				+ "(event_date, level, logger, thread, message, trace)\n"
				+ "VALUES(?,?,?,?,?,?)";
		
		try {
			PreparedStatement preparedStatement=AppConnections.logConnection.prepareStatement(insertToApplog);
			preparedStatement.setObject(1, new Timestamp(event.getTimeMillis()));
			preparedStatement.setObject(2, event.getLevel().name());
			preparedStatement.setObject(3, event.getLoggerName());
			preparedStatement.setObject(4, event.getThreadName());
			preparedStatement.setObject(5, event.getMessage().getFormattedMessage());
			preparedStatement.setObject(6, stringBuilder.toString());
			preparedStatement.execute();
			preparedStatement.close();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		
	}

	

	

}
