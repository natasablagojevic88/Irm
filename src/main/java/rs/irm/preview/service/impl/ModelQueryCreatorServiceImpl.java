package rs.irm.preview.service.impl;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.ModelColumnDTO;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.administration.dto.ModelJasperReportDTO;
import rs.irm.administration.enums.ModelColumnType;
import rs.irm.administration.enums.ModelType;
import rs.irm.administration.utils.ModelData;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.common.entity.Track;
import rs.irm.common.enums.TrackAction;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.common.service.impl.CommonServiceImpl;
import rs.irm.common.service.impl.ResourceBundleServiceImpl;
import rs.irm.common.utils.CheckAdmin;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.ColumnType;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.enums.SortDirection;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.QueryWriterService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.service.impl.QueryWriterServiceImpl;
import rs.irm.database.utils.ColumnData;
import rs.irm.database.utils.EnumList;
import rs.irm.database.utils.FieldInfo;
import rs.irm.database.utils.LeftTable;
import rs.irm.database.utils.LeftTableData;
import rs.irm.database.utils.TableFilter;
import rs.irm.preview.dto.SubCodebookInfoDTO;
import rs.irm.preview.service.ModelQueryCreatorService;
import rs.irm.preview.utils.TableButton;

@Named
public class ModelQueryCreatorServiceImpl implements ModelQueryCreatorService {

	private HttpServletRequest httpServletRequest;

	@Inject
	private ResourceBundleService resourceBundleService;

	@Inject
	private CommonService commonService;

	@Inject
	private DatatableService datatableService;

	public ModelQueryCreatorServiceImpl(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
		this.resourceBundleService = new ResourceBundleServiceImpl(this.httpServletRequest);
		this.commonService = new CommonServiceImpl(this.httpServletRequest);
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
	}

	public ModelQueryCreatorServiceImpl() {
	}

	private QueryWriterService queryWriterService = new QueryWriterServiceImpl();

	@Override
	public List<LinkedHashMap<String, Object>> findAll(TableParameterDTO tableParameterDTO, Long modelId,
			Connection connection) {
		List<ModelColumnDTO> columnDTOs = findColumns(modelId);
		ModelDTO modelDTO = findModel(modelId);

		String query = getFindAllQuery(tableParameterDTO, modelDTO, columnDTOs);
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			List<LinkedHashMap<String, Object>> linkedHashMaps = new ArrayList<>();

			while (resultSet.next()) {
				linkedHashMaps.add(resultSetToHashMap(modelDTO, columnDTOs, resultSet));
			}

			resultSet.close();
			statement.close();

			return linkedHashMaps;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

	}

	private String getFindAllQuery(TableParameterDTO tableParameterDTO, ModelDTO modelDTO,
			List<ModelColumnDTO> columnDTOs) {
		List<FieldInfo> listFieldInfos = new ArrayList<>();
		addDefaultColumn(listFieldInfos, modelDTO);
		addOtherColumns(listFieldInfos, columnDTOs);
		List<LeftTable> leftTables = queryWriterService.leftJoinTable(listFieldInfos);

		String select = queryWriterService.selectPart(listFieldInfos);
		String from = queryWriterService.fromPart(modelDTO.getCode(), leftTables);
		String where = queryWriterService.wherePart(tableParameterDTO, listFieldInfos);
		String order = queryWriterService.orderPart(tableParameterDTO, listFieldInfos);
		String pageable = queryWriterService.pageablePart(tableParameterDTO);

		String query = select + from + where + order + pageable;
		return query;
	}

	private ModelDTO findModel(Long modelId) {
		return ModelData.listModelDTOs.stream().filter(a -> a.getId().doubleValue() == modelId.doubleValue()).findFirst().get();
	}

	private List<ModelColumnDTO> findColumns(Long modelId) {
		List<ModelColumnDTO> list = new ArrayList<>();
		for (ModelColumnDTO modelColumnDTO : ModelData.listColumnDTOs.stream().filter(a -> a.getModelId() == modelId)
				.sorted(Comparator.comparing(ModelColumnDTO::getRowNumber)
						.thenComparing(Comparator.comparing(ModelColumnDTO::getColumnNumber)))
				.toList()) {
			try {
				ModelColumnDTO modelColumnDTO2 = ModelColumnDTO.class.getConstructor().newInstance();

				for (Field field : Arrays.asList(ModelColumnDTO.class.getDeclaredFields())) {
					field.setAccessible(true);
					field.set(modelColumnDTO2, field.get(modelColumnDTO));
				}
				list.add(modelColumnDTO2);
			} catch (Exception e) {
				throw new WebApplicationException(e);
			}

		}

		return list;
	}

	private String findParent(Long modelId) {
		ModelDTO modelDTO = findModel(modelId);
		if (modelDTO.getParentId() != null && modelDTO.getParentType().equals(ModelType.TABLE.name())) {
			return modelDTO.getParentCode();
		}

		return null;
	}

	private void addDefaultColumn(List<FieldInfo> listFieldInfos, ModelDTO modelDTO) {
		FieldInfo fieldInfoID = new FieldInfo();
		fieldInfoID.getColumnList().add("id");
		fieldInfoID.setFieldName("id");
		fieldInfoID.setFieldType("Long");
		listFieldInfos.add(fieldInfoID);

		FieldInfo fieldInfoCode = new FieldInfo();
		fieldInfoCode.getColumnList().add("code");
		fieldInfoCode.setFieldName("code");
		fieldInfoCode.setOrdernum(1);
		fieldInfoCode.setSortDirection(SortDirection.ASC);
		fieldInfoCode.setFieldType(modelDTO.getNumericCode() ? "Long" : "String");
		listFieldInfos.add(fieldInfoCode);

		FieldInfo fieldInfoLock = new FieldInfo();
		fieldInfoLock.getColumnList().add("lock");
		fieldInfoLock.setFieldName("lock");
		fieldInfoLock.setFieldType("Boolean");
		listFieldInfos.add(fieldInfoLock);

		if (modelDTO.getParentId() != null) {
			if (modelDTO.getParentType().equals(ModelType.TABLE.name())) {
				FieldInfo fieldInfoParent = new FieldInfo();
				fieldInfoParent.getColumnList().add(modelDTO.getParentCode());
				fieldInfoParent.setFieldName(modelDTO.getParentCode());
				fieldInfoParent.setFieldType("Long");
				listFieldInfos.add(fieldInfoParent);
			}
		}
	}

	private void addOtherColumns(List<FieldInfo> listFieldInfos, List<ModelColumnDTO> columnDTOS) {

		for (ModelColumnDTO modelColumnDTO : columnDTOS) {
			if (modelColumnDTO.getColumnType().equals(ModelColumnType.CODEBOOK.name())) {
				FieldInfo fieldInfoId = new FieldInfo();
				fieldInfoId.getColumnList().add(modelColumnDTO.getCode());
				fieldInfoId.getColumnList().add("id");
				fieldInfoId.setFieldName(modelColumnDTO.getCode());
				fieldInfoId.setFieldType("Long");
				LeftTableData leftTableData = new LeftTableData();
				leftTableData.setFieldColumn(modelColumnDTO.getCode());
				leftTableData.setIdColumn("id");
				leftTableData.setTable(modelColumnDTO.getCodebookModelCode());
				leftTableData.setPath("/" + modelColumnDTO.getCode());
				fieldInfoId.getLeftTableDatas().add(leftTableData);
				fieldInfoId.setLeftJoinPath("/" + modelColumnDTO.getCode());
				listFieldInfos.add(fieldInfoId);

				FieldInfo fieldInfoCode = new FieldInfo();
				fieldInfoCode.getColumnList().add(modelColumnDTO.getCode());
				fieldInfoCode.getColumnList().add("code");
				fieldInfoCode.setFieldName(modelColumnDTO.getCode() + "Code");
				fieldInfoCode.setFieldType(modelColumnDTO.getCodebookModelNumericCode() ? "Long" : "String");
				leftTableData = new LeftTableData();
				leftTableData.setFieldColumn(modelColumnDTO.getCode());
				leftTableData.setIdColumn("id");
				leftTableData.setTable(modelColumnDTO.getCodebookModelCode());
				leftTableData.setPath("/" + modelColumnDTO.getCode());
				fieldInfoCode.getLeftTableDatas().add(leftTableData);
				fieldInfoCode.setLeftJoinPath("/" + modelColumnDTO.getCode());
				listFieldInfos.add(fieldInfoCode);
			} else {
				FieldInfo fieldInfo = new FieldInfo();
				fieldInfo.getColumnList().add(modelColumnDTO.getCode());
				fieldInfo.setFieldName(modelColumnDTO.getCode());
				fieldInfo.setFieldType(ModelColumnType.valueOf(modelColumnDTO.getColumnType()).type);
				listFieldInfos.add(fieldInfo);
			}

		}
	}

	private Map<String, ModelColumnDTO> findColumnMap(List<ModelColumnDTO> columnDTOs) {
		Map<String, ModelColumnDTO> map = new HashMap<>();
		for (ModelColumnDTO modelColumnDTO : columnDTOs) {
			if (modelColumnDTO.getColumnType().equals(ModelColumnType.CODEBOOK.name())) {
				map.put(modelColumnDTO.getCode(), modelColumnDTO);
				map.put(modelColumnDTO.getCode() + "Code", modelColumnDTO);
			} else {
				map.put(modelColumnDTO.getCode(), modelColumnDTO);
			}
		}
		return map;
	}

	private LinkedHashMap<String, Object> resultSetToHashMap(ModelDTO modelDTO, List<ModelColumnDTO> columnDTOs,
			ResultSet resultSet) {
		try {
			LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<>();
			Map<String, ModelColumnDTO> mapColumn = findColumnMap(columnDTOs);

			List<FieldInfo> defaultColumns = new ArrayList<>();
			addDefaultColumn(defaultColumns, modelDTO);

			for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {

				if (resultSet.getObject(i) == null) {
					continue;
				}
				Object value = resultSet.getObject(i);
				String columnName = resultSet.getMetaData().getColumnName(i);

				if (!defaultColumns.stream().filter(a -> a.getFieldName().equals(columnName)).toList().isEmpty()) {

					FieldInfo fieldInfo = defaultColumns.stream().filter(a -> a.getFieldName().equals(columnName))
							.findFirst().get();
					addValueToMap(linkedHashMap, fieldInfo.getFieldType().toUpperCase(), columnName, value, null);
					continue;
				}

				ModelColumnDTO modelColumnDTO = mapColumn.get(columnName);
				addValueToMap(linkedHashMap, modelColumnDTO.getColumnType(), columnName, value, modelColumnDTO);
			}

			return linkedHashMap;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	private void addValueToMap(LinkedHashMap<String, Object> linkedHashMap, String columnType, String columnName,
			Object value, ModelColumnDTO modelColumnDTO) {
		switch (columnType) {
		case "STRING": {
			linkedHashMap.put(columnName, value.toString().length() == 0 ? null : value.toString());
			break;
		}
		case "INTEGER", "BIGDECIMAL", "LONG": {
			linkedHashMap.put(columnName, (Number) value);
			break;
		}
		case "LOCALDATE": {
			java.sql.Date date = (java.sql.Date) value;
			linkedHashMap.put(columnName, date.toLocalDate());
			break;
		}
		case "LOCALDATETIME": {
			java.sql.Timestamp date = (java.sql.Timestamp) value;
			linkedHashMap.put(columnName, date.toLocalDateTime());
			break;
		}
		case "BOOLEAN": {
			linkedHashMap.put(columnName, Boolean.valueOf(value.toString()));
			break;
		}
		case "CODEBOOK": {
			if (columnName.endsWith("Code")) {

				if (modelColumnDTO.getCodebookModelNumericCode()) {
					linkedHashMap.put(columnName, (Number) value);
				} else {
					linkedHashMap.put(columnName, value.toString());
				}
			} else {

				linkedHashMap.put(columnName, (Number) value);
			}

			break;
		}
		}
	}

	@Override
	public LinkedHashMap<String, Object> findByExistingId(Long id, Long modelId, Connection connection) {
		ModelDTO modelDTO = findModel(modelId);
		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		TableFilter tableFilter = new TableFilter();
		tableFilter.setField("id");
		tableFilter.setParameter1(String.valueOf(id));
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableParameterDTO.getTableFilters().add(tableFilter);

		List<LinkedHashMap<String, Object>> list = findAll(tableParameterDTO, modelId, connection);
		if (list.isEmpty()) {
			String message = resourceBundleService.getText("nodatafound", new Object[] { id, modelDTO.getCode() });
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, message, null);
		}

		return list.get(0);
	}

	@Override
	public TableDataDTO<LinkedHashMap<String, Object>> createTableDataDTO(TableParameterDTO tableParameterDTO,
			Long modelId, Long parentId, Connection connection) {
		TableDataDTO<LinkedHashMap<String, Object>> tableDataDTO = new TableDataDTO<>();

		ModelDTO modelDTO = findModel(modelId);
		List<ModelColumnDTO> columnDTOs = findColumns(modelId);

		tableDataDTO.setList(findAll(tableParameterDTO, modelId, connection));
		tableDataDTO.setTitle(resourceBundleService.getText(modelDTO.getName(), null));
		getTotalQuery(tableDataDTO, tableParameterDTO, modelDTO, columnDTOs, connection);
		Long totalElement = tableDataDTO.getTotalElements();
		Long numberOfPages = totalElement / tableParameterDTO.getPageSize();
		if (totalElement % tableParameterDTO.getPageSize() != 0) {
			numberOfPages = numberOfPages + 1;
		}
		tableDataDTO.setTotalPages(numberOfPages.intValue());
		tableDataDTO.setTable(modelDTO.getCode());
		tableDataDTO.setColumns(getColumnsForTableData(modelDTO, columnDTOs, tableDataDTO, connection));
		tableDataDTO.setNames(getNames(columnDTOs));
		tableDataDTO.setTableWidth(modelDTO.getTableWidth());
		tableDataDTO.setModel(modelDTO);
		tableDataDTO.setFields(createFieldForDialog(modelDTO, columnDTOs));
		tableDataDTO.setSubtables(getSubmodelsTable(modelId));
		tableDataDTO.setRights(getRight(modelId, parentId, connection));

		return tableDataDTO;
	}

	@Override
	public LinkedHashMap<String, Object> getDefaultValues(Long modelId, Long parentId, Connection connection) {
		LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<>();
		ModelDTO modelDTO = findModel(modelId);
		List<ModelColumnDTO> columnDTOs = findColumns(modelId);

		if (modelDTO.getNumericCode()) {
			String maxQuery = "select coalesce(max(code)+1,1) from " + modelDTO.getCode();

			if (modelDTO.getParentId() != null && modelDTO.getParentType().equals(ModelType.TABLE.name())) {
				maxQuery += " where " + modelDTO.getParentCode() + "=" + parentId;
			}

			try {
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(maxQuery);
				resultSet.next();
				linkedHashMap.put("code", resultSet.getObject(1));

				resultSet.close();
				statement.close();
			} catch (Exception e) {
				throw new WebApplicationException(e);
			}

		}
		for (ModelColumnDTO modelColumnDTO : columnDTOs) {
			if (!commonService.hasText(modelColumnDTO.getDefaultValue())) {
				continue;
			}

			try {
				Statement statement = connection.createStatement();
				String query = modelColumnDTO.getDefaultValue();
				if (query.contains("{parent}")) {
					query = query.replace("{parent}", "'" + parentId + "'");
				}
				ResultSet resultSet = statement.executeQuery(query);
				resultSet.next();
				linkedHashMap.put(modelColumnDTO.getCode(), resultSet.getObject(1));

				resultSet.close();
				statement.close();
			} catch (Exception e) {
				throw new WebApplicationException(e);
			}

		}

		return linkedHashMap;
	}

	private void getTotalQuery(TableDataDTO<LinkedHashMap<String, Object>> tableDataDTO,
			TableParameterDTO tableParameterDTO, ModelDTO modelDTO, List<ModelColumnDTO> columnDTOs,
			Connection connection) {
		try {
			List<FieldInfo> listFieldInfos = new ArrayList<>();
			addDefaultColumn(listFieldInfos, modelDTO);
			addOtherColumns(listFieldInfos, columnDTOs);
			List<LeftTable> leftTables = queryWriterService.leftJoinTable(listFieldInfos);

			String from = queryWriterService.fromPart(modelDTO.getCode(), leftTables);
			String where = queryWriterService.wherePart(tableParameterDTO, listFieldInfos);

			String selectPart = "select count(*)";
			List<FieldInfo> bigDecimalFieldInfos = listFieldInfos.stream()
					.filter(a -> a.getFieldType().equals("BigDecimal")).toList();
			for (FieldInfo fieldInfo : bigDecimalFieldInfos) {
				selectPart += ", coalesce(sum(" + fieldInfo.getAlias() + "." + fieldInfo.getColumnName() + "),0)";
			}
			selectPart += " ";

			String query = selectPart + from + where;
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			resultSet.next();
			Long totalNumber = ((Number) resultSet.getObject(1)).longValue();
			int bigDecimalIndex = 1;
			for (FieldInfo fieldInfo : bigDecimalFieldInfos) {
				tableDataDTO.setHasTotal(true);
				bigDecimalIndex++;
				tableDataDTO.getTotals().put(fieldInfo.getFieldName(),
						BigDecimal.valueOf(((Number) resultSet.getObject(bigDecimalIndex)).doubleValue()));
			}
			resultSet.close();
			statement.close();

			tableDataDTO.setTotalElements(totalNumber);
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	private List<ColumnData> getColumnsForTableData(ModelDTO modelDTO, List<ModelColumnDTO> columnDTOs,
			TableDataDTO<LinkedHashMap<String, Object>> tableDataDTO, Connection connection) {
		List<ColumnData> columnDatas = new ArrayList<>();

		ColumnData columnData = new ColumnData();
		columnData.setCode("code");
		columnData.setColumnType(modelDTO.getNumericCode() ? ColumnType.Long : ColumnType.String);
		columnData.setName(resourceBundleService.getText("code", null));
		columnDatas.add(columnData);

		columnData = new ColumnData();
		columnData.setCode("lock");
		columnData.setColumnType(ColumnType.Boolean);
		columnData.setName(resourceBundleService.getText("lock", null));
		columnDatas.add(columnData);

		List<EnumList> enumLists = new ArrayList<>();

		for (ModelColumnDTO modelColumnDTO : columnDTOs) {
			if (!modelColumnDTO.getShowInTable()) {
				continue;
			}

			if (commonService.hasText(modelColumnDTO.getListOfValues())) {
				try {
					Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery(modelColumnDTO.getListOfValues());
					EnumList enumList = new EnumList();
					enumList.setCode(modelColumnDTO.getCode());
					List<ComboboxDTO> listofValues = new ArrayList<>();
					while (resultSet.next()) {

						String value = resultSet.getObject(1).toString();
						String option = value;
						if (resultSet.getMetaData().getColumnCount() > 1) {
							option = resultSet.getObject(2).toString();
						}
						listofValues.add(new ComboboxDTO(value, option));

					}
					enumList.setList(listofValues);
					enumLists.add(enumList);

					resultSet.close();
					statement.close();
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}
			}

			ModelColumnType modelColumnType = ModelColumnType.valueOf(modelColumnDTO.getColumnType());
			if (modelColumnType.equals(ModelColumnType.CODEBOOK)) {

				columnData = new ColumnData();
				columnData.setCode(modelColumnDTO.getCode() + "Code");
				columnData.setColumnType(
						modelColumnDTO.getCodebookModelNumericCode() ? ColumnType.Long : ColumnType.String);
				columnData.setName(resourceBundleService.getText(modelColumnDTO.getName(), null));
				columnDatas.add(columnData);
			} else {
				columnData = new ColumnData();
				columnData.setCode(modelColumnDTO.getCode());
				columnData.setColumnType(ColumnType.valueOf(modelColumnType.type));
				columnData.setName(resourceBundleService.getText(modelColumnDTO.getName(), null));
				if (columnData.getColumnType().equals(ColumnType.BigDecimal)) {
					columnData.setNumberOfDecimal(modelColumnDTO.getPrecision());
				}
				columnDatas.add(columnData);
			}

		}

		tableDataDTO.setEnums(enumLists);

		return columnDatas;
	}

	private LinkedHashMap<String, String> getNames(List<ModelColumnDTO> columnDTOs) {

		LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();
		hashMap.put("id", resourceBundleService.getText("id", null));
		hashMap.put("code", resourceBundleService.getText("code", null));
		hashMap.put("lock", resourceBundleService.getText("lock", null));
		for (ModelColumnDTO modelColumnDTO : columnDTOs) {
			hashMap.put(modelColumnDTO.getCode(), resourceBundleService.getText(modelColumnDTO.getName(), null));
		}
		return hashMap;

	}

	private List<ModelColumnDTO> createFieldForDialog(ModelDTO modelDTO, List<ModelColumnDTO> columnDTOs) {
		List<ModelColumnDTO> list = new ArrayList<>();

		ModelColumnDTO modelColumnDTO = new ModelColumnDTO();
		modelColumnDTO.setCode("code");
		modelColumnDTO.setColspan(1);
		modelColumnDTO.setColumnNumber(1);
		modelColumnDTO
				.setColumnType(modelDTO.getNumericCode() ? ModelColumnType.LONG.name() : ModelColumnType.STRING.name());
		modelColumnDTO.setDisabled(false);
		modelColumnDTO.setName(resourceBundleService.getText("code", null));
		modelColumnDTO.setNullable(false);
		modelColumnDTO.setRowNumber(1);
		list.add(modelColumnDTO);

		for (ModelColumnDTO columnDTO : columnDTOs.stream().sorted(Comparator.comparing(ModelColumnDTO::getRowNumber)
				.thenComparing(Comparator.comparing(ModelColumnDTO::getColumnNumber))).toList()) {
			try {

				columnDTO.setRowNumber(columnDTO.getRowNumber() + 1);
				list.add(columnDTO);
			} catch (Exception e) {
				throw new WebApplicationException(e);
			}

		}

		Integer max = list.stream().sorted(Comparator.comparing(ModelColumnDTO::getRowNumber).reversed()).findFirst()
				.get().getRowNumber();

		modelColumnDTO = new ModelColumnDTO();
		modelColumnDTO.setCode("id");
		modelColumnDTO.setColspan(1);
		modelColumnDTO.setColumnNumber(1);
		modelColumnDTO.setColumnType(ModelColumnType.LONG.name());
		modelColumnDTO.setDisabled(true);
		modelColumnDTO.setName(resourceBundleService.getText("id", null));
		modelColumnDTO.setNullable(false);
		modelColumnDTO.setRowNumber(max + 1);
		list.add(modelColumnDTO);
		
		modelColumnDTO = new ModelColumnDTO();
		modelColumnDTO.setCode("lock");
		modelColumnDTO.setColspan(1);
		modelColumnDTO.setColumnNumber(modelDTO.getColumnsNumber()==1?1:2);
		modelColumnDTO.setColumnType(ModelColumnType.BOOLEAN.name());
		modelColumnDTO.setDisabled(true);
		modelColumnDTO.setName(resourceBundleService.getText("lock", null));
		modelColumnDTO.setNullable(false);
		modelColumnDTO.setRowNumber(modelDTO.getColumnsNumber()==1?(max+2):(max+1));
		list.add(modelColumnDTO);

		return list;

	}

	private List<TableButton> getSubmodelsTable(Long modelId) {
		List<TableButton> tableButtons = new ArrayList<>();

		List<ModelDTO> submodels = ModelData.listModelDTOs.stream().filter(a -> a.getParentId() != null)
				.filter(a -> a.getParentId() == modelId).filter(a -> a.getType().equals(ModelType.TABLE.name()))
				.sorted(Comparator.comparing(ModelDTO::getName)).toList();

		for (ModelDTO modelDTO : submodels) {
			TableButton tableButton = new TableButton();
			tableButton.setCode(String.valueOf(modelDTO.getId()));
			tableButton.setColor("purple");
			tableButton.setIcon(modelDTO.getIcon());
			tableButton.setName(resourceBundleService.getText(modelDTO.getName(), null));

			if (commonService.getRoles().contains(CheckAdmin.roleAdmin)
					|| commonService.getRoles().contains(modelDTO.getPreviewRoleCode())) {
				tableButtons.add(tableButton);
			}

		}

		List<ModelJasperReportDTO> jaspers = ModelData.listModelJasperReportDTOs.stream()
				.filter(a -> a.getModelId() == modelId).sorted(Comparator.comparing(ModelJasperReportDTO::getName))
				.toList();

		for (ModelJasperReportDTO jasperReportDTO : jaspers) {
			TableButton tableButton = new TableButton();
			tableButton.setCode("/jasper/" + jasperReportDTO.getId());
			tableButton.setColor("maroon");
			tableButton.setIcon("fa fa-file-pdf-o");
			tableButton.setName(resourceBundleService.getText(jasperReportDTO.getName(), null));
			tableButtons.add(tableButton);
		}

		return tableButtons;
	}

	private LinkedHashMap<String, Boolean> getRight(Long modelId, Long parentId, Connection connection) {
		ModelDTO modelDTO = findModel(modelId);
		LinkedHashMap<String, Boolean> rights = new LinkedHashMap<>();
		rights.put("parentLock", checkLockParent(modelId, 0L, parentId, connection));

		boolean checkAdmin = commonService.getRoles().contains(CheckAdmin.roleAdmin);

		rights.put("update", checkAdmin ? true : (commonService.getRoles().contains(modelDTO.getUpdateRoleCode())));
		rights.put("lock", checkAdmin ? true : (commonService.getRoles().contains(modelDTO.getLockRoleCode())));
		rights.put("unlock", checkAdmin ? true : (commonService.getRoles().contains(modelDTO.getUnlockRoleCode())));
		return rights;
	}

	@Override
	public LinkedHashMap<String, List<ComboboxDTO>> getCodebooks(Long modelId, Connection connection) {
		LinkedHashMap<String, List<ComboboxDTO>> hashMap = new LinkedHashMap<>();
		List<ModelColumnDTO> columns = findColumns(modelId);

		for (ModelColumnDTO modelColumnDTO : columns) {
			if (!modelColumnDTO.getColumnType().equals(ModelColumnType.CODEBOOK.name())) {
				continue;
			}

			String code = modelColumnDTO.getCode();
			ModelDTO modelDTO = findModel(modelColumnDTO.getCodebookModelId());
			if (modelDTO.getParentType().equals(ModelType.TABLE.name())||modelColumnDTO.getDisabled()) {
				hashMap.put(code, new ArrayList<>());
			} else {
				List<ComboboxDTO> list = new ArrayList<>();
				String query = "select id,code from " + modelDTO.getCode() + " order by 2";
				try {
					Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery(query);

					while (resultSet.next()) {
						ComboboxDTO comboboxDTO = new ComboboxDTO(resultSet.getObject(1),
								resultSet.getObject(2).toString());
						list.add(comboboxDTO);
					}

					resultSet.close();
					statement.close();

					hashMap.put(code, list);
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}

			}

		}

		return hashMap;
	}

	@Override
	public SubCodebookInfoDTO getSubCodebooks(Long modelId, String codebook, Long codebookValue,
			Connection connection) {

		List<ModelColumnDTO> columns = findColumns(modelId);
		List<ModelColumnDTO> findSubCodebook = columns.stream().filter(a -> a.getParentCode() != null)
				.filter(a -> a.getParentCode().equals(codebook)).toList();

		if (findSubCodebook.isEmpty()) {
			return new SubCodebookInfoDTO();
		} else {
			SubCodebookInfoDTO subCodebookInfoDTO = new SubCodebookInfoDTO();
			ModelColumnDTO parent = columns.stream().filter(a -> a.getCode().equals(codebook)).findFirst().get();
			ModelColumnDTO modelColumnDTO = findSubCodebook.get(0);
			subCodebookInfoDTO.setCodebook(modelColumnDTO.getCode());
			if (codebookValue != -1) {
				String query = "select id, code from " + modelColumnDTO.getCodebookModelCode() + " ";
				query += "where " + parent.getCodebookModelCode() + "=" + codebookValue + " ";
				query += "order by 2";

				try {
					Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery(query);
					List<ComboboxDTO> list = new ArrayList<>();
					while (resultSet.next()) {
						list.add(new ComboboxDTO(resultSet.getObject(1), resultSet.getObject(2).toString()));
					}
					subCodebookInfoDTO.setList(list);
					resultSet.close();
					statement.close();
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}

			}
			return subCodebookInfoDTO;
		}

	}

	@Override
	public Long getInsert(Long modelId, LinkedHashMap<String, Object> item, Long parentId, Connection connection) {
		String insertQuery = insertQuery(modelId);
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
			prepareParameters(preparedStatement, modelId, item, parentId, true);
			ResultSet resultSet = preparedStatement.executeQuery();
			resultSet.next();
			Long id = ((Number) resultSet.getObject(1)).longValue();

			insertTrack(modelId, id, TrackAction.INSERT, connection);

			resultSet.close();
			preparedStatement.close();
			return id;
		} catch (Exception e) {

			throw new WebApplicationException(e);
		}
	}

	private String insertQuery(Long modelId) {

		try {
			ModelDTO modelDTO = findModel(modelId);
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			bufferedWriter.write("INSERT INTO " + modelDTO.getCode());
			bufferedWriter.newLine();
			bufferedWriter.write("(");
			bufferedWriter.newLine();
			bufferedWriter.write(getInsertColumns(modelId, true));
			bufferedWriter.newLine();
			bufferedWriter.write(")");
			bufferedWriter.newLine();
			bufferedWriter.write("VALUES");
			bufferedWriter.newLine();
			bufferedWriter.write("(");
			bufferedWriter.newLine();
			bufferedWriter.write(getInsertParamenets(modelId, true));
			bufferedWriter.newLine();
			bufferedWriter.write(")");
			bufferedWriter.newLine();
			bufferedWriter.write("RETURNING *");

			bufferedWriter.close();
			return stringWriter.toString();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

	}

	private void prepareParameters(PreparedStatement preparedStatement, Long modelID,
			LinkedHashMap<String, Object> item, Long parentId, boolean insert) throws Exception {
		String parent = findParent(modelID);
		ModelDTO modelDTO = findModel(modelID);
		int index = 0;

		if (!insert) {
			index++;
			preparedStatement.setObject(index, Long.valueOf(item.get("id").toString()));
		}

		index++;
		preparedStatement.setObject(index,
				modelDTO.getNumericCode() ? Long.valueOf(item.get("code").toString()) : item.get("code").toString());

		if (insert) {
			index++;
			preparedStatement.setObject(index, Boolean.valueOf(item.get("lock").toString()));
		}

		if (parent != null && insert) {
			index++;
			preparedStatement.setObject(index, parentId);

		}

		NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
		numberFormat.setMaximumFractionDigits(10);

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

		List<ModelColumnDTO> columnDTOs = findColumns(modelID);

		for (ModelColumnDTO columnDTO : columnDTOs) {
			index++;
			if (item.get(columnDTO.getCode()) == null) {
				preparedStatement.setObject(index, null);
				continue;
			}

			if (item.get(columnDTO.getCode()).toString().length() == 0) {
				preparedStatement.setObject(index, null);
				continue;
			}
			Object value = item.get(columnDTO.getCode());
			switch (columnDTO.getColumnType()) {
			case "STRING": {
				preparedStatement.setObject(index, value.toString());
				break;
			}
			case "INTEGER", "LONG", "BIGDECIMAL": {
				preparedStatement.setObject(index, numberFormat.parse(value.toString()));
				break;
			}
			case "LOCALDATE": {
				preparedStatement.setObject(index, LocalDate.parse(value.toString()));
				break;
			}
			case "LOCALDATETIME": {
				LocalDateTime localDateTime = LocalDateTime.parse(value.toString());
				String date = dateTimeFormatter.format(localDateTime);
				preparedStatement.setObject(index, LocalDateTime.parse(date, dateTimeFormatter));
				break;
			}
			case "BOOLEAN": {
				preparedStatement.setObject(index, Boolean.valueOf(value.toString()));
				break;
			}
			case "CODEBOOK": {
				preparedStatement.setObject(index, Long.valueOf(value.toString()));
				break;
			}
			}

		}
	}

	private String getInsertColumns(Long modelId, boolean insert) {
		try {
			List<ModelColumnDTO> columns = findColumns(modelId);
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			if (!insert) {
				bufferedWriter.write("id,");
				bufferedWriter.newLine();
			}

			bufferedWriter.write("code");

			if (insert) {
				bufferedWriter.write(",");
				bufferedWriter.newLine();
				bufferedWriter.write("lock");
			}

			String parent = findParent(modelId);
			if (parent != null && insert) {
				bufferedWriter.write(",");
				bufferedWriter.newLine();
				bufferedWriter.write(parent);
			}

			for (ModelColumnDTO columnDTO : columns) {
				bufferedWriter.write(",");
				bufferedWriter.newLine();
				bufferedWriter.write(columnDTO.getCode());
			}

			bufferedWriter.close();
			return stringWriter.toString();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	private String getInsertParamenets(Long modelId, boolean insert) {
		try {
			List<ModelColumnDTO> columns = findColumns(modelId);
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			if (!insert) {
				bufferedWriter.write("?,");
				bufferedWriter.newLine();
			}
			bufferedWriter.write("?");

			if (insert) {
				bufferedWriter.write(",");
				bufferedWriter.newLine();
				bufferedWriter.write("?");
			}

			String parent = findParent(modelId);
			if (parent != null && insert) {
				bufferedWriter.write(",");
				bufferedWriter.newLine();
				bufferedWriter.write("?");
			}

			for (int i = 0; i < columns.size(); i++) {
				bufferedWriter.write(",");
				bufferedWriter.newLine();
				bufferedWriter.write("?");
			}

			bufferedWriter.close();
			return stringWriter.toString();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public void getUpdate(Long modelId, LinkedHashMap<String, Object> item, TrackAction trackAction,
			Connection connection) {
		String updateQuery = updateQuery(modelId, Long.valueOf(item.get("id").toString()));
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
			prepareParameters(preparedStatement, modelId, item, null, false);
			preparedStatement.executeUpdate();
			preparedStatement.close();

			insertTrack(modelId, Long.valueOf(item.get("id").toString()), trackAction, connection);

		} catch (Exception e) {

			throw new WebApplicationException(e);
		}
	}

	private String updateQuery(Long modelId, Long id) {
		try {
			ModelDTO modelDTO = findModel(modelId);
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			bufferedWriter.write("UPDATE " + modelDTO.getCode());
			bufferedWriter.newLine();
			bufferedWriter.write("SET");
			bufferedWriter.newLine();
			bufferedWriter.write("(");
			bufferedWriter.newLine();
			bufferedWriter.write(getInsertColumns(modelId, false));
			bufferedWriter.newLine();
			bufferedWriter.write(")");
			bufferedWriter.newLine();
			bufferedWriter.write("=");
			bufferedWriter.newLine();
			bufferedWriter.write("(");
			bufferedWriter.newLine();
			bufferedWriter.write(getInsertParamenets(modelId, false));
			bufferedWriter.newLine();
			bufferedWriter.write(")");
			bufferedWriter.newLine();
			bufferedWriter.write("WHERE id=" + id);

			bufferedWriter.close();
			return stringWriter.toString();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public void insertTrack(Long modelId, Long id, TrackAction trackAction, Connection connection) {

		Track track = new Track();
		track.setAction(trackAction);
		track.setAddress(commonService.getIpAddress());
		track.setAppUser(commonService.getAppUser());
		track.setDataid(id);
		track.setId(Long.valueOf(0));
		track.setTableName(findModel(modelId).getCode());
		track.setTime(LocalDateTime.now());

		this.datatableService.save(track, connection);
	}

	@Override
	public void getDelete(Long modelId, Long id, Connection connection) {
		ModelDTO modelDTO = findModel(modelId);
		String deleteQuery = "delete from " + modelDTO.getCode() + " where id=" + id;

		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(deleteQuery);
			statement.close();

			insertTrack(modelId, id, TrackAction.DELETE, connection);
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public void getLock(Long modelId, Long id, Connection connection) {
		ModelDTO modelDTO = findModel(modelId);

		String updateQuery = "update " + modelDTO.getCode() + " set lock=true ";
		updateQuery += "where id=" + id;

		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(updateQuery);
			statement.close();

			insertTrack(modelId, id, TrackAction.LOCK, connection);
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

	}

	@Override
	public void getUnlock(Long modelId, Long id, Connection connection) {
		ModelDTO modelDTO = findModel(modelId);

		String updateQuery = "update " + modelDTO.getCode() + " set lock=false ";
		updateQuery += "where id=" + id;

		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(updateQuery);
			statement.close();

			insertTrack(modelId, id, TrackAction.UNLOCK, connection);
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

	}

	@Override
	public Boolean checkLockParent(Long modelId, Long id, Long parentId, Connection connection) {
		try {
			if (parentId == -1) {
				return false;
			}

			List<ModelDTO> parents = new ArrayList<>();
			ModelDTO modelDTOSearch = findModel(modelId);
			while (modelDTOSearch.getParentId() != null) {
				modelDTOSearch = findModel(modelDTOSearch.getParentId());

				if (modelDTOSearch.getType().equals(ModelType.TABLE.name())) {
					parents.add(modelDTOSearch);
				}
			}

			if (parents.isEmpty()) {
				return false;
			}

			ModelDTO modelDTO = findModel(modelId);

			if (parentId == 0) {
				String findQuery = "select " + modelDTO.getParentCode() + " from " + modelDTO.getCode() + " where id="
						+ id;

				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(findQuery);
				resultSet.next();

				parentId = ((Number) resultSet.getObject(1)).longValue();

				resultSet.close();
				statement.close();
			}

			Long idSearch = parentId;
			String tableName = "";
			String parentName = "";

			for (ModelDTO parent : parents) {

				tableName = parent.getCode();
				parentName = parent.getParentCode();

				String lockQuery = "select lock from " + tableName + " where id=" + idSearch;

				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(lockQuery);
				resultSet.next();

				if ((Boolean) resultSet.getObject(1)) {
					return true;
				}

				resultSet.close();
				statement.close();

				if (parent.getParentType().equals(ModelType.MENU.name())) {
					continue;
				}

				String findQuery = "select " + parentName + " from " + tableName + " where id=" + idSearch;

				statement = connection.createStatement();
				resultSet = statement.executeQuery(findQuery);
				resultSet.next();

				idSearch = ((Number) resultSet.getObject(1)).longValue();

				resultSet.close();
				statement.close();

			}

			return false;

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

	}

	@Override
	public List<LinkedHashMap<String, Object>> findAllExcelCodebook(Long modelId, Connection connection) {
		List<LinkedHashMap<String, Object>> list = new ArrayList<>();
		ModelDTO modelDTO = findModel(modelId);
		@SuppressWarnings("unchecked")
		List<FieldInfo> fieldInfos = (List<FieldInfo>) createExcelColumns(modelDTO)[0];
		@SuppressWarnings("unchecked")
		List<ModelColumnDTO> modelColumnDTOs = (List<ModelColumnDTO>) createExcelColumns(modelDTO)[1];
		List<LeftTable> leftTables = queryWriterService.leftJoinTable(fieldInfos);

		String select = queryWriterService.selectPart(fieldInfos);
		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		String from = queryWriterService.fromPart(modelDTO.getCode(), leftTables);
		String where = queryWriterService.wherePart(tableParameterDTO, fieldInfos);
		String order = queryWriterService.orderPart(tableParameterDTO, fieldInfos);
		String query = select + from + where + order;
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			while (resultSet.next()) {
				list.add(resultSetToHashMap(modelDTO, modelColumnDTOs, resultSet));
			}

			resultSet.close();
			statement.close();
			return list;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	public Object[] createExcelColumns(ModelDTO modelDTO) {
		List<FieldInfo> fieldInfos = new ArrayList<>();

		List<ModelColumnDTO> modelColumnDTOs = new ArrayList<>();

		// code
		FieldInfo fieldInfoCode = new FieldInfo();
		fieldInfoCode.getColumnList().add("code");
		fieldInfoCode.setFieldName("code");
		fieldInfoCode.setOrdernum(1);
		fieldInfoCode.setSortDirection(SortDirection.ASC);
		fieldInfoCode.setFieldType(modelDTO.getNumericCode() ? "Long" : "String");
		fieldInfos.add(fieldInfoCode);
		ModelColumnDTO modelColumnD = new ModelColumnDTO();
		modelColumnD.setCode("code");
		modelColumnD.setColumnType(modelDTO.getNumericCode() ? "LONG" : "STRING");
		modelColumnDTOs.add(modelColumnD);

		// parent
		ModelDTO searchModelDTO = modelDTO;
		int orderNum = 1;
		List<ModelDTO> parents = new ArrayList<>();
		while (searchModelDTO.getParentId() != null && searchModelDTO.getParentType().equals(ModelType.TABLE.name())) {
			Long parentId = searchModelDTO.getParentId();
			orderNum++;
			ModelDTO foundModel = findModel(parentId);

			parents.add(foundModel);
			searchModelDTO = foundModel;
		}

		String path = "";
		List<String> columnList = new ArrayList<>();
		List<LeftTableData> leftTables = new ArrayList<>();
		for (int i = 0; i < parents.size(); i++) {
			ModelDTO modelColumnDTO = parents.get(i);
			columnList.add(modelColumnDTO.getCode());
			columnList.add("code");

			FieldInfo fieldInfoParent = new FieldInfo();
			fieldInfoParent.setColumnList(new ArrayList<>(columnList));
			fieldInfoParent.setOrdernum(orderNum);
			fieldInfoParent.setSortDirection(SortDirection.ASC);
			String fieldName = "";
			for (String columnName : fieldInfoParent.getColumnList()) {
				fieldName += columnName;
			}

			fieldInfoParent.setFieldName(fieldName);
			fieldInfoParent.setFieldType(modelColumnDTO.getNumericCode() ? "Long" : "String");

			LeftTableData leftTableData = new LeftTableData();
			leftTableData.setFieldColumn(modelColumnDTO.getCode());
			leftTableData.setIdColumn("id");
			leftTableData.setTable(modelColumnDTO.getCode());
			path += "/" + modelColumnDTO.getCode();
			leftTableData.setPath(new String(path));

			leftTables.add(leftTableData);
			fieldInfoParent.setLeftTableDatas(new ArrayList<>(leftTables));

			fieldInfoParent.setLeftJoinPath(new String(path));
			fieldInfos.add(fieldInfoParent);
			modelColumnD = new ModelColumnDTO();
			modelColumnD.setCode(fieldName);
			modelColumnD.setColumnType(modelColumnDTO.getNumericCode() ? "LONG" : "STRING");
			modelColumnDTOs.add(modelColumnD);
		}

		List<ModelColumnDTO> columns = ModelData.listColumnDTOs.stream().filter(a -> a.getModelId() == modelDTO.getId())
				.filter(a -> a.getShowInTable()).sorted(Comparator.comparing(ModelColumnDTO::getRowNumber)
						.thenComparing(ModelColumnDTO::getColumnNumber))
				.toList();
		addOtherColumns(fieldInfos, columns);
		modelColumnDTOs.addAll(columns);
		return new Object[] { fieldInfos, modelColumnDTOs };
	}

	@Override
	public void checkTotal(TableDataDTO<LinkedHashMap<String, Object>> tableDataDTO,
			TableParameterDTO tableParameterDTO, Long modelId, Connection connection) {
		ModelDTO modelDTO = findModel(modelId);
		List<ModelColumnDTO> columnDTOs = findColumns(modelId);

		getTotalQuery(tableDataDTO, tableParameterDTO, modelDTO, columnDTOs, connection);

	}

	@Override
	public LinkedHashMap<String, List<ComboboxDTO>> createListOfValue(Long modelId, Connection connection) {
		LinkedHashMap<String, List<ComboboxDTO>> map = new LinkedHashMap<>();
		List<ModelColumnDTO> columnDTOs = ModelData.listColumnDTOs.stream().filter(a -> a.getModelId() == modelId)
				.filter(a -> a.getListOfValues() != null).filter(a -> a.getListOfValues().length() > 0).toList();
		for (ModelColumnDTO modelColumnDTO : columnDTOs) {
			try {
				List<ComboboxDTO> list = new ArrayList<>();
				String columnCode = modelColumnDTO.getCode();

				Statement st = connection.createStatement();
				ResultSet resultSet = st.executeQuery(modelColumnDTO.getListOfValues());

				while (resultSet.next()) {
					Object value = resultSet.getObject(1);
					String option = resultSet.getMetaData().getColumnCount() > 1 ? resultSet.getObject(2).toString()
							: value.toString();
					list.add(new ComboboxDTO(value, resourceBundleService.getText(option, null)));
				}

				map.put(columnCode, list);

				resultSet.close();
				st.close();

			} catch (Exception e) {
				throw new WebApplicationException(e);
			}
		}
		return map;
	}
}
