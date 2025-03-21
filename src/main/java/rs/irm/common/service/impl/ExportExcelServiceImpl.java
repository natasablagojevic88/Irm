package rs.irm.common.service.impl;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

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
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import rs.irm.common.dto.Base64DownloadFileDTO;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.ExportExcelService;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.utils.ColumnData;
import rs.irm.utils.ExcelCellStyleCreator;

@Named
public class ExportExcelServiceImpl implements ExportExcelService {

	public static Integer maximumExcelRow = 1048576;
	
	@Inject
	private CommonService commonService;

	@Override
	public Response getExportExcel(TableDataDTO<?> tableDataDTO) {

		try {
			XSSFWorkbook wb = new XSSFWorkbook();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			XSSFSheet sheet = wb.createSheet(tableDataDTO.getTable());

			int columnIndex = -1;
			for (ColumnData columnData : tableDataDTO.getColumns()) {
				columnIndex++;
				String columnType = columnData.getColumnType().name();

				switch (columnType) {
				case "String":
					sheet.setDefaultColumnStyle(columnIndex, ExcelCellStyleCreator.getStringCellStyle(wb, false));
					break;

				case "Integer", "Long":
					sheet.setDefaultColumnStyle(columnIndex, ExcelCellStyleCreator.getIntegerCellStyle(wb, false));
					break;

				case "BigDecimal":
					sheet.setDefaultColumnStyle(columnIndex,
							ExcelCellStyleCreator.getBigDecimalCellStyle(wb, false, columnData.getNumberOfDecimal()));
					break;
				case "LocalDate":
					sheet.setDefaultColumnStyle(columnIndex, ExcelCellStyleCreator.getDateCellStyle(wb, false));
					break;
				case "LocalDateTime":
					sheet.setDefaultColumnStyle(columnIndex, ExcelCellStyleCreator.getDateTimeCellStyle(wb, false));
					break;
				case "Boolean":
					sheet.setDefaultColumnStyle(columnIndex, ExcelCellStyleCreator.getBooleanCellStyle(wb, false));
					SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
					String columnString = CellReference.convertNumToColString(columnIndex);
					ConditionalFormattingRule trueRule = sheetCF
							.createConditionalFormattingRule(columnString + "4=\"true\"");
					PatternFormatting trueParrent = trueRule.createPatternFormatting();
					trueParrent.setFillBackgroundColor(IndexedColors.GREEN.index);
					trueRule.createFontFormatting().setFontColorIndex(IndexedColors.WHITE.index);
					ConditionalFormattingRule falseRule = sheetCF
							.createConditionalFormattingRule(columnString + "4=\"false\"");
					PatternFormatting falseRulePattern = falseRule.createPatternFormatting();
					falseRulePattern.setFillBackgroundColor(IndexedColors.RED.index);
					falseRule.createFontFormatting().setFontColorIndex(IndexedColors.WHITE.index);
					ConditionalFormattingRule[] cfRules = { trueRule, falseRule };

					CellRangeAddress[] regions = {
							CellRangeAddress.valueOf(columnString + "4:" + columnString + maximumExcelRow) };
					sheetCF.addConditionalFormatting(regions, cfRules);

					break;
				}
			}

			// title font

			XSSFRow row = sheet.createRow(0);
			XSSFCell cell = row.createCell(0);
			cell.setCellStyle(ExcelCellStyleCreator.getTitleCellStyle(wb, true));
			cell.setCellValue(tableDataDTO.getTitle());
			sheet.addMergedRegion(new CellRangeAddress(0, // first row (0-based)
					0, // last row (0-based)
					0, // first column (0-based)
					tableDataDTO.getColumns().size() - 1 // last column (0-based)
			));

			int rowIndex = 2;
			row = sheet.createRow(rowIndex);

			columnIndex = -1;
			for (ColumnData columnData : tableDataDTO.getColumns()) {
				columnIndex++;
				cell = row.createCell(columnIndex);
				cell.setCellStyle(ExcelCellStyleCreator.getTitleCellStyle(wb, true));
				cell.setCellValue(columnData.getName());
			}

			for (Object object : tableDataDTO.getList()) {
				rowIndex++;
				row = sheet.createRow(rowIndex);
				@SuppressWarnings("unchecked")
				HashMap<String, Object> dataRow = (HashMap<String, Object>) object;

				columnIndex = -1;

				for (ColumnData columnData : tableDataDTO.getColumns()) {
					columnIndex++;

					if (dataRow.get(columnData.getCode()) == null) {
						continue;
					}

					if (dataRow.get(columnData.getCode()).toString().length() == 0) {
						continue;
					}
					cell = row.createCell(columnIndex);
					switch (columnData.getColumnType().name()) {
					case "String":

						if (isEnum(tableDataDTO, columnData)) {
							cell.setCellValue(getEnumOption(tableDataDTO, columnData,
									dataRow.get(columnData.getCode()).toString()));
						} else {
							cell.setCellValue(dataRow.get(columnData.getCode()).toString());
						}

						break;
					case "Long", "Integer", "BigDecimal":
						Number number = (Number) dataRow.get(columnData.getCode());
						cell.setCellValue(number.doubleValue());
						break;
					case "LocalDate":
						LocalDate localDate = LocalDate.parse(dataRow.get(columnData.getCode()).toString());
						cell.setCellValue(localDate);
						break;
					case "LocalDateTime":
						LocalDateTime localDateTime = LocalDateTime.parse(dataRow.get(columnData.getCode()).toString());
						cell.setCellValue(localDateTime);
						break;
					case "Boolean":
						cell.setCellValue(dataRow.get(columnData.getCode()).toString());
						break;
					}

				}
			}
			columnIndex = -1;
			for (int i = 0; i < tableDataDTO.getColumns().size(); i++) {
				sheet.autoSizeColumn(i);

			}
			String autoFilterRange = CellReference.convertNumToColString(0) + "3";
			autoFilterRange += ":";
			autoFilterRange += CellReference.convertNumToColString(tableDataDTO.getColumns().size() - 1);
			autoFilterRange += (tableDataDTO.getList().size() + 3);
			sheet.setAutoFilter(CellRangeAddress.valueOf(autoFilterRange));

			wb.write(baos);
			wb.close();

			Response response = Response.status(HttpURLConnection.HTTP_OK)
					.header("filename", tableDataDTO.getTable() + ".xlsx")
					.header("Content-Disposition", "attachment;filename=" + tableDataDTO.getTable() + ".xlsx")
					.header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
					.header("Access-Control-Expose-Headers", "filename").entity(baos.toByteArray()).build();
			return response;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	private Boolean isEnum(TableDataDTO<?> tableDataDTO, ColumnData columnData) {

		if (!tableDataDTO.getEnums().stream().filter(a -> a.getCode().equals(columnData.getCode())).toList()
				.isEmpty()) {
			return true;
		}

		return false;
	}

	private String getEnumOption(TableDataDTO<?> tableDataDTO, ColumnData columnData, String value) {

		List<ComboboxDTO> comboboxDTOs = tableDataDTO.getEnums().stream()
				.filter(a -> a.getCode().equals(columnData.getCode())).findFirst().get().getList();

		return comboboxDTOs.stream().filter(a -> a.getValue().toString().equals(value)).findFirst().get().getOption();
	}

	@Override
	public Base64DownloadFileDTO getBase64DownloadFileDTO(TableDataDTO<?> tableDataDTO) {
		
		Response response = getExportExcel(tableDataDTO);
		
		return commonService.responseToBase64(response);
	}

}
