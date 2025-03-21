package rs.irm.administration.service;

import java.util.List;

import jakarta.ws.rs.core.Response;
import rs.irm.administration.dto.SqlEditorInfo;
import rs.irm.administration.dto.SqlExecuteResultDTO;
import rs.irm.administration.dto.SqlQueryParametersDTO;
import rs.irm.administration.dto.SqlResultDTO;
import rs.irm.common.dto.Base64DownloadFileDTO;

public interface SqlExecutorService {

	List<SqlEditorInfo> getTablesAndColumns();
	
	SqlResultDTO getSqlQuery(SqlQueryParametersDTO sqlQueryParametersDTO);
	
	Response getExcel(SqlQueryParametersDTO sqlQueryParametersDTO);

	Base64DownloadFileDTO getExcelBase64(SqlQueryParametersDTO sqlQueryParametersDTO);
	
	SqlExecuteResultDTO getExecute(SqlQueryParametersDTO sqlQueryParametersDTO);
	
	Response createExcel(SqlResultDTO sqlResultDTO,String sqlQuery,String fileName);
}
