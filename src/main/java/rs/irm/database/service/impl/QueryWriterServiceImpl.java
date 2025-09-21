package rs.irm.database.service.impl;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.database.annotations.EntityClass;
import rs.irm.database.annotations.SkipField;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.InitSort;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.QueryWriterService;
import rs.irm.database.utils.FieldInfo;
import rs.irm.database.utils.LeftTable;
import rs.irm.database.utils.LeftTableData;
import rs.irm.database.utils.TableFilter;
import rs.irm.database.utils.TableSort;

public class QueryWriterServiceImpl implements QueryWriterService {
	private String bigLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static List<String> fieldTypes = new ArrayList<String>() {
		{
			add("String");
			add("Long");
			add("Integer");
			add("BigDecimal");
			add("LocalDate");
			add("LocalDateTime");
			add("Boolean");
			add("byte[]");
		}
	};

	@Override
	public List<FieldInfo> pathField(Class<?> inClass) {

		try {
			List<FieldInfo> list = new ArrayList<>();

			for (Field field : Arrays.asList(inClass.getDeclaredFields())) {
				field.setAccessible(true);

				if (field.isAnnotationPresent(SkipField.class)) {
					continue;
				}

				FieldInfo fieldInfo = new FieldInfo();
				Class<?> entityClass = inClass;
				if (inClass.isAnnotationPresent(EntityClass.class)) {
					entityClass = inClass.getAnnotation(EntityClass.class).value();
				}

				String fieldName = field.getName();
				fieldInfo.setFieldName(fieldName);

				if (field.isAnnotationPresent(InitSort.class)) {
					InitSort initSort = field.getAnnotation(InitSort.class);
					fieldInfo.setOrdernum(initSort.value());
					fieldInfo.setSortDirection(initSort.sortDirection());

				}

				List<Integer> bigLettersIndex = new ArrayList<>();
				for (int i = 0; i < fieldName.length(); i++) {
					char letter = fieldName.charAt(i);
					if (bigLetters.contains(String.valueOf(letter))) {
						bigLettersIndex.add(i);
					}
				}
				bigLettersIndex.add(fieldName.length());
				int startIndex = 0;
				int counter = 0;
				String leftPath = "";
				for (Integer index : bigLettersIndex) {
					counter++;
					String fieldCheck = fieldName.substring(startIndex, index);
					String fieldCheckFirstLeter = fieldCheck.substring(0, 1).toLowerCase();
					String fieldCheckLastLeters = fieldCheck.substring(1);
					fieldCheck = fieldCheckFirstLeter + fieldCheckLastLeters;
					try {
						Field fieldFound = entityClass.getDeclaredField(fieldCheck);
						String columnName = fieldFound.getName();
						if (fieldFound.isAnnotationPresent(Column.class)) {
							Column column = fieldFound.getAnnotation(Column.class);
							if (column.name().length() > 0) {
								columnName = column.name();
							}
						}

						if (fieldFound.isAnnotationPresent(JoinColumn.class)) {
							JoinColumn column = fieldFound.getAnnotation(JoinColumn.class);
							if (column.name().length() > 0) {
								columnName = column.name();
							}
						}
						fieldInfo.getColumnList().add(columnName);
						String fieldType = fieldFound.getType().getSimpleName();
						if (!fieldTypes.contains(fieldType)) {
							Class<?> fieldClass = Class.forName(fieldFound.getType().getCanonicalName());
							if (!fieldClass.isEnum()) {
								fieldInfo.setFieldType("Long");
							} else {
								fieldInfo.setFieldType("String");
							}
						} else {
							fieldInfo.setFieldType(fieldType);
						}
						if (counter == bigLettersIndex.size()) {
							continue;
						}
						leftPath += "/" + fieldCheck;

						if (!fieldTypes.contains(fieldType)) {
							Class<?> fieldClass = Class.forName(fieldFound.getType().getCanonicalName());
							if (!fieldClass.isEnum()) {
								LeftTableData leftTableData = new LeftTableData();
								leftTableData.setFieldColumn(columnName);
								leftTableData.setIdColumn(findPrimaryField(fieldClass).getName());
								leftTableData.setPath(new String(leftPath));
								leftTableData.setTable(fieldClass.getAnnotation(Table.class).name());
								fieldInfo.getLeftTableDatas().add(leftTableData);
								entityClass = fieldClass;
							}
						}

						startIndex = index;
					} catch (Exception e) {

					}

				}
				fieldInfo.setLeftJoinPath(leftPath);
				list.add(fieldInfo);

			}

			return list;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public List<LeftTable> leftJoinTable(List<FieldInfo> fieldInfos) {
		try {
			List<LeftTable> leftTables = new ArrayList<>();
			Map<String, String> pathAliasMap = new HashMap<>();
			int aliasCounter = 0;
			for (FieldInfo fieldInfo : fieldInfos) {
				fieldInfo.setColumnName(fieldInfo.getColumnList().get(fieldInfo.getColumnList().size() - 1));
				if (fieldInfo.getLeftTableDatas().isEmpty()) {
					fieldInfo.setAlias("a");
					continue;
				}
				String aliasStart = "a";
				for (LeftTableData leftTableData : fieldInfo.getLeftTableDatas()) {
					if (pathAliasMap.containsKey(leftTableData.getPath())) {
						aliasStart = pathAliasMap.get(leftTableData.getPath());
						continue;
					}
					aliasCounter++;

					LeftTable leftTable = new LeftTable();
					leftTable.setAlias("a" + aliasCounter);
					leftTable.setTable(leftTableData.getTable());
					leftTable.setFieldAlias(new String(aliasStart));
					aliasStart = "a" + aliasCounter;
					leftTable.setFieldColumn(leftTableData.getFieldColumn());
					leftTable.setPath(leftTableData.getPath());
					leftTable.setIdColumn(leftTableData.getIdColumn());
					pathAliasMap.put(leftTableData.getPath(), leftTable.getAlias());
					leftTables.add(leftTable);

				}

				fieldInfo.setAlias(pathAliasMap.get(fieldInfo.getLeftJoinPath()));
			}

			return leftTables;
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
	public String selectPart(List<FieldInfo> fieldInfos) {
		try {
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			bufferedWriter.write("select");
			bufferedWriter.newLine();
			int index = 0;
			for (FieldInfo fieldIField : fieldInfos) {
				index++;
				if(fieldIField.getSqlMetric()!=null) {
					bufferedWriter.write(fieldIField.getSqlMetric().name()+"(");
				}
				bufferedWriter.write(createFieldName(fieldIField.getFieldName(), fieldInfos));
				if(fieldIField.getSqlMetric()!=null) {
					bufferedWriter.write(")");
				}
				bufferedWriter.write(" \""
						+ fieldIField.getFieldName() + "\"");
				if (index != fieldInfos.size()) {
					bufferedWriter.write(",");
					bufferedWriter.newLine();
				}
			}

			bufferedWriter.close();
			return stringWriter.toString();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	private String createFieldName(String fieldName, List<FieldInfo> fieldInfos) {

		FieldInfo fieldPath = fieldInfos.stream().filter(a -> a.getFieldName().equals(fieldName)).findFirst().get();
		String fieldNameColumn = fieldPath.getAlias() + "." + fieldPath.getColumnName();
		return fieldNameColumn;
	}

	@Override
	public String fromPart(String tableName, List<LeftTable> leftTables) {
		try {
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			bufferedWriter.newLine();
			bufferedWriter.write("from");
			bufferedWriter.newLine();

			bufferedWriter.write(tableName + " a");

			for (LeftTable leftTable : leftTables) {
				bufferedWriter.newLine();
				bufferedWriter.write("left join " + leftTable.getTable() + " " + leftTable.getAlias());
				bufferedWriter.newLine();
				bufferedWriter.write("on " + leftTable.getFieldAlias() + "." + leftTable.getFieldColumn() + "="
						+ leftTable.getAlias() + "." + leftTable.getIdColumn() + "");

			}

			bufferedWriter.close();
			return stringWriter.toString();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public String wherePart(TableParameterDTO tableParameterDTO, List<FieldInfo> fieldInfos) {
		try {
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			if (tableParameterDTO.getTableFilters().isEmpty()) {
				bufferedWriter.close();
				return stringWriter.toString();
			}

			List<TableFilter> tableFilters = tableParameterDTO.getTableFilters();

			int index = 0;
			for (TableFilter tableFilter : tableFilters) {
				if (!(tableFilter.getSearchOperation().equals(SearchOperation.isnull)
						|| tableFilter.getSearchOperation().equals(SearchOperation.isnotnull))) {
					if (tableFilter.getParameter1() == null) {
						continue;
					}

					if (tableFilter.getParameter1().length() == 0) {
						continue;
					}

					if (tableFilter.getSearchOperation().equals(SearchOperation.between)
							&& tableFilter.getParameter2() == null) {
						continue;
					}

					if (tableFilter.getSearchOperation().equals(SearchOperation.between)
							&& tableFilter.getParameter2().length() == 0) {
						continue;
					}
				}
				bufferedWriter.newLine();
				index++;
				if (index != 1) {
					bufferedWriter.write("and ");
				} else {
					bufferedWriter.write("where");
					bufferedWriter.newLine();
				}

				addFilter(fieldInfos, tableFilter, bufferedWriter);

			}

			bufferedWriter.close();
			return stringWriter.toString();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}
	
	private void addFilter(List<FieldInfo> fieldInfos,TableFilter tableFilter,BufferedWriter bufferedWriter) throws Exception{
		FieldInfo fieldPath = fieldInfos.stream().filter(a -> a.getFieldName().equals(tableFilter.getField())).findFirst().get();
		String columnPart = "";
		if(fieldPath.getSqlMetric()!=null) {
			columnPart+=fieldPath.getSqlMetric().name()+"(";
		}
		columnPart+=createFieldName(tableFilter.getField(), fieldInfos);
		
		if(fieldPath.getSqlMetric()!=null) {
			columnPart+=")";
		}
		
		String fieldType = fieldInfos.stream().filter(a -> a.getFieldName().equals(tableFilter.getField()))
				.findFirst().get().getFieldType();
		if (fieldType.equals("String") && (!tableFilter.getSearchOperation().equals(SearchOperation.equals))) {
			columnPart = "lower(" + columnPart + ")";
		}

		bufferedWriter.write(columnPart);
		bufferedWriter.write(" ");

		switch (tableFilter.getSearchOperation().name()) {
		case "equals":
			bufferedWriter.write("=");
			bufferedWriter.write(" ");
			break;
		case "notEquals":
			bufferedWriter.write("!=");
			bufferedWriter.write(" ");
			break;
		case "contains":
			bufferedWriter.write("like");
			bufferedWriter.write(" ");
			break;
		case "startswith":
			bufferedWriter.write("like");
			bufferedWriter.write(" ");
			break;
		case "endswith":
			bufferedWriter.write("like");
			bufferedWriter.write(" ");
			break;
		case "less":
			bufferedWriter.write("<");
			bufferedWriter.write(" ");
			break;
		case "lessOrEquals":
			bufferedWriter.write("<=");
			bufferedWriter.write(" ");
			break;
		case "greater":
			bufferedWriter.write(">");
			bufferedWriter.write(" ");
			break;
		case "greaterOrEquals":
			bufferedWriter.write(">=");
			bufferedWriter.write(" ");
			break;
		case "between":
			bufferedWriter.write("between");
			bufferedWriter.write(" ");
			break;
		case "isnull":
			bufferedWriter.write("is null");
			break;
		case "isnotnull":
			bufferedWriter.write("is not null");
			break;

		}

		if (!(tableFilter.getSearchOperation().equals(SearchOperation.isnull)
				|| tableFilter.getSearchOperation().equals(SearchOperation.isnotnull))) {
			String searchParameter = tableFilter.getParameter1();
			searchParameter = "'" + searchParameter + "'";
			if (fieldType.equals("String")
					&& (!tableFilter.getSearchOperation().equals(SearchOperation.equals))) {
				searchParameter = "lower(" + searchParameter + ")";
			}

			if (tableFilter.getSearchOperation().equals(SearchOperation.contains)) {
				searchParameter = "'%'||" + searchParameter + "||'%'";
			}

			if (tableFilter.getSearchOperation().equals(SearchOperation.startswith)) {
				searchParameter = searchParameter + "||'%'";
			}

			if (tableFilter.getSearchOperation().equals(SearchOperation.endswith)) {
				searchParameter = "'%'||" + searchParameter;
			}

			if (fieldType.equals("LocalDateTime")) {
				searchParameter = "to_timestamp(" + searchParameter + ",'yyyy-MM-dd\"T\"HH24:MI')";
			}

			bufferedWriter.write(searchParameter);

			if (tableFilter.getSearchOperation().equals(SearchOperation.between)) {
				bufferedWriter.write(" and ");

				String parameter2 = tableFilter.getParameter2();
				parameter2 = "'" + parameter2 + "'";
				if (fieldType.equals("LocalDateTime")) {
					parameter2 = "to_timestamp(" + parameter2 + ",'yyyy-MM-dd\"T\"HH24:MI')";
				}
				bufferedWriter.write(parameter2);
			}
		}
	}
	

	@Override
	public String groupByPart(List<FieldInfo> fieldInfos) {
		try {
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			List<FieldInfo> groupByFields=new ArrayList<>();
			for (FieldInfo fieldIField : fieldInfos) {
				if(fieldIField.getSqlMetric()!=null) {
					continue;
				}
				groupByFields.add(fieldIField);
				
			}
			
			if(groupByFields.isEmpty()) {
				bufferedWriter.close();
				return stringWriter.toString();
			}
			
			bufferedWriter.newLine();
			bufferedWriter.write("group by");
			
			int index=0;
			
			for(FieldInfo fieldInfo:groupByFields) {
				index++;
				bufferedWriter.newLine();
				bufferedWriter.write(createFieldName(fieldInfo.getFieldName(), fieldInfos));
				
				if(index!=groupByFields.size()) {
					bufferedWriter.write(",");
				}
			}
			

			bufferedWriter.close();
			return stringWriter.toString();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}


	@Override
	public String orderPart(TableParameterDTO tableParameterDTO, List<FieldInfo> fieldInfos) {
		try {
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			List<TableSort> tableSorts = tableParameterDTO.getTableSorts();

			if (tableSorts.isEmpty()) {
				List<FieldInfo> fieldInfosSorted = fieldInfos.stream().filter(a -> a.getOrdernum() != null)
						.sorted(Comparator.comparing(FieldInfo::getOrdernum)).toList();
				if (fieldInfosSorted.isEmpty()) {
					bufferedWriter.close();
					return stringWriter.toString();
				}

				for (FieldInfo fieldInfo : fieldInfosSorted) {
					TableSort tableSort = new TableSort();
					tableSort.setField(fieldInfo.getFieldName());
					tableSort.setSortDirection(fieldInfo.getSortDirection());
					tableParameterDTO.getTableSorts().add(tableSort);
				}
			}
			bufferedWriter.newLine();
			bufferedWriter.write("order by");

			int index = 0;
			for (TableSort tableSort : tableSorts) {
				index++;

				bufferedWriter.newLine();
				bufferedWriter.write(
						createFieldName(tableSort.getField(), fieldInfos) + " " + tableSort.getSortDirection().name());

				if (index != tableSorts.size()) {
					bufferedWriter.write(",");
				}
			}

			bufferedWriter.close();
			return stringWriter.toString();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}
	
	@Override
	public String havingPart(TableParameterDTO tableParameterDTO, List<FieldInfo> fieldInfos) {
		try {
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			if (tableParameterDTO.getHavingFilters().isEmpty()) {
				bufferedWriter.close();
				return stringWriter.toString();
			}

			List<TableFilter> tableFilters = tableParameterDTO.getHavingFilters();

			int index = 0;
			for (TableFilter tableFilter : tableFilters) {
				if (!(tableFilter.getSearchOperation().equals(SearchOperation.isnull)
						|| tableFilter.getSearchOperation().equals(SearchOperation.isnotnull))) {
					if (tableFilter.getParameter1() == null) {
						continue;
					}

					if (tableFilter.getParameter1().length() == 0) {
						continue;
					}

					if (tableFilter.getSearchOperation().equals(SearchOperation.between)
							&& tableFilter.getParameter2() == null) {
						continue;
					}

					if (tableFilter.getSearchOperation().equals(SearchOperation.between)
							&& tableFilter.getParameter2().length() == 0) {
						continue;
					}
				}
				bufferedWriter.newLine();
				index++;
				if (index != 1) {
					bufferedWriter.write("and ");
				} else {
					bufferedWriter.write("having");
					bufferedWriter.newLine();
				}

				addFilter(fieldInfos, tableFilter, bufferedWriter);

			}

			bufferedWriter.close();
			return stringWriter.toString();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public String pageablePart(TableParameterDTO tableParameterDTO) {
		try {
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			int offsetRows = tableParameterDTO.getPageNumber() * tableParameterDTO.getPageSize();
			bufferedWriter.newLine();
			bufferedWriter.write("offset " + offsetRows + " rows");
			bufferedWriter.newLine();
			bufferedWriter.write("fetch first " + tableParameterDTO.getPageSize() + " rows only");

			bufferedWriter.close();
			return stringWriter.toString();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}



	@Override
	public <C> String insertQuery(C entity) {
		try {
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			bufferedWriter.write("insert into");
			bufferedWriter.newLine();
			bufferedWriter.write(entity.getClass().getAnnotation(Table.class).name());
			bufferedWriter.newLine();
			bufferedWriter.write("(");
			bufferedWriter.newLine();
			bufferedWriter.write(getListColumns(entity));
			bufferedWriter.newLine();
			bufferedWriter.write(")");
			bufferedWriter.newLine();
			bufferedWriter.write("values");
			bufferedWriter.newLine();
			bufferedWriter.write("(");
			bufferedWriter.newLine();
			bufferedWriter.write(getListValues(entity));
			bufferedWriter.newLine();
			bufferedWriter.write(")");
			bufferedWriter.newLine();
			bufferedWriter.write("returning *");

			bufferedWriter.close();
			return stringWriter.toString();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	private <C> String getListColumns(C entity) {
		try {
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			List<Field> fields = Arrays.asList(entity.getClass().getDeclaredFields());
			List<String> columnNames = new ArrayList<>();
			for (Field field : fields) {

				field.setAccessible(true);
				if (!(field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(JoinColumn.class))) {
					continue;
				}

				String columnName = field.getName();
				if (field.isAnnotationPresent(Column.class)) {
					Column column = field.getAnnotation(Column.class);
					if (column.name().length() > 0) {
						columnName = column.name();
					}
				}
				if (field.isAnnotationPresent(JoinColumn.class)) {
					JoinColumn column = field.getAnnotation(JoinColumn.class);
					if (column.name().length() > 0) {
						columnName = column.name();
					}
				}

				columnNames.add(columnName);

			}

			int counter = 0;

			for (String column : columnNames) {
				counter++;
				bufferedWriter.write(column);

				if (counter != columnNames.size()) {
					bufferedWriter.write(",");
					bufferedWriter.newLine();
				}

			}

			bufferedWriter.close();
			return stringWriter.toString();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	private <C> String getListValues(C entity) {
		try {
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			List<Field> fields = Arrays.asList(entity.getClass().getDeclaredFields());
			int counter = 0;
			for (Field field : fields) {

				field.setAccessible(true);
				if (!(field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(JoinColumn.class))) {
					continue;
				}

				counter++;

			}

			for (int i = 0; i < counter; i++) {
				bufferedWriter.write("?");

				if (i != counter - 1) {
					bufferedWriter.write(",");
					bufferedWriter.newLine();
				}

			}

			bufferedWriter.close();
			return stringWriter.toString();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public <C> String updateQuery(C entity) {
		try {
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			bufferedWriter.write("update");
			bufferedWriter.newLine();
			bufferedWriter.write(entity.getClass().getAnnotation(Table.class).name());
			bufferedWriter.newLine();
			bufferedWriter.write("set");
			bufferedWriter.newLine();
			bufferedWriter.write("(");
			bufferedWriter.newLine();
			bufferedWriter.write(getListColumns(entity));
			bufferedWriter.newLine();
			bufferedWriter.write(")");
			bufferedWriter.newLine();
			bufferedWriter.write("=");
			bufferedWriter.newLine();
			bufferedWriter.write("(");
			bufferedWriter.newLine();
			bufferedWriter.write(getListValues(entity));
			bufferedWriter.newLine();
			bufferedWriter.write(")");
			bufferedWriter.newLine();
			bufferedWriter.write("where");
			bufferedWriter.newLine();
			bufferedWriter.write("id=" + findPrimaryField(entity.getClass()).get(entity));
			bufferedWriter.newLine();
			bufferedWriter.write("returning *");

			bufferedWriter.close();
			return stringWriter.toString();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public <C> String deleteQuery(C entity) {
		try {
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			bufferedWriter.write("delete from " + entity.getClass().getAnnotation(Table.class).name());
			bufferedWriter.newLine();
			bufferedWriter.write("where");
			bufferedWriter.newLine();
			bufferedWriter.write(findPrimaryField(entity.getClass()).getName() + "="
					+ findPrimaryField(entity.getClass()).get(entity));

			bufferedWriter.close();
			return stringWriter.toString();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}


}

