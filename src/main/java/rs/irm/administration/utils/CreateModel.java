package rs.irm.administration.utils;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.ModelColumnDTO;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.administration.entity.Model;
import rs.irm.administration.entity.Role;
import rs.irm.administration.enums.ModelType;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.exceptions.FieldRequiredException;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.BaseType;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.CreateColumnData;
import rs.irm.database.utils.CreateTableData;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.database.utils.ForeignKeyData;
import rs.irm.database.utils.IndexData;
import rs.irm.database.utils.TableData;
import rs.irm.database.utils.TableFilter;
import rs.irm.database.utils.UniqueData;
import rs.irm.utils.DatabaseListenerJob;

public class CreateModel implements ExecuteMethodWithReturn<ModelDTO> {

	private HttpServletRequest servletRequest;
	private ModelDTO modelDTO;

	private DatatableService datatableService = new DatatableServiceImpl();
	private ModelMapper modelMapper = new ModelMapper();

	public CreateModel(HttpServletRequest servletRequest, ModelDTO modelDTO) {
		this.servletRequest = servletRequest;
		this.modelDTO = modelDTO;
		this.datatableService = new DatatableServiceImpl(this.servletRequest);
	}

	@Override
	public ModelDTO execute(Connection connection) {

		boolean insert = this.modelDTO.getId() == 0;

		modelDTO.setCode(modelDTO.getCode().toLowerCase());

		if (modelDTO.getType().equals(ModelType.TABLE.name())) {
			if (modelDTO.getDialogWidth() == null) {
				throw new FieldRequiredException("ModelDTO.dialogWidth");
			}

			if (modelDTO.getColumnsNumber() == null) {
				throw new FieldRequiredException("ModelDTO.columnsNumber");
			}

			if (modelDTO.getTableWidth() == null) {
				throw new FieldRequiredException("ModelDTO.tableWidth");
			}
		} else {
			modelDTO.setDialogWidth(null);
			modelDTO.setColumnsNumber(null);
			modelDTO.setTableWidth(null);
		}

		TableParameterDTO tableParameterDTOForColumn = new TableParameterDTO();
		TableFilter tableFilter = new TableFilter();
		tableFilter.setField("modelId");
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableFilter.setParameter1(String.valueOf(modelDTO.getId()));
		tableParameterDTOForColumn.getTableFilters().add(tableFilter);
		List<ModelColumnDTO> modelColumnDTOs = this.datatableService.findAll(tableParameterDTOForColumn,
				ModelColumnDTO.class, connection);

		for (ModelColumnDTO modelColumnDTO : modelColumnDTOs) {
			if ((modelColumnDTO.getColumnNumber() + modelColumnDTO.getColspan() - 1) > modelDTO.getColumnsNumber()) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "wrongNumberOfColumns",
						modelColumnDTO.getName());
			}
		}

		Model model = modelDTO.getId() != 0
				? this.datatableService.findByExistingId(modelDTO.getId(), Model.class, connection)
				: new Model();

		customModelMapper().map(modelDTO, model);

		if (modelDTO.getParentId() != null && modelDTO.getParentId() != -1) {
			model.setParent(this.datatableService.findByExistingId(modelDTO.getParentId(), Model.class, connection));
			if (model.getType().equals(ModelType.MENU) && model.getParent().getType().equals(ModelType.TABLE)) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "menuNotAllowed", null);
			}
		} else {
			model.setParent(null);
			if (model.getType().equals(ModelType.TABLE)) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "tableNotAllowed", null);
			}
		}
		model.setPreviewRole(new Role(modelDTO.getPreviewRoleId()));
		model.setUpdateRole(new Role(modelDTO.getUpdateRoleId()));
		model.setLockRole(new Role(modelDTO.getLockRoleId()));
		model.setUnlockRole(new Role(modelDTO.getUnlockRoleId()));

		model = this.datatableService.save(model, connection);

		if (insert && model.getType().equals(ModelType.TABLE)) {
			try {
				insertTable(model, connection);
			} catch (Exception e) {
				throw new WebApplicationException(e);
			}
		}

		modelMapper.map(model, modelDTO);

		if (modelDTO.getParentId() == null) {
			modelDTO.setParentId(Long.valueOf(-1));
		}
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("NOTIFY " + DatabaseListenerJob.model_listener + ", 'Model changed';");
			statement.close();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

		return modelDTO;
	}

	private ModelMapper customModelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.addMappings(new PropertyMap<ModelDTO, Model>() {

			@Override
			protected void configure() {
				skip(destination.getParent());
				skip(destination.getPreviewRole());
				skip(destination.getUnlockRole());
				skip(destination.getUpdateRole());
				skip(destination.getLockRole());

			}
		});

		return modelMapper;
	}

	void insertTable(Model model, Connection connection) throws Exception {

		String catalog = connection.getCatalog();
		String schema = connection.getSchema();

		DatabaseMetaData databaseMetaData = connection.getMetaData();

		if (databaseMetaData.getTables(catalog, schema, model.getCode(), null).next()) {
			throw new CommonException(HttpsURLConnection.HTTP_BAD_REQUEST, "tableAlreadyExists", model.getCode());
		}

		Model parentModel = model.getParent() == null ? null
				: model.getParent().getType().equals(ModelType.TABLE) ? model.getParent() : null;

		TableData tableData = new TableData();
		tableData.setName(model.getCode());

		CreateColumnData idColumn = new CreateColumnData();
		idColumn.setBaseType(BaseType.bigserial);
		idColumn.setId(true);
		idColumn.setName("id");
		idColumn.setNullable(false);
		idColumn.setLength(255);
		idColumn.setPrecision(0);
		tableData.getColumnDataList().add(idColumn);

		CreateColumnData codeColumn = new CreateColumnData();
		codeColumn.setBaseType(model.getNumericCode() ? BaseType.numeric : BaseType.varchar);
		codeColumn.setId(false);
		codeColumn.setName("code");
		codeColumn.setNullable(false);
		codeColumn.setLength(255);
		codeColumn.setPrecision(0);
		tableData.getColumnDataList().add(codeColumn);

		CreateColumnData lockColumn = new CreateColumnData();
		lockColumn.setBaseType(BaseType.bool);
		lockColumn.setId(false);
		lockColumn.setName("lock");
		lockColumn.setNullable(false);
		lockColumn.setLength(255);
		lockColumn.setPrecision(0);
		tableData.getColumnDataList().add(lockColumn);

		UniqueData uniqueData = new UniqueData();
		List<String> uniqueColumns = new ArrayList<>();
		uniqueColumns.add("code");
		List<TableData> tableDatas = new ArrayList<>();

		if (parentModel != null) {
			CreateColumnData headerColumn = new CreateColumnData();
			headerColumn.setBaseType(BaseType.int8);
			headerColumn.setId(false);
			headerColumn.setName(parentModel.getCode());
			headerColumn.setNullable(false);
			headerColumn.setLength(255);
			headerColumn.setPrecision(0);
			tableData.getColumnDataList().add(headerColumn);

			ForeignKeyData foreignKeyData = new ForeignKeyData();
			foreignKeyData.setCascade(true);
			foreignKeyData.setColumn(parentModel.getCode());
			foreignKeyData.setName("fk_" + model.getCode() + "_" + parentModel.getCode());
			foreignKeyData.setTable(parentModel.getCode());
			tableData.getForeignKeyDatas().add(foreignKeyData);
			uniqueColumns.add(parentModel.getCode());

			IndexData indexData = new IndexData();
			indexData.setColumns(parentModel.getCode());
			indexData.setName("index_" + model.getCode() + "_" + parentModel.getCode());
			tableData.getIndexDatas().add(indexData);

			TableData parentData = new TableData();
			parentData.setName(parentModel.getCode());
			CreateColumnData idColumnParent = new CreateColumnData();
			idColumnParent.setId(true);
			idColumnParent.setName("id");
			parentData.getColumnDataList().add(idColumnParent);
			tableDatas.add(parentData);

		}
		uniqueData.setColumns(uniqueColumns);
		uniqueData.setName(model.getCode() + "_code_unique");
		tableData.getUniqueDatas().add(uniqueData);

		tableDatas.add(tableData);
		CreateTableData createTableData = new CreateTableData(tableDatas);
		createTableData.execute(connection);
	}
}
