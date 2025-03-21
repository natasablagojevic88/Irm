package rs.irm.administration.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import rs.irm.administration.dto.ModelColumnDTO;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.administration.dto.ReportColumnFieldInfoDTO;
import rs.irm.administration.dto.ReportColumnInfoDTO;
import rs.irm.administration.dto.ReportDTO;
import rs.irm.administration.dto.ReportJobDTO;
import rs.irm.administration.dto.SmtpServerDTO;
import rs.irm.administration.entity.Report;
import rs.irm.administration.entity.ReportJasper;
import rs.irm.administration.enums.ModelColumnType;
import rs.irm.administration.service.ReportService;
import rs.irm.administration.utils.ConvertReportToReportDTO;
import rs.irm.administration.utils.DeleteReportJob;
import rs.irm.administration.utils.ModelData;
import rs.irm.administration.utils.UpdateReport;
import rs.irm.administration.utils.UpdateReportJob;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.ColumnType;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.utils.LeftTableData;
import rs.irm.database.utils.TableFilter;
import rs.irm.utils.AppParameters;

@Named
public class ReportServiceImpl implements ReportService {

	@Inject
	private DatatableService datatableService;

	@Inject
	private ResourceBundleService resourceBundleService;

	@Context
	private HttpServletRequest httpServletRequest;

	@Override
	public TableDataDTO<ReportDTO> getTable(TableParameterDTO tableParameterDTO, Long reportGroupId) {
		TableFilter tableFilter = new TableFilter("reportGroupId", SearchOperation.equals,
				String.valueOf(reportGroupId), null);
		tableParameterDTO.getTableFilters().add(tableFilter);
		return datatableService.getTableDataDTO(tableParameterDTO, ReportDTO.class);
	}

	@Override
	public List<ReportColumnInfoDTO> getTreeField(Long modelId) {

		List<ReportColumnInfoDTO> columns = new ArrayList<>();

		ModelDTO modelDTO = findModel(modelId);

		ReportColumnInfoDTO parent = new ReportColumnInfoDTO();
		parent.setName(resourceBundleService.getText(modelDTO.getName(), null));
		parent.setIcon(modelDTO.getIcon());

		parent.getChildren().addAll(getDefaultcolumns(modelId, "", new ArrayList<String>(), new ArrayList<String>(),
				new ArrayList<String>(), new ArrayList<String>(), null));
		parent.getChildren().addAll(getOtherColumnsDTO(modelId, "", new ArrayList<String>(), new ArrayList<String>(),
				new ArrayList<String>(), new ArrayList<String>(), null));
		columns.add(parent);

		readChildren(modelId, "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), parent);

		return columns;
	}

	private void readChildren(Long modelId, String prefixInput, List<String> columnsItem, List<String> tablesList,
			List<String> childTable, List<String> parentList, ReportColumnInfoDTO item) {
		List<ModelDTO> children = findChildren(modelId);
		ModelDTO parentModelData = findModel(modelId);

		for (ModelDTO childModel : children) {
			List<String> childs = new ArrayList<>(childTable);
			childs.add(childModel.getCode());
			List<String> parents = new ArrayList<>(parentList);
			parents.add(parentModelData.getCode());
			List<String> columns = new ArrayList<>(columnsItem);
			columns.add(childModel.getCode());

			List<String> tables = new ArrayList<>(tablesList);
			tables.add(childModel.getCode());

			String prefix = new String(prefixInput);
			prefix += "/" + childModel.getCode();

			ReportColumnInfoDTO child = new ReportColumnInfoDTO();
			child.setName(resourceBundleService.getText(childModel.getName(), null));
			child.setIcon(childModel.getIcon());
			child.getChildren().addAll(getDefaultcolumns(childModel.getId(), prefix, columns, tables, childs, parents,
					parentModelData.getCode()));
			child.getChildren().addAll(getOtherColumnsDTO(childModel.getId(), prefix, columns, tables, childs, parents,
					parentModelData.getCode()));

			item.getChildren().add(child);

			readChildren(childModel.getId(), prefix, columns, tables, childs, parents, child);

		}
	}

	private List<ReportColumnInfoDTO> getDefaultcolumns(Long modelId, String leftPathPrefix, List<String> columnsList,
			List<String> tables, List<String> childTables, List<String> parentTables, String parentTable) {
		List<ReportColumnInfoDTO> columns = new ArrayList<>();

		List<LeftTableData> leftTableDatas = new ArrayList<>();

		String path = "";
		for (int i = 0; i < columnsList.size(); i++) {
			if (parentTable != null || childTables.contains(columnsList.get(i))) {
				LeftTableData leftTableData = new LeftTableData();
				leftTableData.setFieldColumn("id");
				if (parentTable != null) {
					leftTableData.setIdColumn(parentTable);
				} else {
					leftTableData.setIdColumn(parentTables.get(columnsList.indexOf(columnsList.get(i))));
				}

				String pathItem = new String(path);
				pathItem += "/" + columnsList.get(i);
				leftTableData.setPath(new String(pathItem));
				leftTableData.setTable(tables.get(i));
				leftTableDatas.add(leftTableData);
				path = pathItem;
			} else {
				LeftTableData leftTableData = new LeftTableData();
				leftTableData.setFieldColumn(columnsList.get(i));
				leftTableData.setIdColumn("id");
				String pathItem = new String(path);
				pathItem += "/" + columnsList.get(i);
				leftTableData.setPath(new String(pathItem));
				leftTableData.setTable(tables.get(i));
				leftTableDatas.add(leftTableData);
				path = pathItem;
			}

		}

		ModelDTO modelDTO = findModel(modelId);

		// id
		ReportColumnInfoDTO reportColumnInfoDTO = new ReportColumnInfoDTO();

		reportColumnInfoDTO.setName(resourceBundleService.getText("id", null));
		reportColumnInfoDTO.setCode("id");
		reportColumnInfoDTO.setColumnType(ColumnType.Long);

		ReportColumnFieldInfoDTO reportColumnFieldInfoDTO = new ReportColumnFieldInfoDTO();

		reportColumnFieldInfoDTO.setFieldType("Long");
		reportColumnFieldInfoDTO.setLeftJoinPath(leftPathPrefix);
		List<String> columnsArray = new ArrayList<String>(columnsList);
		columnsArray.add("id");
		reportColumnFieldInfoDTO.setColumnList(columnsArray);
		reportColumnFieldInfoDTO.setTableList(tables);
		reportColumnFieldInfoDTO.setFieldName(createFieldName(columnsArray));
		reportColumnInfoDTO.setColumnFieldInfoDTO(reportColumnFieldInfoDTO);
		reportColumnFieldInfoDTO.setLeftTableDatas(leftTableDatas);
		columns.add(reportColumnInfoDTO);

		// code
		reportColumnInfoDTO = new ReportColumnInfoDTO();
		reportColumnInfoDTO.setName(resourceBundleService.getText("code", null));
		reportColumnInfoDTO.setCode("code");
		reportColumnInfoDTO.setColumnType(modelDTO.getNumericCode() ? ColumnType.Long : ColumnType.String);

		reportColumnFieldInfoDTO = new ReportColumnFieldInfoDTO();
		reportColumnFieldInfoDTO.setFieldType(modelDTO.getNumericCode() ? "Long" : "String");
		reportColumnFieldInfoDTO.setLeftJoinPath(leftPathPrefix);
		columnsArray = new ArrayList<String>(columnsList);
		columnsArray.add("code");
		reportColumnFieldInfoDTO.setColumnList(columnsArray);
		reportColumnFieldInfoDTO.setTableList(tables);
		reportColumnFieldInfoDTO.setFieldName(createFieldName(columnsArray));
		reportColumnInfoDTO.setColumnFieldInfoDTO(reportColumnFieldInfoDTO);
		reportColumnFieldInfoDTO.setLeftTableDatas(leftTableDatas);
		columns.add(reportColumnInfoDTO);

		// lock
		reportColumnInfoDTO = new ReportColumnInfoDTO();
		reportColumnInfoDTO.setName(resourceBundleService.getText("lock", null));
		reportColumnInfoDTO.setCode("lock");
		reportColumnInfoDTO.setColumnType(ColumnType.Boolean);

		reportColumnFieldInfoDTO = new ReportColumnFieldInfoDTO();
		reportColumnFieldInfoDTO.setFieldType("Boolean");
		reportColumnFieldInfoDTO.setLeftJoinPath(leftPathPrefix);
		columnsArray = new ArrayList<String>(columnsList);
		columnsArray.add("lock");
		reportColumnFieldInfoDTO.setColumnList(columnsArray);
		reportColumnFieldInfoDTO.setTableList(tables);
		reportColumnFieldInfoDTO.setFieldName(createFieldName(columnsArray));
		reportColumnInfoDTO.setColumnFieldInfoDTO(reportColumnFieldInfoDTO);
		reportColumnFieldInfoDTO.setLeftTableDatas(leftTableDatas);
		columns.add(reportColumnInfoDTO);

		return columns;
	}

	private String createFieldName(List<String> columns) {
		String title = "";

		for (String column : columns) {
			String firstLetter = column.substring(0, 1).toUpperCase();
			String sufix = column.substring(1, column.length()).toLowerCase();
			title += firstLetter + sufix;
		}

		String firstLetter = title.substring(0, 1).toLowerCase();
		String sufix = title.substring(1, title.length());

		return firstLetter + sufix;
	}

	private List<ReportColumnInfoDTO> getOtherColumnsDTO(Long modelId, String leftPathPrefix, List<String> columnsList,
			List<String> listTables, List<String> childTables, List<String> parentTables, String parentTable) {
		List<ReportColumnInfoDTO> columns = new ArrayList<>();
		List<ModelColumnDTO> modelColumns = findColumn(modelId);

		List<LeftTableData> leftTableDatas = new ArrayList<>();

		String path = "";
		for (int i = 0; i < columnsList.size(); i++) {
			if (parentTable != null || childTables.contains(columnsList.get(i))) {
				LeftTableData leftTableData = new LeftTableData();
				leftTableData.setFieldColumn("id");
				if (parentTable != null) {
					leftTableData.setIdColumn(parentTable);
				} else {
					leftTableData.setIdColumn(parentTables.get(columnsList.indexOf(columnsList.get(i))));
				}
				String pathItem = new String(path);
				pathItem += "/" + columnsList.get(i);
				leftTableData.setPath(new String(pathItem));
				leftTableData.setTable(listTables.get(i));
				leftTableDatas.add(leftTableData);
				path = pathItem;
			} else {
				LeftTableData leftTableData = new LeftTableData();
				leftTableData.setFieldColumn(columnsList.get(i));
				leftTableData.setIdColumn("id");
				String pathItem = new String(path);
				pathItem += "/" + columnsList.get(i);
				leftTableData.setPath(new String(pathItem));
				leftTableData.setTable(listTables.get(i));
				leftTableDatas.add(leftTableData);
				path = pathItem;
			}

		}

		for (ModelColumnDTO modelColumnDTO : modelColumns) {
			ReportColumnInfoDTO reportColumnInfoDTO = new ReportColumnInfoDTO();
			ReportColumnFieldInfoDTO reportColumnFieldInfoDTO = new ReportColumnFieldInfoDTO();

			reportColumnInfoDTO.setName(resourceBundleService.getText(modelColumnDTO.getName(), null));
			ModelColumnType modelColumnType = ModelColumnType.valueOf(modelColumnDTO.getColumnType());
			String type = modelColumnType.type;
			reportColumnFieldInfoDTO.setFieldType(type);
			reportColumnInfoDTO.setCode(modelColumnDTO.getCode());
			reportColumnInfoDTO.setColumnType(
					modelColumnType.equals(ModelColumnType.CODEBOOK) ? null : ColumnType.valueOf(modelColumnType.type));
			reportColumnInfoDTO.setModelColumnId(modelColumnDTO.getId());
			reportColumnFieldInfoDTO.setLeftJoinPath(new String(leftPathPrefix));
			List<String> columnsArray = new ArrayList<String>(columnsList);
			columnsArray.add(modelColumnDTO.getCode());
			reportColumnFieldInfoDTO.setColumnList(columnsArray);

			List<String> tableArray = new ArrayList<String>(listTables);
			tableArray.add(modelColumnDTO.getCodebookModelCode());
			reportColumnFieldInfoDTO.setTableList(listTables);

			reportColumnFieldInfoDTO.setFieldName(createFieldName(columnsArray));
			reportColumnInfoDTO.setColumnFieldInfoDTO(reportColumnFieldInfoDTO);

			reportColumnFieldInfoDTO.setLeftTableDatas(leftTableDatas);

			if (modelColumnType.equals(ModelColumnType.CODEBOOK)) {
				reportColumnInfoDTO.setIcon(findModel(modelColumnDTO.getCodebookModelId()).getIcon());
				List<ReportColumnInfoDTO> children = new ArrayList<>();
				String prefix = new String(leftPathPrefix);
				prefix += "/" + modelColumnDTO.getCode();
				columnsArray = new ArrayList<String>(columnsList);
				columnsArray.add(modelColumnDTO.getCode());

				tableArray = new ArrayList<String>(listTables);
				tableArray.add(modelColumnDTO.getCodebookModelCode());

				children.addAll(getDefaultcolumns(modelColumnDTO.getCodebookModelId(), prefix, columnsArray, tableArray,
						childTables, parentTables, null));
				children.addAll(getOtherColumnsDTO(modelColumnDTO.getCodebookModelId(), prefix, columnsArray,
						tableArray, childTables, parentTables, null));
				reportColumnInfoDTO.getChildren().addAll(children);
			}

			columns.add(reportColumnInfoDTO);
		}

		return columns;
	}

	private ModelDTO findModel(Long modelId) {
		return ModelData.listModelDTOs.stream().filter(a -> a.getId().doubleValue() == modelId.doubleValue())
				.findFirst().get();
	}

	private List<ModelDTO> findChildren(Long modelId) {
		return ModelData.listModelDTOs.stream().filter(a -> a.getParentId() != null)
				.filter(a -> a.getParentId().doubleValue() == modelId.doubleValue())
				.sorted(Comparator.comparing(ModelDTO::getName)).toList();
	}

	private List<ModelColumnDTO> findColumn(Long modelId) {
		return ModelData.listColumnDTOs
				.stream().filter(a -> a.getModelId().doubleValue() == modelId.doubleValue()).sorted(Comparator
						.comparing(ModelColumnDTO::getRowNumber).thenComparing(ModelColumnDTO::getColumnNumber))
				.toList();
	}

	@Override
	public ReportDTO getUpdate(ReportDTO reportDTO) {

		UpdateReport reportUpdate = new UpdateReport(httpServletRequest, reportDTO);
		ReportDTO report = this.datatableService.executeMethodWithReturn(reportUpdate);

		return report;
	}

	@Override
	public ReportDTO getInfo(Long id) {
		Report report = this.datatableService.findByExistingId(id, Report.class);
		ConvertReportToReportDTO convertReportToReportDTO = new ConvertReportToReportDTO(httpServletRequest, report);
		return this.datatableService.executeMethodWithReturn(convertReportToReportDTO);
	}

	@Override
	public void getDelete(Long id) {
		Report report = this.datatableService.findByExistingId(id, Report.class);
		this.datatableService.delete(report);
		ModelData.listReportDTOs = this.datatableService.findAll(new TableParameterDTO(), ReportDTO.class);
	}

	@Override
	public void getJasperFileRefresh() {
		List<ReportJasper> reportJaspers = this.datatableService.findAll(new TableParameterDTO(), ReportJasper.class);

		for (ReportJasper reportJasper : reportJaspers) {
			String fileName = reportJasper.getName();

			String destination = AppParameters.jasperreportpath + "/" + fileName;

			File destinationFiles = new File(AppParameters.jasperreportpath);
			if (!(destinationFiles.exists() && destinationFiles.isDirectory())) {
				destinationFiles.mkdirs();
			}

			try {
				Files.copy(new ByteArrayInputStream(reportJasper.getBytes()), Paths.get(destination),
						StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				throw new WebApplicationException(e);
			}
		}

	}

	@Override
	public TableDataDTO<ReportJobDTO> getTableJobs(TableParameterDTO tableParameterDTO, Long reportId) {
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("reportId", SearchOperation.equals, String.valueOf(reportId), null));
		return this.datatableService.getTableDataDTO(tableParameterDTO, ReportJobDTO.class);
	}

	@Override
	public List<ComboboxDTO> getSmtpBox() {
		List<SmtpServerDTO> serverDTOs = this.datatableService.findAll(new TableParameterDTO(), SmtpServerDTO.class);
		return serverDTOs.stream().map(a -> new ComboboxDTO(a.getId(), a.getName())).toList();
	}

	@Override
	public ReportJobDTO getJobUpdate(ReportJobDTO reportJobDTO) {
		UpdateReportJob updateReportJob = new UpdateReportJob(this.httpServletRequest, reportJobDTO);

		return datatableService.executeMethodWithReturn(updateReportJob);
	}

	@Override
	public void getJobDelete(Long id) {
		DeleteReportJob deleteReportJob = new DeleteReportJob(this.httpServletRequest, id);
		this.datatableService.executeMethod(deleteReportJob);

	}

}
