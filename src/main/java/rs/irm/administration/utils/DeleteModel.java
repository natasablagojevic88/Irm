package rs.irm.administration.utils;

import java.sql.Connection;
import java.sql.Statement;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.entity.Model;
import rs.irm.administration.enums.ModelType;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethod;

public class DeleteModel implements ExecuteMethod {

	private HttpServletRequest request;
	private Long id;

	private DatatableService datatableService = new DatatableServiceImpl();

	public DeleteModel(HttpServletRequest request, Long id) {
		this.request = request;
		this.id = id;
		this.datatableService = new DatatableServiceImpl(this.request);
	}

	@Override
	public void execute(Connection connection) {
		try {
			Model model = this.datatableService.findByExistingId(id, Model.class, connection);
			boolean table = model.getType().equals(ModelType.TABLE);

			this.datatableService.delete(model, connection);

			if (table) {

				if (model.getType().equals(ModelType.TABLE)) {
					String deleteTableQuary = "drop table " + model.getCode();
					Statement statement = connection.createStatement();
					statement.executeUpdate(deleteTableQuary);
					statement.close();
				}
			}
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

	}

}
