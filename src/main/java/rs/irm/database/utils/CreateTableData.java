package rs.irm.database.utils;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.ws.rs.WebApplicationException;
import rs.irm.database.enums.BaseType;

public class CreateTableData implements ExecuteMethod {
	private List<TableData> tableDatas = new ArrayList<>();
	private Logger logger = LogManager.getLogger(CreateTableData.class);
	private Connection connection;
	private DatabaseMetaData databaseMetaData;
	private String catalog;
	private String schema;

	public CreateTableData(List<TableData> tableDatas) {
		this.tableDatas = tableDatas;
	}

	@Override
	public void execute(Connection connection) {
		try {
			this.connection = connection;
			catalog = connection.getCatalog();
			schema = connection.getSchema();
			this.databaseMetaData = connection.getMetaData();

			for (TableData tableData : tableDatas) {
				if (databaseMetaData.getTables(catalog, schema, tableData.getName(), null).next()) {
					checkColumns(tableData);
				} else {
					createTable(tableData);
				}
				checkUnique(tableData);
				checkIndex(tableData);
			}
			for (TableData tableData : tableDatas) {
				checkForeignKey(tableData);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e);
		}

	}

	private void createTable(TableData tableData) throws Exception {

		StringWriter stringWriter = new StringWriter();
		BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
		bufferedWriter.write("create table " + tableData.getName());
		bufferedWriter.newLine();
		bufferedWriter.write("(");
		bufferedWriter.newLine();
		List<CreateColumnData> columns = tableData.getColumnDataList();
		int columnIndex = 0;
		for (CreateColumnData columnData : columns) {
			columnIndex++;

			addColumnWriter(bufferedWriter, columnData);
			if (columnIndex != columns.size()) {
				bufferedWriter.write(",");
				bufferedWriter.newLine();
			}

		}
		if (!columns.stream().filter(a -> a.getId() == true).toList().isEmpty()) {
			CreateColumnData columnData = columns.stream().filter(a -> a.getId() == true).findFirst().get();
			bufferedWriter.write(",");
			bufferedWriter.newLine();
			bufferedWriter
					.write("CONSTRAINT " + tableData.getName() + "_pk PRIMARY KEY (" + columnData.getName() + ")");
		}
		bufferedWriter.newLine();
		bufferedWriter.write(")");

		bufferedWriter.close();

		Statement statement = connection.createStatement();
		statement.executeUpdate(stringWriter.toString());
		statement.close();
	}

	private void addColumnWriter(BufferedWriter bufferedWriter, CreateColumnData columnData) throws Exception {
		bufferedWriter.write(columnData.getName());
		bufferedWriter.write(" ");
		bufferedWriter.write(columnData.getBaseType().name());
		bufferedWriter
				.write(columnData.getBaseType().equals(BaseType.varchar) ? "(" + columnData.getLength() + ")" : "");
		bufferedWriter.write(columnData.getBaseType().equals(BaseType.numeric)
				? "(" + columnData.getLength() + "," + columnData.getPrecision() + ")"
				: "");
		bufferedWriter.write(" ");
		bufferedWriter.write(columnData.getNullable() ? "NULL" : "NOT NULL");
	}

	private void checkColumns(TableData tableData) throws Exception {

		for (CreateColumnData columnData : tableData.getColumnDataList()) {

			if (!databaseMetaData.getColumns(catalog, schema, tableData.getName(), columnData.getName()).next()) {
				addColumn(tableData.getName(), columnData, this.connection);
			}
		}
	}

	public void addColumn(String tableName, CreateColumnData columnData, Connection connection) throws Exception {
		StringWriter stringWriter = new StringWriter();
		BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
		bufferedWriter.write("alter table " + tableName);
		bufferedWriter.newLine();
		bufferedWriter.write("add ");
		addColumnWriter(bufferedWriter, columnData);
		bufferedWriter.close();
		
		Statement statement = connection.createStatement();
		statement.executeUpdate(stringWriter.toString());
		statement.close();

	}

	private void checkUnique(TableData tableData) throws Exception {
		ResultSet resultSet = this.databaseMetaData.getIndexInfo(catalog, schema, tableData.getName(), true, true);
		Set<String> indexEntered = new HashSet<>();
		while (resultSet.next()) {
			indexEntered.add(resultSet.getString(6));
		}

		resultSet.close();

		for (UniqueData uniqueData : tableData.getUniqueDatas()) {

			if (indexEntered.contains(uniqueData.getName())) {
				continue;
			}

			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
			bufferedWriter.write("alter table " + tableData.getName());
			bufferedWriter.newLine();
			bufferedWriter.write("ADD CONSTRAINT " + uniqueData.getName());
			bufferedWriter.newLine();
			bufferedWriter.write("UNIQUE ");
			bufferedWriter.write("(");
			int index = 0;
			for (String column : uniqueData.getColumns()) {
				index++;
				bufferedWriter.write(column);
				if (index != uniqueData.getColumns().size()) {
					bufferedWriter.write(",");
				}
			}
			bufferedWriter.write(")");

			bufferedWriter.close();
			Statement statement = connection.createStatement();
			statement.executeUpdate(stringWriter.toString());
			statement.close();
		}
	}

	private void checkIndex(TableData tableData) throws Exception {
		ResultSet resultSet = this.databaseMetaData.getIndexInfo(catalog, schema, tableData.getName(), false, true);
		Set<String> indexEntered = new HashSet<>();
		while (resultSet.next()) {
			indexEntered.add(resultSet.getString(6));
		}

		resultSet.close();

		for (IndexData indexData : tableData.getIndexDatas()) {

			if (indexEntered.contains(indexData.getName())) {
				continue;
			}

			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
			bufferedWriter.write("CREATE INDEX " + indexData.getName());
			bufferedWriter.newLine();
			bufferedWriter.write("on " + tableData.getName());
			bufferedWriter.newLine();
			bufferedWriter.write("USING btree (" + indexData.getColumns() + ")");

			bufferedWriter.close();
			Statement statement = connection.createStatement();
			statement.executeUpdate(stringWriter.toString());
			statement.close();

		}
	}

	private void checkForeignKey(TableData tableData) throws Exception {

		for (ForeignKeyData foreignKeyData : tableData.getForeignKeyDatas()) {

			ResultSet resultSet = this.databaseMetaData.getExportedKeys(catalog, schema, foreignKeyData.getTable());
			Set<String> existsKeys = new HashSet<>();
			while (resultSet.next()) {
				existsKeys.add(resultSet.getString(12));
			}
			resultSet.close();

			if (existsKeys.contains(foreignKeyData.getName())) {
				continue;
			}

			String tableName = tableData.getName();

			TableData foreignKeyTable = tableDatas.stream().filter(a -> a.getName().equals(foreignKeyData.getTable()))
					.findFirst().get();
			CreateColumnData foreignKeyID = foreignKeyTable.getColumnDataList().stream().filter(a -> a.getId() == true)
					.findFirst().get();
			String foreignKeyIdName = foreignKeyID.getName();

			addForeignKey(tableName, foreignKeyData, foreignKeyIdName, connection);
		}
	}

	public void addForeignKey(String tableName, ForeignKeyData foreignKeyData, String foreignKeyNameId, Connection connection)
			throws Exception {
		StringWriter stringWriter = new StringWriter();
		BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

		bufferedWriter.write("ALTER TABLE " + tableName);
		bufferedWriter.newLine();

		bufferedWriter.write("ADD CONSTRAINT " + foreignKeyData.getName());
		bufferedWriter.newLine();

		bufferedWriter.write("FOREIGN KEY (" + foreignKeyData.getColumn() + ")");
		bufferedWriter.newLine();

		bufferedWriter.write("REFERENCES " + foreignKeyData.getTable() + "(" + foreignKeyNameId + ")");

		if (foreignKeyData.getCascade()) {
			bufferedWriter.newLine();
			bufferedWriter.write("ON DELETE CASCADE");
		}

		bufferedWriter.close();
		Statement statement = connection.createStatement();
		statement.executeUpdate(stringWriter.toString());
		statement.close();

	}

}
