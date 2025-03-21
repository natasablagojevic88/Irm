package rs.irm.utils;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.HttpURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.util.PSQLException;

import jakarta.inject.Inject;
import jakarta.persistence.Table;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.exceptions.ExcelRowException;
import rs.irm.common.exceptions.FieldRequiredException;
import rs.irm.common.exceptions.MaximumException;
import rs.irm.common.exceptions.MinimumException;
import rs.irm.common.exceptions.NoDataFoundException;
import rs.irm.common.exceptions.WrongTokenException;
import rs.irm.common.service.ResourceBundleService;

@Provider
public class CustomExceptionMapper implements ExceptionMapper<WebApplicationException> {
	Logger logger = LogManager.getLogger(CustomExceptionMapper.class);

	@Context
	private HttpServletRequest request;

	@Inject
	private ResourceBundleService resourceBundleService;

	@Override
	public Response toResponse(WebApplicationException exception) {
		Exception exceptionLast = getLastException(exception);
		String classClass = exceptionLast.getClass().getCanonicalName();

		if (classClass.equals(NoDataFoundException.class.getCanonicalName())) {
			return toResponseNoDataFound((NoDataFoundException) exceptionLast);
		} else if (classClass.equals(CommonException.class.getCanonicalName())) {
			return toResponseCommonException((CommonException) exceptionLast);
		} else if (classClass.equals(WrongTokenException.class.getCanonicalName())) {
			return toResponseWrongTokenException((WrongTokenException) exceptionLast);
		} else if (classClass.equals(FieldRequiredException.class.getCanonicalName())) {
			return toResponseFieldRequiredException((FieldRequiredException) exceptionLast);
		} else if (classClass.equals(MinimumException.class.getCanonicalName())) {
			return toResponseMinimumException((MinimumException) exceptionLast);
		}else if (classClass.equals(MaximumException.class.getCanonicalName())) {
			return toResponseMaximumException((MaximumException) exceptionLast);
		}else if (classClass.equals(PSQLException.class.getCanonicalName())) {
			return toResponsePSQLException((PSQLException) exceptionLast);
		}else if (classClass.equals(ExcelRowException.class.getCanonicalName())) {
			return toResponseExcelRowException((ExcelRowException) exceptionLast);
		}
		else {
			return toResponseDefault(exceptionLast);
		}

	}

	private Exception getLastException(Exception exception) {

		while (exception.getCause() != null) {
			exception = (Exception) exception.getCause();
		}

		return exception;
	}

	private Response toResponseNoDataFound(NoDataFoundException exception) {
		ErrorDetail errorDetail = new ErrorDetail();
		errorDetail.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

		String tableName = exception.getInClass().getAnnotation(Table.class).name();
		Long id = exception.getId();

		errorDetail.setMessage(resourceBundleService.getText(exception.getMessage(), new Object[] { tableName, id }));
		errorDetail.setDescription(request == null ? null : request.getPathInfo());
		logger.error(exception.getMessage() + ":" + tableName + ":" + id + " : " + errorDetail.getDescription());
		return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(errorDetail).build();
	}

	private Response toResponseCommonException(CommonException exception) {
		ErrorDetail errorDetail = new ErrorDetail();
		errorDetail.setStatus(exception.getStatus());
		String message = resourceBundleService.getText(exception.getMessage(), null);
		if (exception.getInObject() != null) {
			message += ": " + exception.getInObject();
		}
		errorDetail.setMessage(message);
		errorDetail.setDescription(request == null ? null : request.getPathInfo());
		logger.error(exception.getMessage() + (exception != null ? ":" + exception.getInObject() : ""), exception);
		return Response.status(exception.getStatus()).entity(errorDetail).build();
	}

	private Response toResponseWrongTokenException(WrongTokenException exception) {
		ErrorDetail errorDetail = new ErrorDetail();
		errorDetail.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
		errorDetail.setDescription(request == null ? null : request.getPathInfo());
		logger.error(exception.getMessage() + ":" + exception.getToken() + " : " + errorDetail.getDescription());
		return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).entity(errorDetail).build();
	}

	private Response toResponseFieldRequiredException(FieldRequiredException exception) {
		ErrorDetail errorDetail = new ErrorDetail();
		errorDetail.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
		String message = resourceBundleService.getText(exception.getMessage(), null);
		message += ": ";
		message += resourceBundleService.getText(exception.getField(), null);
		errorDetail.setMessage(message);
		errorDetail.setDescription(request == null ? null : request.getPathInfo());
		logger.error(exception.getMessage() + ": " + exception.getField() + " : " + errorDetail.getDescription());

		return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(errorDetail).build();
	}

	private Response toResponsePSQLException(PSQLException exception) {
		ErrorDetail errorDetail = new ErrorDetail();
		errorDetail.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
		errorDetail.setDescription(request == null ? null : request.getPathInfo());

		String message = "";

		try {
			BufferedReader bufferedReader = new BufferedReader(new StringReader(exception.getMessage()));

			String row = bufferedReader.readLine().substring(7);
			bufferedReader.close();

			message = resourceBundleService.getText(row, null);
		} catch (Exception e) {
			errorDetail.setMessage(exception.getMessage());
			logger.error(exception.getMessage(), exception);
			return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(errorDetail).build();
		}

		errorDetail.setMessage(message);
		logger.error(exception.getMessage(), exception);

		return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(errorDetail).build();
	}

	private Response toResponseMinimumException(MinimumException exception) {
		ErrorDetail errorDetail = new ErrorDetail();
		errorDetail.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

		Object[] parameters = new Object[] { resourceBundleService.getText(exception.getField(), null),
				exception.getMinimum() };
		String message = resourceBundleService.getText(exception.getMessage(), parameters);

		errorDetail.setMessage(message);
		errorDetail.setDescription(request == null ? null : request.getPathInfo());
		logger.error(exception.getMessage() + ": " + exception.getField() + " : " + exception.getMinimum() + " : "
				+ errorDetail.getDescription());

		return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(errorDetail).build();
	}
	
	private Response toResponseMaximumException(MaximumException exception) {
		ErrorDetail errorDetail = new ErrorDetail();
		errorDetail.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);

		Object[] parameters = new Object[] { resourceBundleService.getText(exception.getField(), null),
				exception.getMinimum() };
		String message = resourceBundleService.getText(exception.getMessage(), parameters);

		errorDetail.setMessage(message);
		errorDetail.setDescription(request == null ? null : request.getPathInfo());
		logger.error(exception.getMessage() + ": " + exception.getField() + " : " + exception.getMinimum() + " : "
				+ errorDetail.getDescription());

		return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(errorDetail).build();
	}
	
	private Response toResponseExcelRowException(ExcelRowException exception) {
		ErrorDetail errorDetail = new ErrorDetail();
		errorDetail.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
		String message = resourceBundleService.getText("row", null);
		message += ": ";
		message += exception.getRow();
		message += " - ";
		message+=resourceBundleService.getText(exception.getInMessage(), null);
		errorDetail.setMessage(message);
		errorDetail.setDescription(request == null ? null : request.getPathInfo());
		logger.error("row:"+exception.getRow()+" : error:"+exception.getMessage()+" : " + errorDetail.getDescription(),exception);

		return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(errorDetail).build();
	}

	private Response toResponseDefault(Exception exception) {
		ErrorDetail errorDetail = new ErrorDetail();
		errorDetail.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
		errorDetail.setMessage(exception.getMessage());
		errorDetail.setDescription(request == null ? null : request.getPathInfo());
		logger.error(exception.getMessage(), exception);
		return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(errorDetail).build();
	}
}
