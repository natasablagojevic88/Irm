package rs.irm.preview.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Hyperlink;
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

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import rs.irm.administration.dto.ModelColumnDTO;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.administration.dto.ModelJasperReportDTO;
import rs.irm.administration.enums.ModelColumnType;
import rs.irm.administration.enums.ModelType;
import rs.irm.administration.utils.ModelData;
import rs.irm.common.dto.Base64DownloadFileDTO;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.common.service.impl.ExportExcelServiceImpl;
import rs.irm.common.utils.CheckAdmin;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.utils.TableFilter;
import rs.irm.preview.dto.SubCodebookInfoDTO;
import rs.irm.preview.enums.CheckRole;
import rs.irm.preview.service.PreviewModelService;
import rs.irm.preview.utils.ChangeEventExecute;
import rs.irm.preview.utils.CheckDisabledCodebook;
import rs.irm.preview.utils.CheckTotal;
import rs.irm.preview.utils.CreateDefaultValue;
import rs.irm.preview.utils.CreateExcelCodebook;
import rs.irm.preview.utils.CreateListCombobox;
import rs.irm.preview.utils.CreateSubCodebook;
import rs.irm.preview.utils.DeleteItem;
import rs.irm.preview.utils.ExcelImport;
import rs.irm.preview.utils.ExecuteProcedure;
import rs.irm.preview.utils.LockItem;
import rs.irm.preview.utils.PreviewItem;
import rs.irm.preview.utils.PreviewTableCreate;
import rs.irm.preview.utils.PrintJasperReport;
import rs.irm.preview.utils.UnlockItem;
import rs.irm.preview.utils.UpdateItem;
import rs.irm.utils.ExcelCellStyleCreator;

@Named
public class PreviewModelServiceImpl implements PreviewModelService {

	@Context
	private HttpServletRequest httpServletRequest;

	@Inject
	private CommonService commonService;

	@Inject
	private DatatableService datatableService;

	@Inject
	private ResourceBundleService resourceBundleService;

	@Override
	public TableDataDTO<LinkedHashMap<String, Object>> getTable(TableParameterDTO tableParameterDTO, Long modelId,
			Long parentId, boolean checkRight) {

		if (checkRight) {
			checkRole(CheckRole.PREVIEW, modelId);
		}
		ModelDTO modelDTO = ModelData.listModelDTOs.stream()
				.filter(a -> a.getId().doubleValue() == modelId.doubleValue()).findFirst().get();

		if (modelDTO.getParentId() != null && parentId != -1L
				&& modelDTO.getParentType().equals(ModelType.TABLE.name())) {
			TableFilter tableFilter = new TableFilter();
			tableFilter.setField(modelDTO.getParentCode());
			tableFilter.setParameter1(String.valueOf(parentId));
			tableFilter.setSearchOperation(SearchOperation.equals);
			tableParameterDTO.getTableFilters().add(tableFilter);
		}
		PreviewTableCreate previewTableCreate = new PreviewTableCreate(tableParameterDTO, modelId, parentId,
				httpServletRequest);
		return datatableService.executeMethodWithReturn(previewTableCreate);
	}

	@Override
	public LinkedHashMap<String, Object> getDefaultValues(Long modelID, Long parentId) {
		checkRole(CheckRole.UPDATE, modelID);
		CreateDefaultValue createDefaultValue = new CreateDefaultValue(httpServletRequest, modelID, parentId);
		return datatableService.executeMethodWithReturn(createDefaultValue);
	}

	private boolean isAdmin() {
		return commonService.getRoles().contains(CheckAdmin.roleAdmin);
	}

	private boolean checkRole(CheckRole checkRole, Long modelId) {
		if (isAdmin()) {
			return true;
		}
		ModelDTO modelDTO = ModelData.listModelDTOs.stream()
				.filter(a -> a.getId().doubleValue() == modelId.doubleValue()).findFirst().get();
		boolean checkRight = false;
		switch (checkRole.name()) {
		case "PREVIEW": {
			checkRight = commonService.getRoles().contains(modelDTO.getPreviewRoleCode());
			break;
		}
		case "UPDATE": {
			checkRight = commonService.getRoles().contains(modelDTO.getUpdateRoleCode());
			break;
		}
		case "LOCK": {
			checkRight = commonService.getRoles().contains(modelDTO.getLockRoleCode());
			break;
		}
		case "UNLOCK": {
			checkRight = commonService.getRoles().contains(modelDTO.getUnlockRoleCode());
			break;
		}
		}

		if (checkRight) {
			return true;
		} else {
			throw new CommonException(HttpURLConnection.HTTP_FORBIDDEN, "noRight", modelDTO.getCode());
		}
	}

	@Override
	public LinkedHashMap<String, List<ComboboxDTO>> getCodebooks(Long modelId) {
		checkRole(CheckRole.PREVIEW, modelId);
		CreateListCombobox createListCombobox = new CreateListCombobox(modelId, httpServletRequest);

		return this.datatableService.executeMethodWithReturn(createListCombobox);
	}

	@Override
	public SubCodebookInfoDTO getSubCodebooks(Long modelId, String codebook, Long codebookValue) {
		checkRole(CheckRole.PREVIEW, modelId);
		CreateSubCodebook createSubCodebook = new CreateSubCodebook(httpServletRequest, modelId, codebook,
				codebookValue);
		return this.datatableService.executeMethodWithReturn(createSubCodebook);
	}

	@Override
	public TableDataDTO<LinkedHashMap<String, Object>> getCodebooksTable(TableParameterDTO tableParameterDTO,
			Long modelId, String codebook, Long parentid) {
		checkRole(CheckRole.PREVIEW, modelId);
		ModelColumnDTO modelColumnDTO = ModelData.listColumnDTOs.stream().filter(a -> a.getModelId() == modelId)
				.filter(a -> a.getCode().equals(codebook)).findFirst().get();

		TableDataDTO<LinkedHashMap<String, Object>> tableDataDTO = getTable(tableParameterDTO,
				modelColumnDTO.getCodebookModelId(), parentid, false);
		tableDataDTO.setTableWidth(0);
		tableDataDTO.setSubtables(new ArrayList<>());
		tableDataDTO.setRights(null);
		return tableDataDTO;
	}

	@Override
	public LinkedHashMap<String, Object> getUpdate(Long modelID, Long parentId, LinkedHashMap<String, Object> item) {
		checkRole(CheckRole.UPDATE, modelID);
		UpdateItem updateItem = new UpdateItem(httpServletRequest, modelID, parentId, item, true);
		return datatableService.executeMethodWithReturn(updateItem);
	}

	@Override
	public void getDelete(Long modelId, Long id) {
		checkRole(CheckRole.UPDATE, modelId);
		DeleteItem deleteItem = new DeleteItem(httpServletRequest, modelId, id);
		datatableService.executeMethod(deleteItem);

	}

	@Override
	public LinkedHashMap<String, Object> getLock(Long modelId, Long id) {
		checkRole(CheckRole.LOCK, modelId);
		LockItem lockItem = new LockItem(httpServletRequest, modelId, id);
		return datatableService.executeMethodWithReturn(lockItem);
	}

	@Override
	public LinkedHashMap<String, Object> getUnlock(Long modelId, Long id) {
		checkRole(CheckRole.UNLOCK, modelId);
		UnlockItem unlockItem = new UnlockItem(httpServletRequest, modelId, id);
		return datatableService.executeMethodWithReturn(unlockItem);
	}
	
	@Override
	public LinkedHashMap<String, Object> getPreview(Long modelId, Long id) {
		checkRole(CheckRole.PREVIEW, modelId);
		PreviewItem previewItem = new PreviewItem(httpServletRequest, modelId, id);
		return datatableService.executeMethodWithReturn(previewItem);
	}

	@Override
	public Response getExcelTemplate(Long modelId) {
		checkRole(CheckRole.UPDATE, modelId);

		try {
			XSSFWorkbook workbook = new XSSFWorkbook();

			createImportSheet(workbook, modelId);

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			workbook.write(byteArrayOutputStream);
			workbook.close();
			Response response = Response.status(HttpURLConnection.HTTP_OK)
					.header("Content-Disposition",
							"attachment;filename=" + getModel(modelId).getCode() + "Template.xlsx")
					.header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
					.header("filename", getModel(modelId).getCode() + "Template.xlsx")
					.header("Access-Control-Expose-Headers", "filename").entity(byteArrayOutputStream.toByteArray())
					.build();
			return response;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public Base64DownloadFileDTO getExcelTemplateBase64(Long modelId) {

		return commonService.responseToBase64(getExcelTemplate(modelId));
	}

	private ModelDTO getModel(Long modelId) {
		return ModelData.listModelDTOs.stream().filter(a -> a.getId().doubleValue() == modelId.doubleValue())
				.findFirst().get();
	}

	private List<ModelColumnDTO> getColumns(Long modelId) {
		return ModelData.listColumnDTOs.stream().filter(a -> a.getModelId().doubleValue() == modelId.doubleValue())
				.filter(a -> a.getDisabled() == false).sorted(Comparator.comparing(ModelColumnDTO::getRowNumber)
						.thenComparing(ModelColumnDTO::getColumnNumber))
				.toList();
	}

	private void createImportSheet(XSSFWorkbook workbook, Long modelId) {
		XSSFSheet sheet = workbook.createSheet("import");

		ModelDTO modelDTO = getModel(modelId);
		List<ModelColumnDTO> columns = getColumns(modelId);

		if (modelDTO.getNumericCode()) {
			sheet.setDefaultColumnStyle(0, ExcelCellStyleCreator.getIntegerCellStyle(workbook, false));
		} else {
			sheet.setDefaultColumnStyle(0, ExcelCellStyleCreator.getStringCellStyle(workbook, false));
		}

		sheet.setColumnWidth(0, 15 * 256);
		int columnIndex = 0;
		for (ModelColumnDTO modelColumnDTO : columns) {
			columnIndex++;

			String type = modelColumnDTO.getColumnType();
			String columnName = resourceBundleService.getText(modelColumnDTO.getName(), null);
			int columnNameLength = columnName.length() + 5;

			switch (type) {
			case "STRING": {
				sheet.setDefaultColumnStyle(columnIndex, ExcelCellStyleCreator.getStringCellStyle(workbook, false));
				sheet.setColumnWidth(columnIndex, (columnNameLength > 60 ? columnNameLength : 60) * 256);
				break;
			}
			case "LONG", "INTEGER": {
				sheet.setDefaultColumnStyle(columnIndex, ExcelCellStyleCreator.getIntegerCellStyle(workbook, false));
				sheet.setColumnWidth(columnIndex, (columnNameLength > 10 ? columnNameLength : 10) * 256);
				break;
			}
			case "BIGDECIMAL": {
				sheet.setDefaultColumnStyle(columnIndex,
						ExcelCellStyleCreator.getBigDecimalCellStyle(workbook, false, modelColumnDTO.getPrecision()));
				sheet.setColumnWidth(columnIndex, (columnNameLength > 15 ? columnNameLength : 15) * 256);
				break;
			}
			case "LOCALDATE": {
				sheet.setDefaultColumnStyle(columnIndex, ExcelCellStyleCreator.getDateCellStyle(workbook, false));
				sheet.setColumnWidth(columnIndex, (columnNameLength > 10 ? columnNameLength : 10) * 256);
				break;
			}
			case "LOCALDATETIME": {
				sheet.setDefaultColumnStyle(columnIndex, ExcelCellStyleCreator.getDateTimeCellStyle(workbook, false));
				sheet.setColumnWidth(columnIndex, (columnNameLength > 15 ? columnNameLength : 15) * 256);
				break;
			}
			case "BOOLEAN": {
				sheet.setDefaultColumnStyle(columnIndex, ExcelCellStyleCreator.getBooleanCellStyle(workbook, false));
				sheet.setColumnWidth(columnIndex, (columnNameLength > 5 ? columnNameLength : 5) * 256);
				SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
				String columnString = CellReference.convertNumToColString(columnIndex);
				ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(columnString + "3=\"false\"");
				PatternFormatting patternFormatting = rule1.createPatternFormatting();
				patternFormatting.setFillBackgroundColor(IndexedColors.RED.index);
				patternFormatting.setFillPattern(FillPatternType.SOLID_FOREGROUND.getCode());
				rule1.createFontFormatting().setFontColorIndex(IndexedColors.WHITE.index);
				ConditionalFormattingRule rule2 = sheetCF.createConditionalFormattingRule(columnString + "3=\"true\"");
				patternFormatting = rule2.createPatternFormatting();
				patternFormatting.setFillBackgroundColor(IndexedColors.GREEN.index);
				patternFormatting.setFillPattern(FillPatternType.SOLID_FOREGROUND.getCode());
				rule2.createFontFormatting().setFontColorIndex(IndexedColors.WHITE.index);
				ConditionalFormattingRule[] cfRules = { rule1, rule2 };
				CellRangeAddress[] regions = { CellRangeAddress
						.valueOf(columnString + "3:" + columnString + ExportExcelServiceImpl.maximumExcelRow) };
				sheetCF.addConditionalFormatting(regions, cfRules);

				break;
			}
			case "CODEBOOK": {
				if (modelColumnDTO.getCodebookModelNumericCode()) {
					sheet.setDefaultColumnStyle(columnIndex,
							ExcelCellStyleCreator.getIntegerCellStyle(workbook, false));
				} else {
					sheet.setDefaultColumnStyle(columnIndex, ExcelCellStyleCreator.getStringCellStyle(workbook, false));
				}
				sheet.setColumnWidth(columnIndex, (columnNameLength > 15 ? columnNameLength : 15) * 256);

				break;
			}
			}

		}

		sheet.getColumnStyle(columnIndex).setBorderRight(BorderStyle.THIN);

		XSSFRow row = sheet.createRow(0);
		XSSFCell cell = row.createCell(0);
		cell.setCellValue("code");
		cell.setCellStyle(ExcelCellStyleCreator.getStringCellStyle(workbook, true));

		columnIndex = 0;

		for (ModelColumnDTO modelColumnDTO : columns) {
			columnIndex++;
			cell = row.createCell(columnIndex);
			cell.setCellValue(modelColumnDTO.getCode());
			cell.setCellStyle(ExcelCellStyleCreator.getStringCellStyle(workbook, true));
		}
		row.setZeroHeight(true);

		row = sheet.createRow(1);
		columnIndex = 0;

		cell = row.createCell(columnIndex);
		cell.setCellValue(resourceBundleService.getText("code", null));
		cell.setCellStyle(ExcelCellStyleCreator.getTitleCellStyle(workbook, modelDTO.getNumericCode()));

		for (ModelColumnDTO modelColumnDTO : columns) {
			columnIndex++;
			cell = row.createCell(columnIndex);
			cell.setCellValue(resourceBundleService.getText(modelColumnDTO.getName(), null));
			cell.setCellStyle(ExcelCellStyleCreator.getTitleCellStyle(workbook, modelColumnDTO.getNullable()));

			if (modelColumnDTO.getColumnType().equals(ModelColumnType.CODEBOOK.name())) {
				CreationHelper createHelper = workbook.getCreationHelper();
				Hyperlink link = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
				link.setAddress(modelColumnDTO.getCodebookModelCode() + "!A1");
				cell.setHyperlink(link);

				cell.setHyperlink(link);
			}
		}

		row.getCell(columnIndex).getCellStyle().setBorderRight(BorderStyle.THIN);

		sheet.setDisplayGridlines(false);
		sheet.protectSheet(UUID.randomUUID().toString());

		CreateExcelCodebook createExcelCodebook = new CreateExcelCodebook(httpServletRequest, workbook, modelId,
				columns);
		LinkedHashMap<String, XSSFSheet> mapSheet = this.datatableService.executeMethodWithReturn(createExcelCodebook);
		addValidations(sheet, mapSheet, modelDTO, columns);

		workbook.lockStructure();
	}

	private void addValidations(XSSFSheet importSheet, LinkedHashMap<String, XSSFSheet> mapSheet, ModelDTO modelDTO,
			List<ModelColumnDTO> columns) {

		if (modelDTO.getNumericCode()) {
			XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(importSheet);
			DataValidationConstraint dvConstraint = dvHelper.createNumericConstraint(
					DataValidationConstraint.ValidationType.INTEGER, DataValidationConstraint.OperatorType.BETWEEN,
					"-999999999999999", "999999999999999");
			CellRangeAddressList addressList = new CellRangeAddressList(2, ExportExcelServiceImpl.maximumExcelRow - 1,
					0, 0);

			DataValidation dataValidation = dvHelper.createValidation(dvConstraint, addressList);
			dataValidation.setEmptyCellAllowed(true);
			dataValidation.setShowErrorBox(true);
			dataValidation.createErrorBox(resourceBundleService.getText("error", null),
					resourceBundleService.getText("excelOnlyNumber", null));
			importSheet.addValidationData(dataValidation);
		}

		int columnIndex = 0;
		for (ModelColumnDTO column : columns) {
			columnIndex++;

			String columnType = column.getColumnType();

			switch (columnType) {

			case "INTEGER", "LONG": {
				XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(importSheet);
				DataValidationConstraint dvConstraint = dvHelper.createNumericConstraint(
						DataValidationConstraint.ValidationType.INTEGER, DataValidationConstraint.OperatorType.BETWEEN,
						"-999999999999999", "999999999999999");
				CellRangeAddressList addressList = new CellRangeAddressList(2,
						ExportExcelServiceImpl.maximumExcelRow - 1, columnIndex, columnIndex);

				DataValidation dataValidation = dvHelper.createValidation(dvConstraint, addressList);
				dataValidation.setEmptyCellAllowed(true);
				dataValidation.setShowErrorBox(true);
				dataValidation.createErrorBox(resourceBundleService.getText("error", null),
						resourceBundleService.getText("excelOnlyNumber", null));
				importSheet.addValidationData(dataValidation);
				break;
			}
			case "BIGDECIMAL": {
				XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(importSheet);
				DataValidationConstraint dvConstraint = dvHelper.createNumericConstraint(
						DataValidationConstraint.ValidationType.DECIMAL, DataValidationConstraint.OperatorType.BETWEEN,
						"-999999999999999", "999999999999999");
				CellRangeAddressList addressList = new CellRangeAddressList(2,
						ExportExcelServiceImpl.maximumExcelRow - 1, columnIndex, columnIndex);

				DataValidation dataValidation = dvHelper.createValidation(dvConstraint, addressList);
				dataValidation.setEmptyCellAllowed(true);
				dataValidation.setShowErrorBox(true);
				dataValidation.createErrorBox(resourceBundleService.getText("error", null),
						resourceBundleService.getText("excelOnlyNumber", null));
				importSheet.addValidationData(dataValidation);
				break;
			}
			case "LOCALDATE", "LOCALDATETIME": {
				XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(importSheet);
				DataValidationConstraint dvConstraint = dvHelper.createDateConstraint(
						DataValidationConstraint.OperatorType.BETWEEN,
						"" + DateUtil.getExcelDate(LocalDate.parse("1900-01-01")),
						"" + DateUtil.getExcelDate(LocalDate.parse("2999-12-31")), "");
				CellRangeAddressList addressList = new CellRangeAddressList(2,
						ExportExcelServiceImpl.maximumExcelRow - 1, columnIndex, columnIndex);

				DataValidation dataValidation = dvHelper.createValidation(dvConstraint, addressList);
				dataValidation.setEmptyCellAllowed(true);
				dataValidation.setShowErrorBox(true);
				dataValidation.createErrorBox(resourceBundleService.getText("error", null),
						resourceBundleService.getText("excelOnlyDate", null));
				importSheet.addValidationData(dataValidation);
				break;
			}
			case "BOOLEAN": {
				XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(importSheet);
				DataValidationConstraint dvConstraint = dvHelper
						.createExplicitListConstraint(new String[] { "true", "false" });
				CellRangeAddressList addressList = new CellRangeAddressList(2,
						ExportExcelServiceImpl.maximumExcelRow - 1, columnIndex, columnIndex);

				DataValidation dataValidation = dvHelper.createValidation(dvConstraint, addressList);
				dataValidation.setEmptyCellAllowed(true);
				dataValidation.setShowErrorBox(true);
				dataValidation.createErrorBox(resourceBundleService.getText("error", null),
						resourceBundleService.getText("excelOnlyBoolean", null));
				importSheet.addValidationData(dataValidation);
				break;
			}
			case "CODEBOOK": {
				String codebookCode = column.getCodebookModelCode();

				XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(importSheet);
				String listOfValues = createListOfValue(codebookCode, column.getCodebookModelNumericCode(), mapSheet);

				DataValidationConstraint dvConstraint = dvHelper.createFormulaListConstraint(listOfValues);
				CellRangeAddressList addressList = new CellRangeAddressList(2,
						ExportExcelServiceImpl.maximumExcelRow - 1, columnIndex, columnIndex);

				DataValidation dataValidation = dvHelper.createValidation(dvConstraint, addressList);
				dataValidation.setEmptyCellAllowed(true);
				dataValidation.setShowErrorBox(true);
				dataValidation.setSuppressDropDownArrow(true);

				dataValidation.createErrorBox(resourceBundleService.getText("error", null),
						resourceBundleService.getText("excelOnlyCodebook", new Object[] { codebookCode }));
				importSheet.addValidationData(dataValidation);

				break;
			}
			}
		}
	}

	private String createListOfValue(String codebookCode, boolean codebookNumbericCode,
			LinkedHashMap<String, XSSFSheet> mapSheet) {

		XSSFSheet sheetWorkbook = mapSheet.get(codebookCode);

		if (codebookNumbericCode) {
			List<Long> codeNumeric = new ArrayList<>();
			for (int i = 1; i < sheetWorkbook.getPhysicalNumberOfRows(); i++) {
				Cell cell = sheetWorkbook.getRow(i).getCell(0);
				Number number = cell.getNumericCellValue();
				codeNumeric.add(number.longValue());

			}

			codeNumeric = codeNumeric.stream().distinct().sorted(Comparator.comparing(a -> a)).toList();

			XSSFWorkbook workbook = sheetWorkbook.getWorkbook();
			XSSFSheet uniqueSheet = workbook.createSheet(codebookCode + "Unique");

			int rowIndex = -1;
			for (Long number : codeNumeric) {
				rowIndex++;
				XSSFRow row = uniqueSheet.createRow(rowIndex);
				row.createCell(0).setCellValue(number);
			}
			uniqueSheet.protectSheet(UUID.randomUUID().toString());
			workbook.setSheetHidden(workbook.getSheetIndex(uniqueSheet), true);

			return uniqueSheet.getSheetName() + "!$A$1:$A$" + codeNumeric.size();

		} else {
			List<String> codes = new ArrayList<>();
			for (int i = 1; i < sheetWorkbook.getPhysicalNumberOfRows(); i++) {
				Cell cell = sheetWorkbook.getRow(i).getCell(0);
				codes.add(cell.getStringCellValue());

			}

			codes = codes.stream().distinct().sorted().map(a -> a.toString()).toList();

			XSSFWorkbook workbook = sheetWorkbook.getWorkbook();
			XSSFSheet uniqueSheet = workbook.createSheet(codebookCode + "Unique");

			int rowIndex = -1;
			for (String code : codes) {
				rowIndex++;
				XSSFRow row = uniqueSheet.createRow(rowIndex);
				row.createCell(0).setCellValue(code);
			}
			uniqueSheet.protectSheet(UUID.randomUUID().toString());
			workbook.setSheetHidden(workbook.getSheetIndex(uniqueSheet), true);

			return uniqueSheet.getSheetName() + "!$A$1:$A$" + codes.size();

		}

	}

	@Override
	public TableDataDTO<LinkedHashMap<String, Object>> getCheckTotal(TableParameterDTO tableParameterDTO, Long modelId,
			Long parentId) {
		checkRole(CheckRole.PREVIEW, modelId);
		ModelDTO modelDTO = ModelData.listModelDTOs.stream()
				.filter(a -> a.getId().doubleValue() == modelId.doubleValue()).findFirst().get();

		if (modelDTO.getParentId() != null && parentId != -1L
				&& modelDTO.getParentType().equals(ModelType.TABLE.name())) {
			TableFilter tableFilter = new TableFilter();
			tableFilter.setField(modelDTO.getParentCode());
			tableFilter.setParameter1(String.valueOf(parentId));
			tableFilter.setSearchOperation(SearchOperation.equals);
			tableParameterDTO.getTableFilters().add(tableFilter);
		}

		CheckTotal checkTotal = new CheckTotal(httpServletRequest, modelId, tableParameterDTO);
		return this.datatableService.executeMethodWithReturn(checkTotal);
	}

	@Override
	public LinkedHashMap<String, Object> getImportExcel(File excelFile, Long modelId, Long parentId) {
		if (!excelFile.exists()) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "noFileFound", null);
		}

		ExcelImport excelImport = new ExcelImport(httpServletRequest, modelId, parentId, excelFile);
		return this.datatableService.executeMethodWithReturn(excelImport);
	}

	@Override
	public Response getPrintJasper(Long jasperReportId, Long id) {

		ModelJasperReportDTO jasperReportDTO = ModelData.listModelJasperReportDTOs.stream()
				.filter(a -> a.getId().doubleValue() == jasperReportId.doubleValue()).findFirst().get();

		checkRole(CheckRole.PREVIEW, jasperReportDTO.getModelId());

		PrintJasperReport jasperPrint = new PrintJasperReport(jasperReportId, id, httpServletRequest);
		ByteArrayOutputStream byteArrayOutputStream = this.datatableService.executeMethodWithReturn(jasperPrint);

		Response response = Response.status(HttpURLConnection.HTTP_OK)
				.header("Content-Disposition", "attachment;filename=print.pdf")
				.header("Content-Type", "application/pdf").header("filename", "print.pdf")
				.header("Access-Control-Expose-Headers", "filename").entity(byteArrayOutputStream.toByteArray())
				.build();
		return response;
	}

	@Override
	public Base64DownloadFileDTO getPrintJasperBase64(Long jasperReportId, Long id) {
		Response response = getPrintJasper(jasperReportId, id);
		return commonService.responseToBase64(response);
	}

	@Override
	public LinkedHashMap<String, Object> getChangeEvent(LinkedHashMap<String, Object> value, Long modelId,
			Long parentId, String jsonFunction) {
		checkRole(CheckRole.UPDATE, modelId);

		List<ModelColumnDTO> modelColumnDTOs = ModelData.listColumnDTOs.stream().filter(a -> a.getModelId() == modelId)
				.filter(a -> a.getEventFunction() != null).filter(a -> a.getEventFunction().equals(jsonFunction))
				.toList();

		if (modelColumnDTOs.isEmpty()) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "NoFunction", jsonFunction);
		}

		ModelDTO modelDTO = getModel(modelId);
		if (modelDTO.getParentId() != null && modelDTO.getParentType().equals(ModelType.TABLE.name())) {
			value.put(modelDTO.getParentCode(), parentId);
		}

		ChangeEventExecute changeEventExecute = new ChangeEventExecute(value, jsonFunction);

		return this.datatableService.executeMethodWithReturn(changeEventExecute);
	}

	@Override
	public LinkedHashMap<String, List<ComboboxDTO>> getCodebookDisabled(LinkedHashMap<String, Object> value,
			Long modelId) {

		List<ModelColumnDTO> columnDTOs = ModelData.listColumnDTOs.stream()
				.filter(a -> a.getModelId().doubleValue() == modelId.doubleValue())
				.filter(a -> a.getColumnType().equals(ModelColumnType.CODEBOOK.name()))
				.filter(a -> a.getDisabled() == true).toList();

		CheckDisabledCodebook checkDisabledCodebook = new CheckDisabledCodebook(value, columnDTOs);

		return this.datatableService.executeMethodWithReturn(checkDisabledCodebook);
	}

	@Override
	public void getProcedureExecute(Long procedureId, Long modelId, Long id) {
		checkRole(CheckRole.UPDATE, modelId);
		
		ExecuteProcedure executeProcedure=new ExecuteProcedure(procedureId, modelId, id, httpServletRequest);
		this.datatableService.executeMethod(executeProcedure);
	}

}
