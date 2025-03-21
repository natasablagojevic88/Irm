package rs.irm.administration.utils;

import java.sql.Connection;

import jakarta.ws.rs.core.Response;
import rs.irm.administration.dto.SqlQueryParametersDTO;
import rs.irm.administration.dto.SqlResultDTO;
import rs.irm.administration.service.SqlExecutorService;
import rs.irm.administration.service.impl.SqlExecutorServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;

public class SqlQueryExcel implements ExecuteMethodWithReturn<Response> {

	private SqlQueryParametersDTO sqlQueryParametersDTO;
	private SqlExecutorService sqlExecutorService = new SqlExecutorServiceImpl();

	public SqlQueryExcel(SqlQueryParametersDTO sqlQueryParametersDTO) {
		this.sqlQueryParametersDTO = sqlQueryParametersDTO;
	}

	@Override
	public Response execute(Connection connection) {
		SqlQueryExecute sqlQueryExecute = new SqlQueryExecute(sqlQueryParametersDTO);
		SqlResultDTO sqlResultDTO = sqlQueryExecute.execute(connection);

		return sqlExecutorService.createExcel(sqlResultDTO, sqlQueryParametersDTO.getSqlQuery(),"exportQuery");
	}

}
