package rs.irm.preview.utils;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jakarta.servlet.http.HttpServletRequest;
import rs.irm.administration.dto.ModelColumnDTO;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.administration.enums.ModelColumnType;
import rs.irm.administration.enums.ModelType;
import rs.irm.administration.utils.ModelData;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.common.service.impl.ExportExcelServiceImpl;
import rs.irm.common.service.impl.ResourceBundleServiceImpl;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.preview.service.ModelQueryCreatorService;
import rs.irm.preview.service.impl.ModelQueryCreatorServiceImpl;
import rs.irm.utils.ExcelCellStyleCreator;

public class CreateExcelCodebook implements ExecuteMethodWithReturn<LinkedHashMap<String, XSSFSheet>> {

	private HttpServletRequest httpServletRequest;
	private XSSFWorkbook workbook;
	private Long modelId;
	private List<ModelColumnDTO> columns;

	private ModelQueryCreatorService modelQueryCreatorService;
	private ResourceBundleService resourceBundleService;

	public CreateExcelCodebook(HttpServletRequest httpServletRequest, XSSFWorkbook workbook, Long modelId,
			List<ModelColumnDTO> columns) {
		this.httpServletRequest = httpServletRequest;
		this.workbook = workbook;
		this.modelId = modelId;
		this.columns = columns;
		this.modelQueryCreatorService = new ModelQueryCreatorServiceImpl(this.httpServletRequest);
		this.resourceBundleService = new ResourceBundleServiceImpl(this.httpServletRequest);
	}

	@Override
	public LinkedHashMap<String, XSSFSheet> execute(Connection connection) {

		LinkedHashMap<String, XSSFSheet> mapSheet = new LinkedHashMap<>();

		List<String> createdSheet = new ArrayList<>();

		LinkedHashMap<String, List<ComboboxDTO>> listOfValues = this.modelQueryCreatorService.createListOfValue(modelId,
				connection);

		for (ModelColumnDTO modelColumnDTO : this.columns) {
			if (modelColumnDTO.getColumnType().equals(ModelColumnType.STRING.name())
					&& listOfValues.containsKey(modelColumnDTO.getCode())) {
				addListOfValueValidation(modelColumnDTO.getCode(), listOfValues);
				continue;
			}

			if (!modelColumnDTO.getColumnType().equals(ModelColumnType.CODEBOOK.name())) {
				continue;
			}

			if (createdSheet.contains(modelColumnDTO.getCodebookModelCode())) {
				continue;
			}

			XSSFSheet codebookSheet= addSheet(modelColumnDTO, connection);
			createdSheet.add(modelColumnDTO.getCodebookModelCode());
			mapSheet.put(modelColumnDTO.getCodebookModelCode(), codebookSheet);

		}

		return mapSheet;

	}

	private XSSFSheet addSheet(ModelColumnDTO modelColumnDTO, Connection connection) {

		XSSFSheet sheet = this.workbook.createSheet(modelColumnDTO.getCodebookModelCode());

		ModelDTO modelDTO = ModelData.listModelDTOs.stream()
				.filter(a -> a.getId().doubleValue() == modelColumnDTO.getCodebookModelId().doubleValue()).findFirst().get();
		List<ModelColumnDTO> columns = ModelData.listColumnDTOs.stream().filter(a -> a.getModelId() == modelDTO.getId())
				.filter(a -> a.getShowInTable()).sorted(Comparator.comparing(ModelColumnDTO::getRowNumber)
						.thenComparing(Comparator.comparing(ModelColumnDTO::getColumnNumber)))
				.toList();

		Integer columnCount = formatColumns(sheet, modelDTO, columns);
		addTitle(sheet, modelDTO, columns);

		List<LinkedHashMap<String, Object>> list = modelQueryCreatorService.findAllExcelCodebook(modelDTO.getId(),
				connection);
		fillItems(list, sheet, modelDTO);

		sheet.protectSheet(UUID.randomUUID().toString());

		String maxColumnString = CellReference.convertNumToColString(columnCount - 1);
		sheet.setAutoFilter(CellRangeAddress.valueOf("A1:" + maxColumnString + "" + (list.size() + 1)));

		for (int i = 0; i < columnCount; i++) {
			sheet.autoSizeColumn(i);
		}

		sheet.setDisplayGridlines(false);
		return sheet;
	}

	private Integer formatColumns(XSSFSheet sheet, ModelDTO modelDTO, List<ModelColumnDTO> columns) {
		int columnCount = 1;
		if (modelDTO.getNumericCode()) {
			sheet.setDefaultColumnStyle(0, ExcelCellStyleCreator.getIntegerCellStyle(workbook, true));
		} else {
			sheet.setDefaultColumnStyle(0, ExcelCellStyleCreator.getStringCellStyle(workbook, true));
		}

		ModelDTO searchModelDTO = modelDTO;
		while (searchModelDTO.getParentId() != null && searchModelDTO.getParentType().equals(ModelType.TABLE.name())) {
			Long parentId = searchModelDTO.getParentId();
			ModelDTO foundModel = ModelData.listModelDTOs.stream().filter(a -> a.getId().doubleValue() == parentId.doubleValue()).findFirst().get();
			columnCount++;
			if (foundModel.getNumericCode()) {
				sheet.setDefaultColumnStyle(columnCount - 1, ExcelCellStyleCreator.getIntegerCellStyle(workbook, true));
			} else {
				sheet.setDefaultColumnStyle(columnCount - 1, ExcelCellStyleCreator.getStringCellStyle(workbook, true));
			}

			searchModelDTO = foundModel;
		}

		for (ModelColumnDTO columnDTO : columns) {
			columnCount++;
			String type = columnDTO.getColumnType();

			switch (type) {
			case "STRING": {
				sheet.setDefaultColumnStyle(columnCount - 1, ExcelCellStyleCreator.getStringCellStyle(workbook, true));
				break;
			}
			case "LONG", "INTEGER": {
				sheet.setDefaultColumnStyle(columnCount - 1, ExcelCellStyleCreator.getIntegerCellStyle(workbook, true));
				break;
			}
			case "BIGDECIMAL": {
				sheet.setDefaultColumnStyle(columnCount - 1,
						ExcelCellStyleCreator.getBigDecimalCellStyle(workbook, true, columnDTO.getPrecision()));
				break;
			}
			case "LOCALDATE": {
				sheet.setDefaultColumnStyle(columnCount - 1, ExcelCellStyleCreator.getDateCellStyle(workbook, true));
				break;
			}
			case "LOCALDATETIME": {
				sheet.setDefaultColumnStyle(columnCount - 1,
						ExcelCellStyleCreator.getDateTimeCellStyle(workbook, true));
				break;
			}
			case "BOOLEAN": {
				sheet.setDefaultColumnStyle(columnCount - 1, ExcelCellStyleCreator.getBooleanCellStyle(workbook, true));
				SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
				String columnString = CellReference.convertNumToColString(columnCount - 1);
				ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(columnString + "2=\"false\"");
				PatternFormatting patternFormatting = rule1.createPatternFormatting();
				patternFormatting.setFillBackgroundColor(IndexedColors.RED.index);
				patternFormatting.setFillPattern(FillPatternType.SOLID_FOREGROUND.getCode());
				rule1.createFontFormatting().setFontColorIndex(IndexedColors.WHITE.index);
				ConditionalFormattingRule rule2 = sheetCF.createConditionalFormattingRule(columnString + "2=\"true\"");
				patternFormatting = rule2.createPatternFormatting();
				patternFormatting.setFillBackgroundColor(IndexedColors.GREEN.index);
				patternFormatting.setFillPattern(FillPatternType.SOLID_FOREGROUND.getCode());
				rule2.createFontFormatting().setFontColorIndex(IndexedColors.WHITE.index);
				ConditionalFormattingRule[] cfRules = { rule1, rule2 };
				CellRangeAddress[] regions = { CellRangeAddress
						.valueOf(columnString + "2:" + columnString + ExportExcelServiceImpl.maximumExcelRow) };
				sheetCF.addConditionalFormatting(regions, cfRules);

				break;
			}
			case "CODEBOOK": {

				if (columnDTO.getCodebookModelNumericCode()) {
					sheet.setDefaultColumnStyle(columnCount - 1,
							ExcelCellStyleCreator.getIntegerCellStyle(workbook, true));
				} else {
					sheet.setDefaultColumnStyle(columnCount - 1,
							ExcelCellStyleCreator.getStringCellStyle(workbook, true));
				}

				break;
			}
			}
		}

		return columnCount;
	}

	private void addTitle(XSSFSheet sheet, ModelDTO modelDTO, List<ModelColumnDTO> columns) {
		XSSFRow row = sheet.createRow(0);

		Cell cell = row.createCell(0);
		cell.setCellValue(this.resourceBundleService.getText("code", null));
		cell.setCellStyle(ExcelCellStyleCreator.getTitleCellStyle(workbook, true));

		int columnIndex = 0;
		ModelDTO searchModelDTO = modelDTO;
		while (searchModelDTO.getParentId() != null && searchModelDTO.getParentType().equals(ModelType.TABLE.name())) {
			Long parentId = searchModelDTO.getParentId();
			ModelDTO foundModel = ModelData.listModelDTOs.stream().filter(a -> a.getId().doubleValue() == parentId.doubleValue()).findFirst().get();
			columnIndex++;
			cell = row.createCell(columnIndex);
			cell.setCellValue(this.resourceBundleService.getText(foundModel.getName(), null));
			cell.setCellStyle(ExcelCellStyleCreator.getTitleCellStyle(workbook, true));

			searchModelDTO = foundModel;
		}

		for (ModelColumnDTO columnDTO : columns) {
			columnIndex++;
			cell = row.createCell(columnIndex);
			cell.setCellValue(this.resourceBundleService.getText(columnDTO.getName(), null));
			cell.setCellStyle(ExcelCellStyleCreator.getTitleCellStyle(workbook, true));

		}

	}

	private void fillItems(List<LinkedHashMap<String, Object>> list, XSSFSheet sheet, ModelDTO codebookModel) {

		@SuppressWarnings("unchecked")
		List<ModelColumnDTO> modelColumnDTOs = (List<ModelColumnDTO>) new ModelQueryCreatorServiceImpl()
				.createExcelColumns(codebookModel)[1];

		int rowIndex = 0;

		for (LinkedHashMap<String, Object> item : list) {

			rowIndex++;

			XSSFRow row = sheet.createRow(rowIndex);

			int columnIndex = -1;
			for (ModelColumnDTO modelColumnDTO : modelColumnDTOs) {
				columnIndex++;
				if (item.get(modelColumnDTO.getCode()) == null) {
					continue;
				}
				Object value = item.get(modelColumnDTO.getCode());
				XSSFCell cell = row.createCell(columnIndex);
				String cellType = modelColumnDTO.getColumnType();

				switch (cellType) {
				case "STRING": {
					cell.setCellValue(value.toString());
					break;
				}
				case "INTEGER", "BIGDECIMAL", "LONG": {
					Number number = (Number) value;
					cell.setCellValue(number.doubleValue());
					break;
				}
				case "LOCALDATE": {
					LocalDate date = (LocalDate) value;
					cell.setCellValue(date);
					break;
				}
				case "LOCALDATETIME": {
					LocalDateTime date = (LocalDateTime) value;
					cell.setCellValue(date);
					break;
				}
				case "BOOLEAN": {
					cell.setCellValue(value.toString());
					break;
				}
				case "CODEBOOK": {
					Object codebookValue = item.get(modelColumnDTO.getCode() + "Code");
					if (modelColumnDTO.getCodebookModelNumericCode()) {
						Number number = (Number) codebookValue;
						cell.setCellValue(number.doubleValue());
					} else {
						cell.setCellValue(codebookValue.toString());
					}
					break;
				}
				}
			}

		}

	}

	private void addListOfValueValidation(String columnCode, LinkedHashMap<String, List<ComboboxDTO>> listOfValues) {
		Integer findColumn = -1;

		XSSFSheet sheet = workbook.getSheet("import");
		XSSFRow row = sheet.getRow(0);
		for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
			if (!row.getCell(i).getStringCellValue().equals(columnCode)) {
				continue;
			}

			findColumn = i;
			break;
		}

		List<ComboboxDTO> comboboxSComboboxDTOs = listOfValues.get(columnCode);
		String[] values = new String[comboboxSComboboxDTOs.size()];

		int index = -1;
		for (ComboboxDTO comboboxDTO : comboboxSComboboxDTOs) {
			index++;
			values[index] = comboboxDTO.getValue().toString();
		}

		XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(
				sheet);
		DataValidationConstraint dvConstraint = dvHelper.createExplicitListConstraint(values);
		CellRangeAddressList addressList = new CellRangeAddressList(2, ExportExcelServiceImpl.maximumExcelRow - 1,
				findColumn, findColumn);

		DataValidation dataValidation = dvHelper.createValidation(dvConstraint, addressList);
		dataValidation.setEmptyCellAllowed(true);
		dataValidation.setShowErrorBox(true);
		dataValidation.setSuppressDropDownArrow(true);

		dataValidation.createErrorBox(resourceBundleService.getText("error", null),
				resourceBundleService.getText("valueNotOnListOfValue", null));
		sheet.addValidationData(dataValidation);
	}

}
