package rs.irm.administration.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.entity.Model;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;

public class FindColumns implements ExecuteMethodWithReturn<List<ComboboxDTO>> {

	private HttpServletRequest httpServletRequest;
	private Long modelId;

	private DatatableService datatableService;

	public FindColumns(HttpServletRequest httpServletRequest, Long modelId) {
		this.httpServletRequest = httpServletRequest;
		this.modelId = modelId;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
	}

	@Override
	public List<ComboboxDTO> execute(Connection connection) {
		try {
			List<ComboboxDTO> list = new ArrayList<>();
			String catalog = connection.getCatalog();
			String schema = connection.getSchema();
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			Model model = this.datatableService.findByExistingId(modelId, Model.class, connection);

			ResultSet resultSet = databaseMetaData.getColumns(catalog, schema, model.getCode(), null);

			while (resultSet.next()) {
				list.add(new ComboboxDTO(resultSet.getObject(4).toString(), resultSet.getObject(4).toString()));
			}
			resultSet.close();

			return list;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

}
