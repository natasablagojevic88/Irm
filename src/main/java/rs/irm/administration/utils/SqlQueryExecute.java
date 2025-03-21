package rs.irm.administration.utils;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import jakarta.ws.rs.WebApplicationException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.Select;
import rs.irm.administration.dto.SqlQueryParametersDTO;
import rs.irm.administration.dto.SqlQuerySortDTO;
import rs.irm.administration.dto.SqlResultColumnDTO;
import rs.irm.administration.dto.SqlResultDTO;
import rs.irm.common.exceptions.CommonException;
import rs.irm.database.enums.ColumnType;
import rs.irm.database.enums.SortDirection;
import rs.irm.database.utils.ExecuteMethodWithReturn;

public class SqlQueryExecute implements ExecuteMethodWithReturn<SqlResultDTO> {

	private SqlQueryParametersDTO sqlQueryParametersDTO;

	public SqlQueryExecute(SqlQueryParametersDTO sqlQueryParametersDTO) {
		this.sqlQueryParametersDTO = sqlQueryParametersDTO;
	}

	@Override
	public SqlResultDTO execute(Connection connection) {

		SqlResultDTO sqlResultDTO = new SqlResultDTO();

		String query = createQuery(sqlQueryParametersDTO, sqlResultDTO);
		try {
			PreparedStatement st = connection.prepareStatement(query);

			ResultSet resultSet = st.executeQuery();

			createColumns(sqlResultDTO, resultSet);

			readData(sqlResultDTO, resultSet);

			resultSet.close();
			st.close();

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

		sqlResultDTO.setTotalItems(getTotal(sqlResultDTO, sqlQueryParametersDTO, connection));

		Long numberOfPages = sqlResultDTO.getTotalItems() / sqlQueryParametersDTO.getPageSize();

		if (sqlResultDTO.getTotalItems() % sqlQueryParametersDTO.getPageSize() != 0) {
			numberOfPages = numberOfPages + 1;
		}
		sqlResultDTO.setNumberOfPages(numberOfPages.intValue());

		return sqlResultDTO;
	}

	private String createQuery(SqlQueryParametersDTO parametersDTO, SqlResultDTO sqlResultDTO) {
		try {

			boolean hasOffsetAndFetch = false;

			net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(parametersDTO.getSqlQuery());

			if (!(statement instanceof Select)) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "unallowedQuery", null);
			}

			Select select = (Select) statement;

			if (select.getOffset() != null || select.getFetch() != null) {
				hasOffsetAndFetch = true;
				sqlResultDTO.setQueryWithFetch(true);
			}

			if (!parametersDTO.getSorts().isEmpty()) {
				List<OrderByElement> orderByElements = new ArrayList<>();

				for (SqlQuerySortDTO querySortDTO : parametersDTO.getSorts()) {
					if (querySortDTO.getOrderNumber() == -1) {
						continue;
					}
					OrderByElement orderByElement = new OrderByElement();
					orderByElement.setAsc(querySortDTO.getSortDirection().equals(SortDirection.ASC) ? true : false);
					orderByElement.setExpression(
							CCJSqlParserUtil.parseExpression(String.valueOf(querySortDTO.getOrderNumber())));
					orderByElements.add(orderByElement);
				}

				if (!orderByElements.isEmpty()) {
					select.setOrderByElements(orderByElements);
					parametersDTO.setSqlQuery(select.toString());
				}

			}

			int offsetRows = sqlQueryParametersDTO.getPageNumber() * sqlQueryParametersDTO.getPageSize();

			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			bufferedWriter.write(parametersDTO.getSqlQuery());
			if (!hasOffsetAndFetch) {
				bufferedWriter.newLine();
				bufferedWriter.write("offset " + offsetRows + " rows");
				bufferedWriter.newLine();
				bufferedWriter.write("fetch first " + sqlQueryParametersDTO.getPageSize() + " rows only");
			}

			bufferedWriter.close();
			return stringWriter.toString();

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	private Long getTotal(SqlResultDTO sqlResultDTO, SqlQueryParametersDTO sqlQueryParametersDTO,
			Connection connection) {
		try {
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			bufferedWriter.write("select count(*)");

			for (Integer totalColumn : sqlQueryParametersDTO.getTotals().stream().distinct().toList()) {
				sqlResultDTO.setHasTotal(true);
				bufferedWriter.write(",");
				bufferedWriter.newLine();

				SqlResultColumnDTO resultColumnDTO = sqlResultDTO.getColumns().stream()
						.filter(a -> a.getOrderNumber().doubleValue() == totalColumn.doubleValue()).findFirst().get();

				bufferedWriter.write("coalesce(sum(" + resultColumnDTO.getName() + "),0)");
				bufferedWriter.newLine();

			}
			bufferedWriter.newLine();
			bufferedWriter.write("from");
			bufferedWriter.newLine();
			bufferedWriter.write("(");
			bufferedWriter.newLine();
			bufferedWriter.write(sqlQueryParametersDTO.getSqlQuery());
			bufferedWriter.newLine();
			bufferedWriter.write(") t");

			bufferedWriter.close();

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(stringWriter.toString());
			resultSet.next();

			LinkedHashMap<Integer, Object> totalColumns = new LinkedHashMap<>();
			int index = 1;
			for (Integer totalColumn : sqlQueryParametersDTO.getTotals().stream().distinct().toList()) {
				index++;
				totalColumns.put(totalColumn, (Number) resultSet.getObject(index));
			}

			sqlResultDTO.setTotalsColumn(totalColumns);
			Long total = ((Number) resultSet.getObject(1)).longValue();
			resultSet.close();
			statement.close();

			return total;

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	private void createColumns(SqlResultDTO sqlResultDTO, ResultSet resultSet) {
		try {
			for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
				SqlResultColumnDTO resultColumnDTO = new SqlResultColumnDTO();
				resultColumnDTO.setOrderNumber(i);
				resultColumnDTO.setName(resultSet.getMetaData().getColumnName(i));

				@SuppressWarnings("rawtypes")
				Class columnClass = Class.forName(resultSet.getMetaData().getColumnClassName(i));

				switch (columnClass.getSimpleName()) {
				case "Date": {
					resultColumnDTO.setType(ColumnType.LocalDate);
					break;
				}
				case "Timestamp": {
					resultColumnDTO.setType(ColumnType.LocalDateTime);
					break;
				}
				default: {
					resultColumnDTO.setType(ColumnType.valueOf(columnClass.getSimpleName()));
					break;
				}

				}

				sqlResultDTO.getColumns().add(resultColumnDTO);
			}

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	private void readData(SqlResultDTO sqlResultDTO, ResultSet resultSet) {
		try {
			while (resultSet.next()) {

				LinkedHashMap<Integer, Object> data = new LinkedHashMap<>();
				for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
					final int k = i;
					SqlResultColumnDTO sqlResultColumnDTO = sqlResultDTO.getColumns().stream()
							.filter(a -> a.getOrderNumber().intValue() == k).findFirst().get();

					if (resultSet.getObject(i) == null || resultSet.getObject(i).toString().length() == 0) {
						continue;
					}

					switch (sqlResultColumnDTO.getType().name()) {
					case "LocalDate": {
						java.sql.Date date = (java.sql.Date) resultSet.getObject(i);
						data.put(i, date.toLocalDate());
						break;
					}
					case "LocalDateTime": {
						java.sql.Timestamp date = (java.sql.Timestamp) resultSet.getObject(i);
						data.put(i, date.toLocalDateTime());
						break;
					}
					default: {
						data.put(i, resultSet.getObject(i));
						break;
					}
					}

				}

				sqlResultDTO.getList().add(data);
			}

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

}
