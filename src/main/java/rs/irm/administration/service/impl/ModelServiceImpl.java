package rs.irm.administration.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import rs.irm.administration.dto.CheckParentDTO;
import rs.irm.administration.dto.ModelColumnDTO;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.administration.dto.ModelJasperReportDTO;
import rs.irm.administration.dto.ModelProcedureDTO;
import rs.irm.administration.dto.ModelTriggerDTO;
import rs.irm.administration.dto.NextRowColumnDTO;
import rs.irm.administration.dto.ReportJobDTO;
import rs.irm.administration.dto.RoleDTO;
import rs.irm.administration.entity.Model;
import rs.irm.administration.entity.ModelColumn;
import rs.irm.administration.entity.ModelJasperReport;
import rs.irm.administration.enums.ModelType;
import rs.irm.administration.service.ModelService;
import rs.irm.administration.utils.CreateModel;
import rs.irm.administration.utils.CreateModelColumn;
import rs.irm.administration.utils.DeleteModel;
import rs.irm.administration.utils.DeleteModelColumn;
import rs.irm.administration.utils.DeleteTrigger;
import rs.irm.administration.utils.FindColumns;
import rs.irm.administration.utils.FindFunctions;
import rs.irm.administration.utils.FindProcedures;
import rs.irm.administration.utils.ModelJasperReportDelete;
import rs.irm.administration.utils.ModelJasperReportUpdate;
import rs.irm.administration.utils.ModelProcedureDelete;
import rs.irm.administration.utils.ModelProcedureUpdate;
import rs.irm.administration.utils.RefreshModel;
import rs.irm.administration.utils.UpdateTrigger;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.enums.SortDirection;
import rs.irm.database.service.DatatableService;
import rs.irm.database.utils.TableFilter;
import rs.irm.database.utils.TableSort;
import rs.irm.utils.AppParameters;

@Named
public class ModelServiceImpl implements ModelService {

	@Inject
	private ResourceBundleService resourceBundleService;

	@Inject
	private DatatableService datatableService;

	@Override
	public ModelDTO getTree() {
		ModelDTO modelDTO = new ModelDTO();
		modelDTO.setId(Long.valueOf(-1));
		modelDTO.setCode("root");
		modelDTO.setName(resourceBundleService.getText("model", null));
		modelDTO.setIcon("fa fa-database");
		modelDTO.setType(ModelType.MENU.name());

		List<ModelDTO> modelDTOs = this.datatableService.findAll(new TableParameterDTO(), ModelDTO.class);

		List<ModelDTO> withoutParent = modelDTOs.stream().filter(a -> a.getParentId() == null)
				.sorted(Comparator.comparing(ModelDTO::getName)).toList();

		for (ModelDTO modelDTOSub : withoutParent) {
			modelDTOSub.setParentId(Long.valueOf(-1));
			modelDTO.getChildren().add(modelDTOSub);

			List<ModelDTO> findChilred = modelDTOs.stream().filter(a -> a.getParentId() != null)
					.filter(a -> a.getParentId() == modelDTOSub.getId()).sorted(Comparator.comparing(ModelDTO::getName))
					.toList();

			for (ModelDTO child : findChilred) {
				addChildren(modelDTOs, modelDTOSub, child);
			}
		}

		return modelDTO;
	}

	private void addChildren(List<ModelDTO> list, ModelDTO parent, ModelDTO child) {

		parent.getChildren().add(child);
		List<ModelDTO> findChilred = list.stream().filter(a -> a.getParentId() != null)
				.filter(a -> a.getParentId() == child.getId()).sorted(Comparator.comparing(ModelDTO::getName)).toList();

		for (ModelDTO modelDTO : findChilred) {
			addChildren(list, child, modelDTO);
		}
	}

	@Override
	public List<ComboboxDTO> getRoles() {
		List<RoleDTO> roles = this.datatableService.findAll(new TableParameterDTO(), RoleDTO.class);

		return roles.stream().map(a -> new ComboboxDTO(a.getId(), a.getCode())).toList();
	}

	@Context
	private HttpServletRequest request;

	@Override
	public ModelDTO getUpdate(ModelDTO modelDTO) {

		CreateModel updateModel = new CreateModel(request, modelDTO);

		return this.datatableService.executeMethodWithReturn(updateModel);
	}

	@Override
	public void getDelete(Long id) {
		DeleteModel deleteModel = new DeleteModel(request, id);
		datatableService.executeMethod(deleteModel);

	}

	@Override
	public TableDataDTO<ModelColumnDTO> getColumns(TableParameterDTO tableParameterDTO, Long modelId) {

		TableFilter tableFilter = new TableFilter();
		tableFilter.setField("modelId");
		tableFilter.setParameter1(String.valueOf(modelId));
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableParameterDTO.getTableFilters().add(tableFilter);

		return this.datatableService.getTableDataDTO(tableParameterDTO, ModelColumnDTO.class);
	}

	@Override
	public List<ComboboxDTO> getCodebookList() {
		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		TableFilter tableFilter = new TableFilter();
		tableFilter.setField("type");
		tableFilter.setParameter1(ModelType.TABLE.name());
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableParameterDTO.getTableFilters().add(tableFilter);
		List<Model> models = this.datatableService.findAll(tableParameterDTO, Model.class);

		List<ComboboxDTO> list = models.stream().map(a -> new ComboboxDTO(a.getId(), a.getName()))
				.sorted(Comparator.comparing(ComboboxDTO::getOption)).toList();
		return list;
	}

	@Override
	public TableDataDTO<ModelDTO> getCodebookTable(TableParameterDTO tableParameterDTO) {
		TableFilter tableFilter = new TableFilter();
		tableFilter.setField("type");
		tableFilter.setParameter1(ModelType.TABLE.name());
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableParameterDTO.getTableFilters().add(tableFilter);

		return this.datatableService.getTableDataDTO(tableParameterDTO, ModelDTO.class);
	}

	@Override
	public NextRowColumnDTO getNextRowColumn(Long modelId) {
		NextRowColumnDTO nextRowColumnDTO = new NextRowColumnDTO();

		Model model = this.datatableService.findByExistingId(modelId, Model.class);

		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.setPageNumber(0);
		tableParameterDTO.setPageSize(1);

		TableFilter tableFilter = new TableFilter();
		tableFilter.setField("model");
		tableFilter.setParameter1(String.valueOf(modelId));
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableParameterDTO.getTableFilters().add(tableFilter);

		TableSort tableSort = new TableSort();
		tableSort.setField("rowNumber");
		tableSort.setSortDirection(SortDirection.DESC);
		tableParameterDTO.getTableSorts().add(tableSort);

		tableSort = new TableSort();
		tableSort.setField("columnNumber");
		tableSort.setSortDirection(SortDirection.DESC);
		tableParameterDTO.getTableSorts().add(tableSort);

		List<ModelColumn> columns = this.datatableService.findAll(tableParameterDTO, ModelColumn.class);

		if (columns.isEmpty()) {
			nextRowColumnDTO.setRow(1);
			nextRowColumnDTO.setColumn(1);
		} else {
			ModelColumn lastColumn = columns.get(0);

			int columnNumber = lastColumn.getColumnNumber() + lastColumn.getColspan();
			if (columnNumber > model.getColumnsNumber()) {
				nextRowColumnDTO.setRow(lastColumn.getRowNumber() + 1);
				nextRowColumnDTO.setColumn(1);
			} else {
				nextRowColumnDTO.setRow(lastColumn.getRowNumber());
				nextRowColumnDTO.setColumn(columnNumber);
			}
		}

		return nextRowColumnDTO;
	}

	@Override
	public CheckParentDTO getCheckParent(Long columnId, Long modelId, Long codebookId) {
		CheckParentDTO checkParentDTO = new CheckParentDTO();
		Model model = this.datatableService.findByExistingId(codebookId, Model.class);
		checkParentDTO
				.setNeedParent(model.getParent() == null ? false : model.getParent().getType().equals(ModelType.TABLE));

		if (checkParentDTO.getNeedParent()) {
			TableParameterDTO tableParameterDTO = new TableParameterDTO();

			TableFilter tableFilter = new TableFilter();
			tableFilter.setField("model");
			tableFilter.setParameter1(String.valueOf(modelId));
			tableFilter.setSearchOperation(SearchOperation.equals);
			tableParameterDTO.getTableFilters().add(tableFilter);

			tableFilter = new TableFilter();
			tableFilter.setField("codebookModel");
			tableFilter.setSearchOperation(SearchOperation.isnotnull);
			tableParameterDTO.getTableFilters().add(tableFilter);

			List<ModelColumn> columnModelColumns = this.datatableService.findAll(tableParameterDTO, ModelColumn.class);
			Long parentId = model.getParent().getId();
			for (ModelColumn modelColumn : columnModelColumns) {
				if (modelColumn.getCodebookModel().getId() != parentId) {
					continue;
				}
				Set<Long> usedAsParent = columnModelColumns.stream().filter(a -> a.getParent() != null)
						.map(a -> a.getParent().getId()).collect(Collectors.toSet());

				if (usedAsParent.contains(modelColumn.getId()) && columnId == 0) {
					continue;
				}

				checkParentDTO.getList().add(new ComboboxDTO(modelColumn.getId(), modelColumn.getName()));
			}

		}

		return checkParentDTO;
	}

	@Override
	public ModelColumnDTO getUpdateColumn(ModelColumnDTO modelColumnDTO) {

		CreateModelColumn createModelColumn = new CreateModelColumn(request, modelColumnDTO);
		return datatableService.executeMethodWithReturn(createModelColumn);
	}

	@Override
	public void getColumnDelete(Long id) {
		DeleteModelColumn deleteModelColumn = new DeleteModelColumn(request, id);
		this.datatableService.executeMethod(deleteModelColumn);

	}

	@Override
	public TableDataDTO<ModelTriggerDTO> getTriggerTable(TableParameterDTO tableParameterDTO, Long modelId) {
		TableFilter tableFilter = new TableFilter();
		tableFilter.setField("modelId");
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableFilter.setParameter1(String.valueOf(modelId));
		tableParameterDTO.getTableFilters().add(tableFilter);

		return this.datatableService.getTableDataDTO(tableParameterDTO, ModelTriggerDTO.class);
	}

	@Override
	public ModelTriggerDTO getTriggerUpdate(ModelTriggerDTO modelTriggerDTO) {

		UpdateTrigger updateTrigger = new UpdateTrigger(modelTriggerDTO, request);
		return this.datatableService.executeMethodWithReturn(updateTrigger);
	}

	@Override
	public List<ComboboxDTO> getAllColumnsForModel(Long modelId) {
		FindColumns findColumns = new FindColumns(request, modelId);
		return this.datatableService.executeMethodWithReturn(findColumns);
	}

	@Override
	public List<ComboboxDTO> getAllTriggerFunctions() {
		FindFunctions findFunctions = new FindFunctions("trigger");
		return this.datatableService.executeMethodWithReturn(findFunctions);
	}

	@Override
	public void getTriggerDelete(Long id) {
		DeleteTrigger deleteTrigger = new DeleteTrigger(id, request);
		this.datatableService.executeMethod(deleteTrigger);

	}

	@Override
	public void refreshModel() {
		RefreshModel refreshModel = new RefreshModel(request);
		this.datatableService.executeMethod(refreshModel);
	}

	@Override
	public TableDataDTO<ModelJasperReportDTO> getJasperListTable(TableParameterDTO tableParameterDTO, Long modelId) {

		TableFilter tableFilter = new TableFilter("modelId", SearchOperation.equals, String.valueOf(modelId), null);
		tableParameterDTO.getTableFilters().add(tableFilter);

		return this.datatableService.getTableDataDTO(tableParameterDTO, ModelJasperReportDTO.class);
	}

	@Override
	public ModelJasperReportDTO getJasperUpdate(ModelJasperReportDTO modelJasperReportDTO) {

		ModelJasperReportUpdate modelJasperReportUpdate = new ModelJasperReportUpdate(request, modelJasperReportDTO);

		return this.datatableService.executeMethodWithReturn(modelJasperReportUpdate);
	}

	@Override
	public void getJasperDelete(Long id) {
		ModelJasperReportDelete modelJasperReportDelete = new ModelJasperReportDelete(id, request);
		this.datatableService.executeMethod(modelJasperReportDelete);

	}

	@Override
	public void refreshJasperFiles() {
		List<ModelJasperReport> jasperReports = this.datatableService.findAll(new TableParameterDTO(),
				ModelJasperReport.class);

		for (ModelJasperReport jasperReport : jasperReports) {
			byte[] bytes = jasperReport.getBytes();

			String jasperFolderPath = AppParameters.jasperfilepath;

			File jasperFolderFolder = new File(jasperFolderPath);
			if (!(jasperFolderFolder.exists() && jasperFolderFolder.isDirectory())) {
				jasperFolderFolder.mkdirs();
			}

			Path jasperPath = Paths.get(jasperFolderPath + "/" + jasperReport.getJasperFileName());

			try {
				Files.copy(new ByteArrayInputStream(bytes), jasperPath, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				throw new WebApplicationException(e);
			}
		}

	}

	@Override
	public List<ComboboxDTO> getAllJsonFunctions() {
		FindFunctions findFunctions = new FindFunctions("json");
		return this.datatableService.executeMethodWithReturn(findFunctions);
	}

	@Override
	public TableDataDTO<ReportJobDTO> getJobs(TableParameterDTO tableParameterDTO, Long modelID) {

		tableParameterDTO.getTableFilters()
				.add(new TableFilter("modelId", SearchOperation.equals, String.valueOf(modelID), null));
		TableDataDTO<ReportJobDTO> tableDataDTO = this.datatableService.getTableDataDTO(tableParameterDTO,
				ReportJobDTO.class);
		tableDataDTO.getNames().put("fileName", resourceBundleService.getText("fileNameStartWith", null));
		return tableDataDTO;
	}

	@Override
	public TableDataDTO<ModelProcedureDTO> getProceduresTable(TableParameterDTO tableParameterDTO, Long modelId) {
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("modelId", SearchOperation.equals, String.valueOf(modelId), null));
		return this.datatableService.getTableDataDTO(tableParameterDTO, ModelProcedureDTO.class);
	}

	@Override
	public List<ComboboxDTO> getProcedures() {
		FindProcedures findProcedures = new FindProcedures();
		return this.datatableService.executeMethodWithReturn(findProcedures);
	}

	@Override
	public ModelProcedureDTO getUpdateProcedure(ModelProcedureDTO modelProcedureDTO) {
		ModelProcedureUpdate modelProcedureUpdate=new ModelProcedureUpdate(modelProcedureDTO, request);
		return this.datatableService.executeMethodWithReturn(modelProcedureUpdate);
	}

	@Override
	public void getProcedureDelete(Long id) {
		ModelProcedureDelete modelProcedureDelete=new ModelProcedureDelete(id, this.request);
		this.datatableService.executeMethod(modelProcedureDelete);
	}

}
