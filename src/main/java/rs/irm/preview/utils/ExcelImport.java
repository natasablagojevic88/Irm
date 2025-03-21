package rs.irm.preview.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.postgresql.util.PSQLException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.irm.administration.dto.ModelColumnDTO;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.administration.enums.ModelColumnType;
import rs.irm.administration.enums.ModelType;
import rs.irm.administration.utils.ModelData;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.exceptions.ExcelRowException;
import rs.irm.common.exceptions.FieldRequiredException;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.common.service.impl.ResourceBundleServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.preview.service.ModelQueryCreatorService;
import rs.irm.preview.service.impl.ModelQueryCreatorServiceImpl;

public class ExcelImport implements ExecuteMethodWithReturn<LinkedHashMap<String, Object>> {

	private HttpServletRequest httpServletRequest;
	private Long modelId;
	private Long parentId;
	private File excelFile;

	private ModelQueryCreatorService modelQueryCreatorService;
	private ResourceBundleService resourceBundleService;

	public ExcelImport(HttpServletRequest httpServletRequest, Long modelId, Long parentId, File excelFile) {
		this.httpServletRequest = httpServletRequest;
		this.modelId = modelId;
		this.parentId = parentId;
		this.excelFile = excelFile;
		this.modelQueryCreatorService = new ModelQueryCreatorServiceImpl(this.httpServletRequest);
		this.resourceBundleService = new ResourceBundleServiceImpl(this.httpServletRequest);
	}

	@Override
	public LinkedHashMap<String, Object> execute(Connection connection) {
		if (modelQueryCreatorService.checkLockParent(modelId, 0L, parentId, connection)) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "parentIsLocked", null);
		}

		LinkedHashMap<String, Object> itemExport = new LinkedHashMap<>();

		InputStream inputStream;
		try {
			inputStream = new FileInputStream(excelFile);
		} catch (FileNotFoundException e) {
			throw new WebApplicationException(e);
		}

		Workbook workbook;
		try {
			workbook = WorkbookFactory.create(inputStream);
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

		Sheet sheet = workbook.getSheet("import");
		Map<String, Integer> columnMap = getExcelColumnMap(sheet);

		ModelDTO modelDTO = findModel(modelId);
		List<ModelColumnDTO> columnDTOs = findColumns(modelId);
		Map<String, ModelDTO> parentCodebookMap = findColumnParent(columnDTOs);
		Map<String, CodebookInfo> codebooks = findCodebookData(columnDTOs, parentCodebookMap, connection);
		
		LinkedHashMap<String, List<ComboboxDTO>> findListOfValue = this.modelQueryCreatorService
				.createListOfValue(modelId, connection);

		List<LinkedHashMap<String, Object>> data = readData(sheet, columnMap, connection, modelDTO, parentCodebookMap,
				codebooks, findListOfValue);
		
		Integer errorRow=2;
		try {
			for(LinkedHashMap<String, Object> item:data) {
				errorRow++;
				UpdateItem updateItem=new UpdateItem(httpServletRequest, modelId, parentId, item, false);
				itemExport=updateItem.execute(connection);
			}
		}catch(Exception e) {
			handlerError(errorRow, e);
			
		}

		if (modelQueryCreatorService.checkLockParent(modelId, 0L, parentId, connection)) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "parentIsLocked", null);
		}

		return itemExport;
	}

	private Map<String, Integer> getExcelColumnMap(Sheet sheet) {
		Map<String, Integer> map = new HashMap<>();

		Row row = sheet.getRow(0);

		for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
			map.put(row.getCell(i).getStringCellValue(), i);
		}

		return map;
	}

	private ModelDTO findModel(Long modelId) {
		return ModelData.listModelDTOs.stream().filter(a -> a.getId().doubleValue() == modelId.doubleValue()).findFirst().get();
	}

	private List<ModelColumnDTO> findColumns(Long modelId) {
		return ModelData.listColumnDTOs.stream().filter(a -> a.getModelId() == modelId)
				.filter(a -> a.getDisabled() == false).toList();
	}

	private ModelColumnDTO findColumnById(Long modelId, Long columnId) {
		List<ModelColumnDTO> columnForModelColumnDTOs=ModelData.listColumnDTOs.stream().filter(a -> a.getModelId() == modelId).toList();
		return 	columnForModelColumnDTOs.stream()
				.filter(a -> a.getId().doubleValue() == columnId.doubleValue()).findFirst().get();
	}

	private List<ModelColumnDTO> findColumnsWithCodebook(List<ModelColumnDTO> columnDTOs) {
		return columnDTOs.stream().filter(a -> a.getColumnType().equals(ModelColumnType.CODEBOOK.name())).toList();
	}

	private Map<String, ModelDTO> findColumnParent(List<ModelColumnDTO> columnDTOs) {
		Map<String, ModelDTO> map = new HashMap<>();
		List<ModelColumnDTO> codebookColumns = findColumnsWithCodebook(columnDTOs);

		for (ModelColumnDTO modelColumnDTO : codebookColumns) {
			ModelDTO codebookModel = findModel(modelColumnDTO.getCodebookModelId());

			if (map.containsKey(codebookModel.getCode())) {
				continue;
			}

			if (!(codebookModel.getParentId() != null
					&& codebookModel.getParentType().equals(ModelType.TABLE.name()))) {
				continue;
			}
			map.put(codebookModel.getCode(), findModel(codebookModel.getParentId()));

		}

		return map;
	}

	private Map<String, CodebookInfo> findCodebookData(List<ModelColumnDTO> columnDTOs,
			Map<String, ModelDTO> parentCodebookMap, Connection connection) {
		Map<String, CodebookInfo> map = new HashMap<>();
		List<ModelColumnDTO> codebookColumns = findColumnsWithCodebook(columnDTOs);

		for (ModelColumnDTO columnDTO : codebookColumns) {
			String codebook = columnDTO.getCodebookModelCode();
			if (map.containsKey(codebook)) {
				continue;
			}
			CodebookInfo codebookInfo = new CodebookInfo();
			codebookInfo.setCodeType(
					columnDTO.getCodebookModelNumericCode() ? CodebookColumnType.LONG : CodebookColumnType.STRING);

			String findCodebook = new String(codebook);

			while (parentCodebookMap.containsKey(findCodebook)) {
				ModelDTO parentModelDTO = parentCodebookMap.get(findCodebook);
				codebookInfo.getParentType().put(parentModelDTO.getCode(),
						parentModelDTO.getNumericCode() ? CodebookColumnType.LONG : CodebookColumnType.STRING);
				findCodebook = parentModelDTO.getCode();
			}

			String query = readCodebookQuery(columnDTO, parentCodebookMap);

			try {
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(query);

				while (resultSet.next()) {
					CodebookData codebookData = new CodebookData();
					Number id = (Number) resultSet.getObject(1);
					codebookData.setId(id.longValue());

					if (columnDTO.getCodebookModelNumericCode()) {
						Number code = (Number) resultSet.getObject(2);
						codebookData.setCodeLong(code.longValue());
					} else {
						codebookData.setCodeString(resultSet.getObject(2).toString());
					}

					int resultSetCount = 2;
					findCodebook = new String(codebook);

					LinkedHashMap<String, CodebookData> parentsData = new LinkedHashMap<>();
					while (parentCodebookMap.containsKey(findCodebook)) {
						resultSetCount++;
						ModelDTO parentModelDTO = parentCodebookMap.get(findCodebook);
						String parentCodeName = parentModelDTO.getCode();

						CodebookData parentData = new CodebookData();
						if (parentModelDTO.getNumericCode()) {
							Number code = (Number) resultSet.getObject(resultSetCount);
							parentData.setCodeLong(code.longValue());
						} else {
							parentData.setCodeString(resultSet.getObject(resultSetCount).toString());
						}
						parentsData.put(parentCodeName, parentData);

						findCodebook = parentModelDTO.getCode();
					}
					codebookData.setParents(parentsData);

					codebookInfo.getData().add(codebookData);
				}

				resultSet.close();
				statement.close();
			} catch (Exception e) {
				throw new WebApplicationException(e);
			}
			map.put(codebook, codebookInfo);
		}

		return map;
	}

	private String readCodebookQuery(ModelColumnDTO codebookColumn, Map<String, ModelDTO> parentCodebookMap) {
		try {
			String tableName = codebookColumn.getCodebookModelCode();
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

			bufferedWriter.write("select");
			bufferedWriter.newLine();
			bufferedWriter.write("a.id,");
			bufferedWriter.newLine();
			bufferedWriter.write("a.code");

			String findTable = new String(tableName);
			int aliasCounter = 0;
			while (parentCodebookMap.containsKey(findTable)) {
				ModelDTO modelDTO = parentCodebookMap.get(findTable);
				aliasCounter++;
				bufferedWriter.write(",");
				bufferedWriter.newLine();
				bufferedWriter.write("a" + aliasCounter + ".code");

				findTable = modelDTO.getCode();
			}

			findTable = new String(tableName);
			bufferedWriter.newLine();
			bufferedWriter.write("from " + findTable + " a");
			aliasCounter = 0;
			while (parentCodebookMap.containsKey(findTable)) {
				ModelDTO modelDTO = parentCodebookMap.get(findTable);
				aliasCounter++;
				bufferedWriter.write(",");
				bufferedWriter.newLine();
				bufferedWriter.write(modelDTO.getCode() + " a" + aliasCounter);

				findTable = modelDTO.getCode();
			}

			findTable = new String(tableName);

			if (parentCodebookMap.containsKey(findTable)) {
				bufferedWriter.newLine();
				bufferedWriter.write("where");
			}

			String alias = "a";
			int counter = 0;
			aliasCounter = 0;
			while (parentCodebookMap.containsKey(findTable)) {
				counter++;
				ModelDTO modelDTO = parentCodebookMap.get(findTable);
				aliasCounter++;
				bufferedWriter.newLine();
				if (counter != 1) {
					bufferedWriter.write("and ");
				}
				bufferedWriter.write(alias + "." + modelDTO.getCode() + "=a" + aliasCounter + ".id");
				alias = "a" + aliasCounter;

				findTable = modelDTO.getCode();
			}

			bufferedWriter.close();
			return stringWriter.toString();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	private List<LinkedHashMap<String, Object>> readData(Sheet sheet, Map<String, Integer> columnMap,
			Connection connection, ModelDTO modelDTO, Map<String, ModelDTO> parentCodebookMap,
			Map<String, CodebookInfo> codebooks, LinkedHashMap<String, List<ComboboxDTO>> listOfValue) {
		List<LinkedHashMap<String, Object>> list = new ArrayList<>();

		LinkedHashMap<String, Object> defaultValue = this.modelQueryCreatorService.getDefaultValues(modelId, parentId,
				connection);

		Long nextCode = modelDTO.getNumericCode() ? ((Number) defaultValue.get("code")).longValue() : null;

		int rowError = 1;
		try {
			for (int i = 2; i < sheet.getPhysicalNumberOfRows(); i++) {
				LinkedHashMap<String, Object> item = new LinkedHashMap<>();
				rowError = i + 1;
				Row row = sheet.getRow(i);

				item.put("id", Long.valueOf(0));
				item.put("lock", false);
				Cell codeCell = row.getCell(0);
				if (modelDTO.getNumericCode()) {
					if (codeCell == null || codeCell.getCellType().equals(CellType.BLANK)) {
						item.put("code", nextCode);
						nextCode = nextCode + 1;
					} else {
						Number codeNumber = (Number) codeCell.getNumericCellValue();
						item.put("code", codeNumber.longValue());
					}
				} else {
					if (!(codeCell == null || codeCell.getCellType().equals(CellType.BLANK))) {
						if (codeCell.getCellType().equals(CellType.NUMERIC)) {
							Number codeNumber = (Number) codeCell.getNumericCellValue();
							item.put("code", codeNumber.toString());
						} else {
							item.put("code", codeCell.getStringCellValue());
						}
					}
				}

				List<ModelColumnDTO> columnModelDTOs = findColumns(modelId);

				for (ModelColumnDTO columnDTO : columnModelDTOs) {
					String columnCode = columnDTO.getCode();

					if (!columnMap.containsKey(columnCode)) {
						continue;
					}

					Cell cell = row.getCell(columnMap.get(columnCode));

					if (cell == null || cell.getCellType().equals(CellType.BLANK)) {
						continue;
					}
					
					if(listOfValue.containsKey(columnCode)) {
						List<ComboboxDTO> listOfValues = listOfValue.get(columnCode);
						if (listOfValues.stream().filter(a -> a.getValue().toString().equals(cell.getStringCellValue())).toList().isEmpty()) {
							throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "valueNotOnListOfValue", columnCode);
						}
					}

					item.put(columnDTO.getCode(), readCell(cell, columnDTO, parentCodebookMap, codebooks, columnMap));

				}

				list.add(item);
			}
		} catch (Exception e) {
			handlerError(rowError, e);
		}

		return list;
	}

	private Object readCell(Cell cell, ModelColumnDTO columnDTO, Map<String, ModelDTO> parentCodebookMap,
			Map<String, CodebookInfo> codebooks, Map<String, Integer> columnMap) {

		String columnType = columnDTO.getColumnType();
		String datePattern = "yyyy-MM-dd";
		DateFormat dateFormat = new SimpleDateFormat(datePattern);

		String dateTimePattern = "yyyy-MM-dd'T'HH:mm";
		DateFormat dateTimeFormat = new SimpleDateFormat(dateTimePattern);

		switch (columnType) {
		case "STRING": {
			if (cell.getCellType().equals(CellType.NUMERIC)) {
				Number number = cell.getNumericCellValue();
				return number.toString();
			} else {
				return cell.getStringCellValue();
			}
		}
		case "INTEGER": {
			Number number = cell.getNumericCellValue();
			return number.intValue();
		}
		case "LONG": {
			Number number = cell.getNumericCellValue();
			return number.longValue();
		}
		case "BIGDECIMAL": {
			Number number = cell.getNumericCellValue();
			return BigDecimal.valueOf(number.doubleValue());
		}
		case "LOCALDATE": {
			java.util.Date date = cell.getDateCellValue();
			return LocalDate.parse(dateFormat.format(date));
		}
		case "LOCALDATETIME": {
			java.util.Date date = cell.getDateCellValue();
			return LocalDateTime.parse(dateTimeFormat.format(date));
		}
		case "BOOLEAN": {

			return Boolean.valueOf(cell.getStringCellValue());
		}
		case "CODEBOOK": {
			ModelDTO modelDTO = findModel(columnDTO.getCodebookModelId());

			String tableName = modelDTO.getCode();

			Long codeLong;
			String codeString;

			CodebookInfo codebookInfo = codebooks.get(tableName);

			if (codebookInfo.getCodeType().equals(CodebookColumnType.LONG)) {
				Number number = cell.getNumericCellValue();
				codeLong = number.longValue();
				codeString = null;
			} else {
				if (cell.getCellType().equals(CellType.NUMERIC)) {
					Number number = cell.getNumericCellValue();
					codeString = number.toString();
				} else {
					codeString = cell.getStringCellValue();
				}
				codeLong = null;
			}

			List<CodebookData> list;
			// search by code
			if (codebookInfo.getCodeType().equals(CodebookColumnType.LONG)) {
				list = codebookInfo.getData().stream().filter(a -> a.getCodeLong() == codeLong).toList();
			} else {
				list = codebookInfo.getData().stream().filter(a -> a.getCodeString().equals(codeString)).toList();
			}

			if (list.isEmpty()) {
				Object[] objects = { codeLong == null ? codeString : codeLong, tableName,
						resourceBundleService.getText(columnDTO.getName(), null) };

				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST,
						resourceBundleService.getText("noCodebookValueFound", objects), null);
			}

			String findTable = new String(tableName);
			ModelColumnDTO childColumnDTO=columnDTO;
			// search By Parent
			while (parentCodebookMap.containsKey(findTable)) {
				ModelDTO parentDto = parentCodebookMap.get(findTable);
				String parentTable=parentDto.getCode();
				ModelColumnDTO parentColumnDTO = findColumnById(modelId, childColumnDTO.getParentId());
				String errorTextNoParentFund = resourceBundleService.getText("fieldRequired", null) + ": "
						+ resourceBundleService.getText(parentColumnDTO.getName(), null);
				if (!columnMap.containsKey(parentColumnDTO.getCode())) {
					throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, errorTextNoParentFund,
							null);
				}

				Row row = cell.getRow();
				Cell parentCell = row.getCell(columnMap.get(parentColumnDTO.getCode()));
				if (parentCell == null || parentCell.getCellType().equals(CellType.BLANK)) {
					throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, errorTextNoParentFund,
							null);
				}
				
				Long codeParentLong;
				String codeParentString;
				
				if(codebookInfo.getParentType().get(parentTable).equals(CodebookColumnType.LONG)) {
					Number number = parentCell.getNumericCellValue();
					codeParentLong= number.longValue();
					codeParentString=null;
				}else {
					if (parentCell.getCellType().equals(CellType.NUMERIC)) {
						Number number = parentCell.getNumericCellValue();
						codeParentString = number.toString();
					} else {
						codeParentString = parentCell.getStringCellValue();
					}
					codeParentLong=null;
				}
				List<CodebookData> listNew=new ArrayList<>();
				for(CodebookData codebookData:list) {
					CodebookData parentData=codebookData.getParents().get(parentTable);
					
					if(codebookInfo.getParentType().get(parentTable).equals(CodebookColumnType.LONG)) {
						if(parentData.getCodeLong()==codeParentLong) {
							listNew.add(codebookData);
						}
					}else {
						if(parentData.getCodeString().equals(codeParentString)) {
							listNew.add(codebookData);
						}
					}
				}
				list=listNew;
				if (list.isEmpty()) {
					Object[] objects = { codeParentLong == null ? codeParentString : codeParentLong, parentTable,
							resourceBundleService.getText(parentColumnDTO.getName(), null) };

					throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST,
							resourceBundleService.getText("wrongParent", objects), null);
				}
				
				findTable = parentDto.getCode();
				childColumnDTO=parentColumnDTO;
			}

			return list.get(0).getId();
		}
		}

		return null;
	}
	
	private void handlerError(int excelRow,Exception exception) {
		Exception lastException=exception;
		while(lastException.getCause()!=null) {
			lastException=(Exception) lastException.getCause();
		}
		if(lastException.getClass().getCanonicalName().equals(FieldRequiredException.class.getCanonicalName())) {
			FieldRequiredException fieldRequiredException=(FieldRequiredException)lastException;
			throw new ExcelRowException(excelRow, resourceBundleService.getText(fieldRequiredException.getMessage(), null)+": "+resourceBundleService.getText(fieldRequiredException.getField(), null));
		}else if(lastException.getClass().getCanonicalName().equals(CommonException.class.getCanonicalName())) {
			CommonException commonException=(CommonException)lastException;
			String message=resourceBundleService.getText(commonException.getMessage(),null);
			message+=commonException.getInObject()==null?"":": "+resourceBundleService.getText(commonException.getInObject().toString(),null);
			throw new ExcelRowException(excelRow, message);
		}else if(lastException.getClass().getCanonicalName().equals(PSQLException.class.getCanonicalName())) {
			BufferedReader reader=new BufferedReader(new StringReader(lastException.getMessage()));
			String message;
			try {
				String rowReader=reader.readLine();
				message=rowReader.substring(7);
				reader.close();
			} catch (IOException e1) {
				throw new WebApplicationException(e1);
			}
			throw new ExcelRowException(excelRow, resourceBundleService.getText(message, null));
		}
		else {
			throw new ExcelRowException(excelRow, resourceBundleService.getText(lastException.getMessage(), null));
		}
	}

}

enum CodebookColumnType {
	LONG, STRING
}

@Data
@NoArgsConstructor
class CodebookData {

	private Long id;
	private Long codeLong;
	private String codeString;
	private LinkedHashMap<String, CodebookData> parents = new LinkedHashMap<>();

}

@Data
@NoArgsConstructor
class CodebookInfo {

	private CodebookColumnType codeType;
	private LinkedHashMap<String, CodebookColumnType> parentType = new LinkedHashMap<>();
	private List<CodebookData> data = new ArrayList<>();
}