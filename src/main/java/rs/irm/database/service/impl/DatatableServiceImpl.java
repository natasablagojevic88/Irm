package rs.irm.database.service.impl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import rs.irm.common.entity.Track;
import rs.irm.common.enums.TrackAction;
import rs.irm.common.exceptions.NoDataFoundException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.common.service.impl.AppInitServiceImpl;
import rs.irm.common.service.impl.CommonServiceImpl;
import rs.irm.common.service.impl.ResourceBundleServiceImpl;
import rs.irm.database.annotations.DecimalNumber;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.annotations.EnumField;
import rs.irm.database.annotations.SkipField;
import rs.irm.database.annotations.TableHide;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.ColumnType;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.QueryWriterService;
import rs.irm.database.utils.ColumnData;
import rs.irm.database.utils.EnumList;
import rs.irm.database.utils.ExecuteMethod;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.database.utils.FieldInfo;
import rs.irm.database.utils.LeftTable;
import rs.irm.database.utils.TableFilter;
import rs.irm.utils.AppConnections;
import rs.irm.utils.AppParameters;

@Named
public class DatatableServiceImpl implements DatatableService {

	@Context
	private HttpServletRequest httpServletRequest;

	private QueryWriterService queryWriterService = new QueryWriterServiceImpl();

	private ResourceBundleService resourceBundleService = new ResourceBundleServiceImpl();

	@Inject
	private CommonService commonService;

	public DatatableServiceImpl() {
	}

	public DatatableServiceImpl(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
		this.commonService = new CommonServiceImpl(httpServletRequest);
		this.resourceBundleService = new ResourceBundleServiceImpl(httpServletRequest);
	}

	private Connection getConnection() {
		try {

			Connection connection = null;
			List<Connection> connections = new ArrayList<>(AppConnections.freeConnections);
			for (int i = 0; i < connections.size(); i++) {
				Connection connectionIn = connections.get(i);
				try {
					Statement statement = connectionIn.createStatement();
					ResultSet rs = statement.executeQuery(AppParameters.checkconnection);
					rs.close();
					statement.close();

					connection = connectionIn;
					AppConnections.freeConnections.remove(connectionIn);
					break;
				} catch (Exception e) {
					if (AppConnections.allConnections.contains(connectionIn)) {
						AppConnections.allConnections.remove(connectionIn);
					}
					AppConnections.freeConnections.remove(connectionIn);
				}
			}

			new AppInitServiceImpl().initConnections();

			if (connection == null) {
				return getConnection();
			} else {
				return connection;
			}

		} catch (Exception e) {

			throw new WebApplicationException(e);
		}
	}

	@Override
	public <C> TableDataDTO<C> getTableDataDTO(TableParameterDTO tableParameterDTO, Class<C> inClass) {
		try {

			Connection connection = null;

			try {
				connection = getConnection();

				TableDataDTO<C> tableDataDTO = getTableDataDTO(tableParameterDTO, inClass, connection);

				connection.commit();
				AppConnections.freeConnections.add(connection);
				return tableDataDTO;
			} catch (Exception e) {
				connection.rollback();
				AppConnections.freeConnections.add(connection);
				throw e;
			}

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public <C> TableDataDTO<C> getTableDataDTO(TableParameterDTO tableParameterDTO, Class<C> inClass,
			Connection connection) {
		try {

			TableDataDTO<C> tableDataDTO = new TableDataDTO<>();
			addColumnToTableDataDTO(tableDataDTO, inClass);
			List<C> list = findAll(tableParameterDTO, inClass, connection);
			tableDataDTO.setList(list);

			String query = "select count(*)\n";
			List<FieldInfo> fieldInfos = queryWriterService.pathField(inClass);
			List<LeftTable> leftTables = queryWriterService.leftJoinTable(fieldInfos);
			String tableName = "";
			if (inClass.isAnnotationPresent(EntityClass.class)) {
				EntityClass entityClass = inClass.getAnnotation(EntityClass.class);
				tableName = entityClass.value().getAnnotation(Table.class).name();
			} else {
				tableName = inClass.getAnnotation(Table.class).name();
			}
			tableDataDTO.setTable(tableName);
			String fromPart = queryWriterService.fromPart(tableName, leftTables);
			String wherePart = queryWriterService.wherePart(tableParameterDTO, fieldInfos);
			query += fromPart;
			query += wherePart;
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			resultSet.next();
			Number totalElement = (Number) resultSet.getObject(1);

			resultSet.close();
			statement.close();

			tableDataDTO.setTotalElements(totalElement.longValue());
			Long numberOfPages = totalElement.longValue() / tableParameterDTO.getPageSize();
			if (totalElement.longValue() % tableParameterDTO.getPageSize() != 0) {
				numberOfPages = numberOfPages + 1;
			}
			tableDataDTO.setTotalPages(numberOfPages.intValue());

			return tableDataDTO;

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	private <C> void addColumnToTableDataDTO(TableDataDTO<C> tableDataDTO, Class<C> inClass) {

		try {
			resourceBundleService = new ResourceBundleServiceImpl(httpServletRequest);

			tableDataDTO.setTitle(resourceBundleService.getText(inClass.getSimpleName() + ".title", null));

			LinkedHashMap<String, String> namesMap = new LinkedHashMap<>();
			List<EnumList> enumLists = new ArrayList<>();

			for (Field field : Arrays.asList(inClass.getDeclaredFields())) {
				field.setAccessible(true);

				namesMap.put(field.getName(),
						resourceBundleService.getText(inClass.getSimpleName() + "." + field.getName(), null));

				if (field.isAnnotationPresent(TableHide.class)) {
					continue;
				}

				if (field.isAnnotationPresent(SkipField.class)) {
					continue;
				}

				ColumnData columnData = new ColumnData();
				columnData.setCode(field.getName());
				columnData.setColumnType(ColumnType.valueOf(field.getType().getSimpleName()));
				columnData
						.setName(resourceBundleService.getText(inClass.getSimpleName() + "." + field.getName(), null));

				if (columnData.getColumnType().equals(ColumnType.BigDecimal)) {
					columnData.setNumberOfDecimal(2);

					if (field.isAnnotationPresent(DecimalNumber.class)) {
						DecimalNumber decimalNumber = field.getAnnotation(DecimalNumber.class);
						columnData.setNumberOfDecimal(decimalNumber.value());
					}
				}

				if (field.isAnnotationPresent(EnumField.class)) {
					EnumField enumField = field.getAnnotation(EnumField.class);
					CommonService commonServiceEnum = commonService == null
							? new CommonServiceImpl(this.httpServletRequest)
							: commonService;

					EnumList enumList = new EnumList();
					enumList.setCode(field.getName());
					enumList.setList(commonServiceEnum.enumToCombobox(enumField.value()));
					enumLists.add(enumList);

				}

				tableDataDTO.getColumns().add(columnData);
			}

			tableDataDTO.setNames(namesMap);
			tableDataDTO.setEnums(enumLists);

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public <C> List<C> findAll(TableParameterDTO tableParameterDTO, Class<C> inClass) {
		try {
			Connection connection = null;

			try {
				connection = getConnection();
				List<C> list = findAll(tableParameterDTO, inClass, connection);
				connection.commit();
				if (!AppConnections.freeConnections.contains(connection)) {
					AppConnections.freeConnections.add(connection);
				}
				return list;
			} catch (Exception e) {
				connection.rollback();
				if (!AppConnections.freeConnections.contains(connection)) {
					AppConnections.freeConnections.add(connection);
				}
				throw e;
			}
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public <C> List<C> findAll(TableParameterDTO tableParameterDTO, Class<C> inClass, Connection connection) {
		try {
			List<C> list = new ArrayList<>();

			List<FieldInfo> filedInfos = queryWriterService.pathField(inClass);
			List<LeftTable> leftTables = queryWriterService.leftJoinTable(filedInfos);

			String tableName = "";
			if (inClass.isAnnotationPresent(EntityClass.class)) {
				EntityClass entityClass = inClass.getAnnotation(EntityClass.class);
				tableName = entityClass.value().getAnnotation(Table.class).name();
			} else {
				tableName = inClass.getAnnotation(Table.class).name();
			}

			String selectPart = queryWriterService.selectPart(filedInfos);
			String fromPart = queryWriterService.fromPart(tableName, leftTables);
			String wherePart = queryWriterService.wherePart(tableParameterDTO, filedInfos);
			String orderPart = queryWriterService.orderPart(tableParameterDTO, filedInfos);
			String pageablePart = queryWriterService.pageablePart(tableParameterDTO);

			String query = selectPart + fromPart + wherePart + orderPart + pageablePart;
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			while (resultSet.next()) {
				list.add(resultSetToObject(resultSet, inClass, connection));
			}

			resultSet.close();
			statement.close();

			return list;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public <C> Optional<C> findById(Long id, Class<C> inClass) {
		try {
			Connection connection = null;

			try {
				connection = getConnection();

				Optional<C> objectOptional = findById(id, inClass, connection);

				connection.commit();
				if (!AppConnections.freeConnections.contains(connection)) {
					AppConnections.freeConnections.add(connection);
				}
				return objectOptional;
			} catch (Exception e) {
				connection.rollback();
				if (!AppConnections.freeConnections.contains(connection)) {
					AppConnections.freeConnections.add(connection);
				}
				throw e;
			}
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public <C> Optional<C> findById(Long id, Class<C> inClass, Connection connection) {
		try {
			TableFilter tableFilter = new TableFilter();
			tableFilter.setField(findPrimaryField(inClass).getName());
			tableFilter.setSearchOperation(SearchOperation.equals);
			tableFilter.setParameter1(String.valueOf(id));

			TableParameterDTO tableParameterDTO = new TableParameterDTO();
			tableParameterDTO.getTableFilters().add(tableFilter);

			C object = null;

			List<C> list = findAll(tableParameterDTO, inClass, connection);
			if (!list.isEmpty()) {
				object = list.get(0);
			}
			return Optional.ofNullable(object);
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public <C> C findByExistingId(Long id, Class<C> inClass) {
		try {

			Connection connection = null;

			try {
				connection = getConnection();

				C objectC = findByExistingId(id, inClass, connection);

				connection.commit();
				if (!AppConnections.freeConnections.contains(connection)) {
					AppConnections.freeConnections.add(connection);
				}
				return objectC;
			} catch (Exception e) {
				connection.rollback();
				if (!AppConnections.freeConnections.contains(connection)) {
					AppConnections.freeConnections.add(connection);
				}
				throw e;
			}

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public <C> C findByExistingId(Long id, Class<C> inClass, Connection connection) {
		try {

			C objectC = findById(id, inClass, connection).orElseThrow(() -> new NoDataFoundException(id, inClass));

			return objectC;

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public <C> C save(C entity) {
		try {

			Connection connection = null;

			try {
				connection = getConnection();

				C objectC = save(entity, connection);

				connection.commit();
				AppConnections.freeConnections.add(connection);
				return objectC;
			} catch (Exception e) {
				connection.rollback();
				AppConnections.freeConnections.add(connection);
				throw e;
			}

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public <C> C save(C entity, Connection connection) {
		try {
			Field primaryField = findPrimaryField(entity.getClass());
			Long idValue = (Long) primaryField.get(entity);
			C objectC = null;
			if (idValue == 0) {
				objectC = getInsert(entity, connection);
			} else {
				objectC = getUpdate(entity, connection);
			}

			return objectC;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <C> C getInsert(C entity, Connection connection) {
		try {

			String query = queryWriterService.insertQuery(entity);

			PreparedStatement preparedStatement = connection.prepareStatement(query);
			getPrepareParameters(entity, preparedStatement);
			ResultSet resultSet = preparedStatement.executeQuery();
			resultSet.next();
			C objectC = (C) resultSetToObject(resultSet, entity.getClass(), connection);
			resultSet.close();
			preparedStatement.close();

			if (!entity.getClass().getCanonicalName().equals(Track.class.getCanonicalName())) {
				insertToTrack(objectC, TrackAction.INSERT, connection);
			}

			return objectC;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	private <C> void insertToTrack(C entity, TrackAction trackAction, Connection connection) {

		Track track = new Track();
		track.setAction(trackAction);
		track.setAddress(commonService == null ? new CommonServiceImpl(httpServletRequest).getIpAddress()
				: commonService.getIpAddress());
		track.setAppUser(commonService == null ? new CommonServiceImpl(httpServletRequest).getAppUser()
				: commonService.getAppUser());
		try {
			track.setDataid((Long) findPrimaryField(entity.getClass()).get(entity));
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

		track.setId(Long.valueOf(0));
		track.setTableName(entity.getClass().getAnnotation(Table.class).name());
		track.setTime(LocalDateTime.now());

		save(track, connection);
	}

	@SuppressWarnings("unchecked")
	private <C> C getUpdate(C entity, Connection connection) {
		try {
			findByExistingId((Long) findPrimaryField(entity.getClass()).get(entity), entity.getClass(), connection);
			String query = queryWriterService.updateQuery(entity);

			PreparedStatement preparedStatement = connection.prepareStatement(query);
			getPrepareParameters(entity, preparedStatement);
			ResultSet resultSet = preparedStatement.executeQuery();
			resultSet.next();
			C objectC = (C) resultSetToObject(resultSet, entity.getClass(), connection);
			resultSet.close();
			preparedStatement.close();

			insertToTrack(objectC, TrackAction.UPDATE, connection);

			return objectC;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	private <C> void getPrepareParameters(C entity, PreparedStatement preparedStatement) {
		try {
			List<Field> fields = Arrays.asList(entity.getClass().getDeclaredFields());
			int counter = 0;
			for (Field field : fields) {

				field.setAccessible(true);
				if (!(field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(JoinColumn.class))) {
					continue;
				}

				counter++;
				String fieldType = field.getType().getSimpleName();
				if (QueryWriterServiceImpl.fieldTypes.contains(fieldType)) {
					preparedStatement.setObject(counter, field.get(entity));

					if (field.get(entity) != null) {

						if (fieldType.equals("String") && field.get(entity).toString().length() == 0) {
							preparedStatement.setObject(counter, null);
						}
					}

				} else {
					if (field.get(entity) != null) {
						Class<?> fieldClass = Class.forName(field.getType().getCanonicalName());

						if (fieldClass.isEnum()) {
							Enum<?> enumValue = (Enum<?>) field.get(entity);
							preparedStatement.setObject(counter, enumValue.name());
						} else {
							Field primaryField = findPrimaryField(fieldClass);
							preparedStatement.setObject(counter, primaryField.get(field.get(entity)));
						}
					} else {
						preparedStatement.setObject(counter, null);
					}

				}

			}

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public <C> void delete(C entity) {
		try {

			Connection connection = null;

			try {
				connection = getConnection();
				delete(entity, connection);
				connection.commit();
				if (!AppConnections.freeConnections.contains(connection)) {
					AppConnections.freeConnections.add(connection);
				}
			} catch (Exception e) {
				connection.rollback();
				if (!AppConnections.freeConnections.contains(connection)) {
					AppConnections.freeConnections.add(connection);
				}
				throw e;
			}

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public <C> void delete(C entity, Connection connection) {
		try {
			findByExistingId((Long) findPrimaryField(entity.getClass()).get(entity), entity.getClass(), connection);
			String deleteString = queryWriterService.deleteQuery(entity);

			Statement statement = connection.createStatement();
			statement.executeUpdate(deleteString);
			statement.close();

			insertToTrack(entity, TrackAction.DELETE, connection);

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <C> C resultSetToObject(ResultSet resultSet, Class<C> inClass, Connection connection) {

		try {
			C objectC = inClass.getConstructor().newInstance();

			Map<String, Field> columnNames = new HashMap<>();

			for (Field field : Arrays.asList(inClass.getDeclaredFields())) {
				field.setAccessible(true);
				if (field.isAnnotationPresent(Column.class)) {
					Column column = field.getAnnotation(Column.class);
					if (column.name().length() > 0) {
						columnNames.put(column.name(), field);
						continue;
					}
				}

				if (field.isAnnotationPresent(JoinColumn.class)) {
					JoinColumn column = field.getAnnotation(JoinColumn.class);
					if (column.name().length() > 0) {
						columnNames.put(column.name(), field);
						continue;
					}
				}

				columnNames.put(field.getName(), field);
			}

			for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
				String columnName = resultSet.getMetaData().getColumnName(i);
				Field field = null;
				try {
					field = inClass.getDeclaredField(columnName);
				} catch (Exception e) {
					field = columnNames.get(columnName);
				}

				field.setAccessible(true);
				if (resultSet.getObject(i) == null) {
					continue;
				}

				String fieldType = field.getType().getSimpleName();

				switch (fieldType) {
				case "String":
					field.set(objectC, resultSet.getObject(i).toString());

					break;
				case "Long":
					Number numberLong = (Number) resultSet.getObject(i);
					field.set(objectC, numberLong.longValue());
					break;
				case "Integer":
					Number numberInteger = (Number) resultSet.getObject(i);
					field.set(objectC, numberInteger.intValue());
					break;
				case "BigDecimal":
					Number numberBigDecimal = (Number) resultSet.getObject(i);
					field.set(objectC, BigDecimal.valueOf(numberBigDecimal.doubleValue()));
					break;
				case "Boolean":
					field.set(objectC, Boolean.valueOf(resultSet.getObject(i).toString()));
					break;
				case "LocalDate":
					Date date = (Date) resultSet.getObject(i);
					field.set(objectC, date.toLocalDate());
					break;
				case "LocalDateTime":
					Timestamp dateTime = (Timestamp) resultSet.getObject(i);
					field.set(objectC, dateTime.toLocalDateTime());
					break;
				case "byte[]":
					field.set(objectC, resultSet.getObject(i));
					break;
				default:
					@SuppressWarnings("rawtypes")
					Class fieldClass = Class.forName(field.getType().getCanonicalName());
					if (field.getType().isEnum()) {
						String enumValue = resultSet.getObject(i).toString();

						field.set(objectC, Enum.valueOf(fieldClass, enumValue));

					} else {
						Number numberId = (Number) resultSet.getObject(i);
						field.set(objectC, findByExistingId(numberId.longValue(), fieldClass, connection));
					}
					break;

				}

			}

			return objectC;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	private Field findPrimaryField(Class<?> inClass) {
		try {
			for (Field field : Arrays.asList(inClass.getDeclaredFields())) {
				if (field.isAnnotationPresent(Id.class)) {
					field.setAccessible(true);
					return field;
				}
			}
			return null;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public void executeMethod(ExecuteMethod executeMethod) {
		try {
			Connection connection = null;
			try {
				connection = getConnection();
				executeMethod(executeMethod, connection);
				connection.commit();
				if (!AppConnections.freeConnections.contains(connection)) {
					AppConnections.freeConnections.add(connection);
				}
			} catch (Exception e) {
				connection.rollback();
				if (!AppConnections.freeConnections.contains(connection)) {
					AppConnections.freeConnections.add(connection);
				}
				throw e;
			}
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

	}

	@Override
	public void executeMethod(ExecuteMethod executeMethod, Connection connection) {
		try {
			executeMethod.execute(connection);
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

	}

	@Override
	public <C> C executeMethodWithReturn(ExecuteMethodWithReturn<C> executeMethodWithReturn) {
		try {
			Connection connection = null;
			try {
				connection = getConnection();
				C returnObject = executeMethodWithReturn(executeMethodWithReturn, connection);
				connection.commit();
				if (!AppConnections.freeConnections.contains(connection)) {
					AppConnections.freeConnections.add(connection);
				}

				return returnObject;
			} catch (Exception e) {
				connection.rollback();
				if (!AppConnections.freeConnections.contains(connection)) {
					AppConnections.freeConnections.add(connection);
				}
				throw e;
			}
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public <C> C executeMethodWithReturn(ExecuteMethodWithReturn<C> executeMethodWithReturn, Connection connection) {
		C returnObject = executeMethodWithReturn.execute(connection);
		return returnObject;
	}

}
