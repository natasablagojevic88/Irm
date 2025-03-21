package rs.irm.utils;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelCellStyleCreator {

	public static CellStyle getStringCellStyle(XSSFWorkbook workbook, boolean isLocked) {
		CellStyle cellStyle = workbook.createCellStyle();
		CreationHelper creationHelper = workbook.getCreationHelper();
		cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("@"));
		cellStyle.setLocked(isLocked);
		cellStyle.setAlignment(HorizontalAlignment.LEFT);

		return cellStyle;
	}
	
	public static CellStyle getIntegerCellStyle(XSSFWorkbook workbook, boolean isLocked) {
		CellStyle cellStyle = workbook.createCellStyle();
		CreationHelper creationHelper = workbook.getCreationHelper();
		cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("0"));
		cellStyle.setAlignment(HorizontalAlignment.RIGHT);
		cellStyle.setLocked(isLocked);

		return cellStyle;
	}
	
	public static CellStyle getBigDecimalCellStyle(XSSFWorkbook workbook, boolean isLocked, int numberOfDecimal) {
		CellStyle cellStyle = workbook.createCellStyle();
		CreationHelper creationHelper = workbook.getCreationHelper();
		String decimal="";
		cellStyle.setAlignment(HorizontalAlignment.RIGHT);
		for(int i=0;i<numberOfDecimal;i++) {
			decimal+="0";
		}
		cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("#,##0."+decimal));
		cellStyle.setLocked(isLocked);

		return cellStyle;
	}
	
	public static CellStyle getBooleanCellStyle(XSSFWorkbook workbook, boolean isLocked) {
		CellStyle cellStyle = workbook.createCellStyle();
		CreationHelper creationHelper = workbook.getCreationHelper();
		cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("@"));
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setLocked(isLocked);

		return cellStyle;
	}
	
	public static CellStyle getDateCellStyle(XSSFWorkbook workbook, boolean isLocked) {
		CellStyle cellStyle = workbook.createCellStyle();
		CreationHelper creationHelper = workbook.getCreationHelper();
		cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("DD.MM.YYYY"));
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setLocked(isLocked);

		return cellStyle;
	}
	
	public static CellStyle getDateTimeCellStyle(XSSFWorkbook workbook, boolean isLocked) {
		CellStyle cellStyle = workbook.createCellStyle();
		CreationHelper creationHelper = workbook.getCreationHelper();
		cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("DD.MM.YYYY HH:MM"));
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setLocked(isLocked);

		return cellStyle;
	}
	
	public static CellStyle getTitleCellStyle(XSSFWorkbook workbook, boolean nullable) {
		CellStyle cellStyle = workbook.createCellStyle();
		CreationHelper creationHelper = workbook.getCreationHelper();
		cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("@"));
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setLocked(true);
		cellStyle.setFillForegroundColor(nullable?IndexedColors.GREY_25_PERCENT.index:IndexedColors.RED.index);
		cellStyle.setFillBackgroundColor(nullable?IndexedColors.GREY_25_PERCENT.index:IndexedColors.RED.index);
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		Font font=workbook.createFont();
		font.setBold(true);
		cellStyle.setFont(font);

		return cellStyle;
	}
}
