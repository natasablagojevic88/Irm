package rs.irm.administration.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import rs.irm.administration.dto.ModelColumnDTO;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.administration.dto.SqlColumnInfo;
import rs.irm.administration.dto.SqlEditorInfo;
import rs.irm.administration.dto.SqlExecuteResultDTO;
import rs.irm.administration.dto.SqlQueryParametersDTO;
import rs.irm.administration.dto.SqlResultColumnDTO;
import rs.irm.administration.dto.SqlResultDTO;
import rs.irm.administration.enums.ModelColumnType;
import rs.irm.administration.enums.ModelType;
import rs.irm.administration.service.SqlExecutorService;
import rs.irm.administration.utils.ModelData;
import rs.irm.administration.utils.SqlExecute;
import rs.irm.administration.utils.SqlQueryExcel;
import rs.irm.administration.utils.SqlQueryExecute;
import rs.irm.common.dto.Base64DownloadFileDTO;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.common.service.impl.ExportExcelServiceImpl;
import rs.irm.database.service.DatatableService;
import rs.irm.utils.ExcelCellStyleCreator;

@Named
public class SqlExecutorServiceImpl implements SqlExecutorService {

	@Inject
	private ResourceBundleService resourceBundleService;

	@Inject
	private DatatableService datatableService;

	@Context
	private HttpServletRequest httpServletRequest;
	
	@Inject
	private CommonService commonService;

	@Override
	public List<SqlEditorInfo> getTablesAndColumns() {
		List<ModelDTO> tables = new ArrayList<>(
				ModelData.listModelDTOs.stream().filter(a -> a.getType().equals(ModelType.TABLE.name())).toList());

		for (ModelDTO table : tables) {
			table.setName(resourceBundleService.getText(table.getName(), null));
		}

		tables = tables.stream().sorted(Comparator.comparing(ModelDTO::getName)).toList();

		List<SqlEditorInfo> sqlEditorInfos = new ArrayList<>();
		List<ModelColumnDTO> columnDTOs = new ArrayList<>(ModelData.listColumnDTOs);

		for (ModelDTO modelDTO : tables) {
			SqlEditorInfo editorInfo = new SqlEditorInfo();
			editorInfo.setCode(modelDTO.getCode());
			editorInfo.setName(modelDTO.getName());
			editorInfo.setIcon(modelDTO.getIcon());
			sqlEditorInfos.add(editorInfo);

			SqlColumnInfo idColumn = new SqlColumnInfo("id", resourceBundleService.getText("id", null), null, null);
			editorInfo.getColumns().add(idColumn);

			SqlColumnInfo codeColumn = new SqlColumnInfo("code", resourceBundleService.getText("code", null), null, null);
			editorInfo.getColumns().add(codeColumn);

			SqlColumnInfo lockColumn = new SqlColumnInfo("lock", resourceBundleService.getText("lock", null), null, null);
			editorInfo.getColumns().add(lockColumn);

			if (modelDTO.getParentId() != null && modelDTO.getParentType().equals(ModelType.TABLE.name())) {
				ModelDTO parentModelDTO = tables.stream().filter(a -> a.getId().doubleValue() == modelDTO.getParentId().doubleValue()).findFirst()
						.get();

				SqlColumnInfo parentColumn = new SqlColumnInfo(parentModelDTO.getCode(),
						resourceBundleService.getText(parentModelDTO.getName(), null), null, null);
				parentColumn.setCodebookName(resourceBundleService.getText(parentModelDTO.getName(), null));
				parentColumn.setCodebookIcon(parentModelDTO.getIcon());
				editorInfo.getColumns().add(parentColumn);
			}

			List<ModelColumnDTO> columnForTable = columnDTOs.stream().filter(a -> a.getModelId() == modelDTO.getId())
					.toList();

			for (ModelColumnDTO modelColumnDTO : columnForTable) {
				modelColumnDTO.setName(resourceBundleService.getText(modelColumnDTO.getName(), null));
			}

			columnForTable = columnForTable.stream().sorted(Comparator.comparing(ModelColumnDTO::getName)).toList();

			for (ModelColumnDTO columnDTO : columnForTable) {
				SqlColumnInfo sqlColumnInfo = new SqlColumnInfo();
				sqlColumnInfo.setCode(columnDTO.getCode());
				sqlColumnInfo.setName(columnDTO.getName());
				editorInfo.getColumns().add(sqlColumnInfo);

				if (!columnDTO.getColumnType().equals(ModelColumnType.CODEBOOK.name())) {
					continue;
				}

				ModelDTO codebookModelDTO = tables.stream().filter(a -> a.getId().doubleValue() == columnDTO.getCodebookModelId().doubleValue())
						.findFirst().get();

				sqlColumnInfo.setCodebookName(codebookModelDTO.getName());
				sqlColumnInfo.setCodebookIcon(codebookModelDTO.getIcon());

			}
		}

		return sqlEditorInfos;
	}

	@Override
	public SqlResultDTO getSqlQuery(SqlQueryParametersDTO sqlQueryParametersDTO) {
		SqlQueryExecute sqlQueryExecute = new SqlQueryExecute(sqlQueryParametersDTO);
		return this.datatableService.executeMethodWithReturn(sqlQueryExecute);

	}

	@Override
	public Response getExcel(SqlQueryParametersDTO sqlQueryParametersDTO) {
		SqlQueryExcel sqlQueryExcel=new SqlQueryExcel(sqlQueryParametersDTO);
		return this.datatableService.executeMethodWithReturn(sqlQueryExcel);
	}

	@Override
	public Base64DownloadFileDTO getExcelBase64(SqlQueryParametersDTO sqlQueryParametersDTO) {
		
		return commonService.responseToBase64(getExcel(sqlQueryParametersDTO));
	}

	@Override
	public SqlExecuteResultDTO getExecute(SqlQueryParametersDTO sqlQueryParametersDTO) {
		SqlExecute sqlExecute=new SqlExecute(httpServletRequest, sqlQueryParametersDTO,new HashMap<>());
		Long numberOfRows=this.datatableService.executeMethodWithReturn(sqlExecute);
		
		return new SqlExecuteResultDTO(resourceBundleService.getText("updatedRowsNumber", new Object[] {numberOfRows}));
	}

	@Override
	public Response createExcel(SqlResultDTO sqlResultDTO,String sqlQuery,String fileName) {
		try {
			XSSFWorkbook wb = new XSSFWorkbook();

			XSSFSheet sheet = wb.createSheet("result");

			int columnIndex = -1;
			for (SqlResultColumnDTO sqlResultColumnDTO : sqlResultDTO.getColumns()) {
				columnIndex++;
				String type = sqlResultColumnDTO.getType().name();

				switch (type) {
				case "String": {
					sheet.setDefaultColumnStyle(sqlResultColumnDTO.getOrderNumber() - 1,
							ExcelCellStyleCreator.getStringCellStyle(wb, false));
					break;
				}
				case "Integer", "Long": {
					sheet.setDefaultColumnStyle(sqlResultColumnDTO.getOrderNumber() - 1,
							ExcelCellStyleCreator.getIntegerCellStyle(wb, false));
					break;
				}
				case "BigDecimal": {
					CellStyle cellStyle = ExcelCellStyleCreator.getBigDecimalCellStyle(wb, false, 2);
					cellStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("#,##0.00##########"));
					sheet.setDefaultColumnStyle(sqlResultColumnDTO.getOrderNumber() - 1, cellStyle);
					break;
				}
				case "LocalDate": {

					sheet.setDefaultColumnStyle(sqlResultColumnDTO.getOrderNumber() - 1,
							ExcelCellStyleCreator.getDateCellStyle(wb, false));
					break;
				}
				case "LocalDateTime": {
					sheet.setDefaultColumnStyle(sqlResultColumnDTO.getOrderNumber() - 1,
							ExcelCellStyleCreator.getDateTimeCellStyle(wb, false));
					break;
				}
				case "Boolean": {
					sheet.setDefaultColumnStyle(sqlResultColumnDTO.getOrderNumber() - 1,
							ExcelCellStyleCreator.getBooleanCellStyle(wb, false));
					SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
					String columnString = CellReference.convertNumToColString(sqlResultColumnDTO.getOrderNumber() - 1);
					ConditionalFormattingRule trueRule = sheetCF
							.createConditionalFormattingRule(columnString + "2=\"true\"");
					PatternFormatting trueParrent = trueRule.createPatternFormatting();
					trueParrent.setFillBackgroundColor(IndexedColors.GREEN.index);
					trueRule.createFontFormatting().setFontColorIndex(IndexedColors.WHITE.index);
					ConditionalFormattingRule falseRule = sheetCF
							.createConditionalFormattingRule(columnString + "2=\"false\"");
					PatternFormatting falseRulePattern = falseRule.createPatternFormatting();
					falseRulePattern.setFillBackgroundColor(IndexedColors.RED.index);
					falseRule.createFontFormatting().setFontColorIndex(IndexedColors.WHITE.index);
					ConditionalFormattingRule[] cfRules = { trueRule, falseRule };

					CellRangeAddress[] regions = { CellRangeAddress
							.valueOf(columnString + "2:" + columnString + ExportExcelServiceImpl.maximumExcelRow) };
					sheetCF.addConditionalFormatting(regions, cfRules);
					break;
				}
				}
			}

			XSSFRow row = sheet.createRow(0);

			for (SqlResultColumnDTO sqlResultColumnDTO : sqlResultDTO.getColumns()) {
				XSSFCell cell = row.createCell(sqlResultColumnDTO.getOrderNumber() - 1);
				cell.setCellValue(sqlResultColumnDTO.getName());
				cell.setCellStyle(ExcelCellStyleCreator.getTitleCellStyle(wb, true));
			}

			int index = 0;
			for (LinkedHashMap<Integer, Object> data : sqlResultDTO.getList()) {
				index++;
				row = sheet.createRow(index);

				for (SqlResultColumnDTO sqlResultColumnDTO : sqlResultDTO.getColumns()) {
					XSSFCell cell = row.createCell(sqlResultColumnDTO.getOrderNumber() - 1);

					Object value = data.get(sqlResultColumnDTO.getOrderNumber());

					if (value == null || value.toString().length() == 0) {
						continue;
					}

					String type = sqlResultColumnDTO.getType().name();

					switch (type) {
					case "String": {
						cell.setCellValue(value.toString());
						break;
					}
					case "Integer", "Long": {
						Number number = (Number) value;
						cell.setCellValue(number.longValue());
						break;
					}
					case "BigDecimal": {
						Number number = (Number) value;
						cell.setCellValue(number.doubleValue());
						break;
					}
					case "LocalDate": {
						LocalDate date = (LocalDate) value;
						cell.setCellValue(date);
						break;
					}
					case "LocalDateTime": {
						LocalDateTime date = (LocalDateTime) value;
						cell.setCellValue(date);
						break;
					}
					case "Boolean": {
						cell.setCellValue(value.toString());
						break;
					}
					}
				}
			}
			sheet.setAutoFilter(CellRangeAddress.valueOf(
					"A1:" + CellReference.convertNumToColString(columnIndex) + "" + (sqlResultDTO.getList().size() + 1)));

			for (SqlResultColumnDTO sqlResultColumnDTO : sqlResultDTO.getColumns()) {
				sheet.autoSizeColumn(sqlResultColumnDTO.getOrderNumber() - 1);
			}
			
			XSSFSheet sqlSheet=wb.createSheet("query");
			XSSFRow rowQuery=sqlSheet.createRow(0);
			XSSFCell cell=rowQuery.createCell(0);
			cell.setCellValue(sqlQuery);
			sqlSheet.autoSizeColumn(0);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				wb.write(baos);
				wb.close();
			} catch (IOException e) {
				throw new WebApplicationException(e);
			}

			Response response = Response.ok().entity(baos.toByteArray())
					.header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
					.header("Content-Disposition", "attachment;filename="+fileName+".xlsx")
					.header("filename", fileName+".xlsx").header("Access-Control-Expose-Headers", "filename").build();
			return response;
			
		}catch(Exception e) {
			throw new WebApplicationException(e);
		}
	}


}
