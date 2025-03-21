package rs.irm.administration.utils;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.ModelColumnDTO;
import rs.irm.administration.entity.Model;
import rs.irm.administration.entity.ModelColumn;
import rs.irm.administration.enums.ModelColumnType;
import rs.irm.administration.enums.ModelType;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.exceptions.FieldRequiredException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.impl.CommonServiceImpl;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.CreateColumnData;
import rs.irm.database.utils.CreateTableData;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.database.utils.ForeignKeyData;
import rs.irm.database.utils.TableFilter;

public class CreateModelColumn implements ExecuteMethodWithReturn<ModelColumnDTO> {

	private HttpServletRequest httpServletRequest;
	private ModelColumnDTO modelColumnDTO;

	private DatatableService datatableService;
	private ModelMapper modelMapper = new ModelMapper();
	private CommonService commonService;

	public CreateModelColumn(HttpServletRequest httpServletRequest, ModelColumnDTO modelColumnDTO) {
		this.httpServletRequest = httpServletRequest;
		this.modelColumnDTO = modelColumnDTO;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
		this.commonService=new CommonServiceImpl(this.httpServletRequest);
	}

	@Override
	public ModelColumnDTO execute(Connection connection) {
		boolean insert = modelColumnDTO.getId() == 0;
		modelColumnDTO.setCode(modelColumnDTO.getCode().toLowerCase());
		ModelColumn modelColumn = modelColumnDTO.getId() != 0
				? this.datatableService.findByExistingId(modelColumnDTO.getId(), ModelColumn.class, connection)
				: new ModelColumn();
		modelColumn.setParent(null);
		modelColumnDTO.setParentId(modelColumnDTO.getDisabled()?null:modelColumnDTO.getParentId());
		Model model = this.datatableService.findByExistingId(modelColumnDTO.getModelId(), Model.class, connection);

		if (!model.getType().equals(ModelType.TABLE)) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "modelNotTable", model.getCode());
		}

		if (modelColumnDTO.getColumnType().equals(ModelColumnType.STRING.name())) {
			if (modelColumnDTO.getLength() == null) {
				throw new FieldRequiredException("ModelColumnDTO.length");
			}
		}

		if (modelColumnDTO.getColumnType().equals(ModelColumnType.BIGDECIMAL.name())) {
			this.modelColumnDTO.setLength(255);
			if (modelColumnDTO.getPrecision() == null) {
				throw new FieldRequiredException("ModelColumnDTO.precision");
			}
		}

		if (modelColumnDTO.getColumnType().equals(ModelColumnType.LONG.name())) {
			this.modelColumnDTO.setLength(255);
			this.modelColumnDTO.setPrecision(0);
		}

		if (modelColumnDTO.getColumnType().equals(ModelColumnType.BOOLEAN.name())) {
			this.modelColumnDTO.setNullable(false);
		}
		
		if (!modelColumnDTO.getColumnType().equals(ModelColumnType.STRING.name())) {
			this.modelColumnDTO.setListOfValues(null);
		}
		
		if(commonService.hasText(modelColumnDTO.getDefaultValue())) {
			commonService.checkDefaultParameter(modelColumnDTO.getDefaultValue());
		}
		
		if(commonService.hasText(modelColumnDTO.getListOfValues())) {
			commonService.checkDefaultParameter(modelColumnDTO.getListOfValues());
		}

		customModelMapper().map(modelColumnDTO, modelColumn);
		modelColumn.setModel(model);
		if (modelColumnDTO.getColumnType().equals(ModelColumnType.CODEBOOK.name())) {
			if (modelColumnDTO.getCodebookModelId() == null) {
				throw new FieldRequiredException("ModelColumnDTO.codebookModelId");
			} else {
				Model modelCodebook = this.datatableService.findByExistingId(modelColumnDTO.getCodebookModelId(),
						Model.class, connection);
				modelColumn.setCodebookModel(modelCodebook);

				if (!modelCodebook.getType().equals(ModelType.TABLE)) {
					throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "modelNotTable",
							modelCodebook.getCode());
				}

				if (modelCodebook.getParent().getType().equals(ModelType.TABLE)&&(!modelColumnDTO.getDisabled())) {
					if (modelColumnDTO.getParentId() == null) {
						throw new FieldRequiredException("ModelColumnDTO.parentId");
					} else {
						modelColumn.setParent(this.datatableService.findByExistingId(modelColumnDTO.getParentId(),
								ModelColumn.class, connection));
						if (!modelColumn.getParent().getCodebookModel().getCode()
								.equals(modelCodebook.getParent().getCode())) {
							throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "wrongParent",
									modelColumn.getParent().getCodebookModel().getCode());
						}
					}
				} else {
					modelColumnDTO.setParentId(null);
				}
			}
		}
		checkColumnNumber(model, connection);

		modelColumn = this.datatableService.save(modelColumn, connection);

		try {
			if (insert) {
				createColumn(modelColumn, connection);
			}
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
		ModelData.listColumnDTOs = this.datatableService.findAll(new TableParameterDTO(), ModelColumnDTO.class,
				connection);
		return modelMapper.map(modelColumn, ModelColumnDTO.class);
	}

	private ModelMapper customModelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.addMappings(new PropertyMap<ModelColumnDTO, ModelColumn>() {

			@Override
			protected void configure() {
				skip(destination.getModel());
				skip(destination.getCodebookModel());
				skip(destination.getParent());

			}
		});
		return modelMapper;
	}

	private void checkColumnNumber(Model model, Connection connection) {

		TableParameterDTO tableParameterDTO = new TableParameterDTO();

		TableFilter tableFilter = new TableFilter();
		tableFilter.setField("model");
		tableFilter.setParameter1(String.valueOf(model.getId()));
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableParameterDTO.getTableFilters().add(tableFilter);

		tableFilter = new TableFilter();
		tableFilter.setField("rowNumber");
		tableFilter.setParameter1(String.valueOf(modelColumnDTO.getRowNumber()));
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableParameterDTO.getTableFilters().add(tableFilter);

		tableFilter = new TableFilter();
		tableFilter.setField("id");
		tableFilter.setParameter1(String.valueOf(modelColumnDTO.getId()));
		tableFilter.setSearchOperation(SearchOperation.notEquals);
		tableParameterDTO.getTableFilters().add(tableFilter);

		List<ModelColumn> columns = this.datatableService.findAll(tableParameterDTO, ModelColumn.class, connection);

		Set<Integer> usedColumns = new HashSet<>();

		for (ModelColumn column : columns) {
			for (int i = column.getColumnNumber(); i <= (column.getColumnNumber() + column.getColspan() - 1); i++) {
				usedColumns.add(i);
			}
		}

		for (int i = modelColumnDTO.getColumnNumber(); i <= (modelColumnDTO.getColumnNumber()
				+ modelColumnDTO.getColspan() - 1); i++) {
			if (usedColumns.contains(i)) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "columnNumberInUse",
						modelColumnDTO.getColumnNumber());
			}
		}

		if ((modelColumnDTO.getColumnNumber() + modelColumnDTO.getColspan() - 1) > model.getColumnsNumber()) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "maximumColumnNumber",
					model.getColumnsNumber());
		}

	}

	private void createColumn(ModelColumn modelColumn, Connection connection) throws Exception {

		String catalog = connection.getCatalog();
		String schema = connection.getSchema();

		String tableName = modelColumn.getModel().getCode();

		DatabaseMetaData databaseMetaData = connection.getMetaData();

		if (databaseMetaData.getColumns(catalog, schema, tableName, modelColumn.getCode()).next()) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "columnAlreadyExists", modelColumn.getCode());
		}

		CreateColumnData createColumnData = new CreateColumnData();
		createColumnData.setBaseType(modelColumn.getColumnType().baseType);
		createColumnData.setId(false);
		createColumnData.setLength(modelColumn.getLength());
		createColumnData.setName(modelColumn.getCode());
		createColumnData.setNullable(modelColumn.getNullable());
		createColumnData.setPrecision(modelColumn.getPrecision());

		new CreateTableData(null).addColumn(tableName, createColumnData, connection);

		if (modelColumn.getColumnType().equals(ModelColumnType.CODEBOOK)) {

			ForeignKeyData foreignKeyData = new ForeignKeyData();
			foreignKeyData.setCascade(false);
			foreignKeyData.setColumn(modelColumn.getCode());
			foreignKeyData.setName("fk_" + tableName + "_" + modelColumn.getCode());
			foreignKeyData.setTable(modelColumn.getCodebookModel().getCode());

			new CreateTableData(null).addForeignKey(tableName, foreignKeyData, "id", connection);
		}

	}

}
