package rs.irm.administration.utils;


import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Iterator;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import rs.irm.administration.dto.SqlQueryParametersDTO;
import rs.irm.administration.entity.SqlExecuteTrack;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.impl.CommonServiceImpl;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;

public class SqlExecute implements ExecuteMethodWithReturn<Long> {

	private SqlQueryParametersDTO sqlQueryParametersDTO;
	private HashMap<Integer, Object> parameters;
	private HttpServletRequest httpServletRequest;
	
	private DatatableService datatableService;
	private CommonService commonService;
	public SqlExecute(HttpServletRequest httpServletRequest,SqlQueryParametersDTO sqlQueryParametersDTO,HashMap<Integer, Object> parameters) {
		this.httpServletRequest=httpServletRequest;
		this.sqlQueryParametersDTO = sqlQueryParametersDTO;
		this.parameters=parameters;
		this.datatableService=new DatatableServiceImpl(this.httpServletRequest);
		this.commonService=new CommonServiceImpl(this.httpServletRequest);
	}

	@Override
	public Long execute(Connection connection) {
		
		Long numberOfRows=-1L;

		try {
			net.sf.jsqlparser.statement.Statement queryStatement = CCJSqlParserUtil
					.parse(sqlQueryParametersDTO.getSqlQuery());
			
			if(queryStatement instanceof Select) {
				
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "unallowedQuery", null);
			};
		} catch (Exception e) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage(), null);
		}
		try {
			PreparedStatement statement=connection.prepareStatement(sqlQueryParametersDTO.getSqlQuery());
			
			Iterator<Integer> itr=parameters.keySet().iterator();
			while(itr.hasNext()) {
				Integer parameterIndex=itr.next();
				statement.setObject(parameterIndex, parameters.get(parameterIndex));
			}
			
			numberOfRows= statement.executeLargeUpdate();
			statement.close();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
		
		insertToTrack(sqlQueryParametersDTO.getSqlQuery(), numberOfRows, connection);

		return numberOfRows;
	}
	
	private void insertToTrack(String query,Long numberOfRows,Connection connection) {
		SqlExecuteTrack executeTrack=new SqlExecuteTrack();
		executeTrack.setAppUser(commonService.getAppUser());
		executeTrack.setIpaddress(commonService.getIpAddress());
		executeTrack.setSqlQuery(query);
		executeTrack.setUpdatedNumberOfRows(numberOfRows);
		
		this.datatableService.save(executeTrack, connection);
		
	}

}
