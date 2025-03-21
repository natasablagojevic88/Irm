package rs.irm.database.utils;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class TableData {
	
	private String name;
	
	private List<CreateColumnData> columnDataList=new ArrayList<>();
	
	private List<UniqueData> uniqueDatas=new ArrayList<>();
	
	private List<ForeignKeyData> foreignKeyDatas=new ArrayList<>();

	private List<IndexData> indexDatas=new ArrayList<>();
}
