package rs.irm.database.service;

import java.util.List;

import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.utils.FieldInfo;
import rs.irm.database.utils.LeftTable;

public interface QueryWriterService {

	List<FieldInfo> pathField(Class<?> inClass);
	List<LeftTable> leftJoinTable(List<FieldInfo> fieldInfos);
	String selectPart(List<FieldInfo> fieldInfos);
	String fromPart(String tableName,List<LeftTable> leftTables);
	String wherePart(TableParameterDTO tableParameterDTO,List<FieldInfo> fieldInfos);
	String groupByPart(List<FieldInfo> fieldInfos);
	String havingPart(TableParameterDTO tableParameterDTO,List<FieldInfo> fieldInfos);
	String orderPart(TableParameterDTO tableParameterDTO,List<FieldInfo> fieldInfos);
	String pageablePart(TableParameterDTO tableParameterDTO);
	<C> String insertQuery(C entity);
	<C> String updateQuery(C entity);
	<C> String deleteQuery(C entity);
}
