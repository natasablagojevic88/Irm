package rs.irm.preview.utils;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.ModelColumnDTO;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.administration.enums.ModelColumnType;
import rs.irm.administration.enums.ModelType;
import rs.irm.administration.utils.ModelData;
import rs.irm.common.dto.ComboboxDTO;
import rs.irm.common.enums.TrackAction;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.exceptions.FieldRequiredException;
import rs.irm.database.utils.ExecuteMethodWithReturn;
import rs.irm.preview.service.ModelQueryCreatorService;
import rs.irm.preview.service.impl.ModelQueryCreatorServiceImpl;

public class UpdateItem implements ExecuteMethodWithReturn<LinkedHashMap<String, Object>> {

	private HttpServletRequest httpServletRequest;
	private Long modelID;
	private Long parentId;
	private LinkedHashMap<String, Object> item;
	private Boolean checkItem;
	private ModelQueryCreatorService modelQueryCreatorService;

	public UpdateItem(HttpServletRequest httpServletRequest, Long modelID, Long parentId,
			LinkedHashMap<String, Object> item, Boolean checkItem) {
		this.httpServletRequest = httpServletRequest;
		this.modelID = modelID;
		this.parentId = parentId;
		this.item = item;
		this.checkItem = checkItem;
		this.modelQueryCreatorService = new ModelQueryCreatorServiceImpl(this.httpServletRequest);
	}

	@Override
	public LinkedHashMap<String, Object> execute(Connection connection) {
		List<ModelColumnDTO> columns = findColumn();
		String parent = findParent(this.modelID);
		checkNull(columns, parent);
		if (this.checkItem) {
			checkCodebooks(columns, connection);
			if (modelQueryCreatorService.checkLockParent(modelID, 0L, parentId, connection)) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "parentIsLocked", null);
			}

			checkListOfValues(connection);

		}

		boolean insert = this.item.get("id").toString().equals("0");
		Long id = Long.valueOf(this.item.get("id").toString());
		if (insert) {

			id = modelQueryCreatorService.getInsert(this.modelID, item, this.parentId, connection);

		} else {
			LinkedHashMap<String, Object> existing = modelQueryCreatorService.findByExistingId(id, modelID, connection);

			if ((Boolean) existing.get("lock")) {
				throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "itemIsLocked", null);
			}

			modelQueryCreatorService.getUpdate(this.modelID, this.item, TrackAction.UPDATE, connection);

		}

		return modelQueryCreatorService.findByExistingId(id, modelID, connection);
	}

	private ModelDTO findModelDTO(Long modelId) {
		return ModelData.listModelDTOs.stream().filter(a -> a.getId().doubleValue() == modelId.doubleValue()).findFirst().get();
	}

	private List<ModelColumnDTO> findColumn() {
		return ModelData.listColumnDTOs.stream().filter(a -> a.getModelId() == this.modelID)
				.sorted(Comparator.comparing(ModelColumnDTO::getRowNumber)
						.thenComparing(Comparator.comparing(ModelColumnDTO::getColumnNumber)))
				.toList();
	}

	private String findParent(Long modelId) {
		ModelDTO modelDTO = findModelDTO(modelId);
		if (modelDTO.getParentId() != null && modelDTO.getParentType().equals(ModelType.TABLE.name())) {
			return modelDTO.getParentCode();
		}

		return null;
	}

	private void checkNull(List<ModelColumnDTO> columns, String parent) {

		if (item.get("id") == null) {
			throw new FieldRequiredException("id");
		}

		if (item.get("id").toString().length() == 0) {
			throw new FieldRequiredException("id");
		}

		if (item.get("code") == null) {
			throw new FieldRequiredException("code");
		}

		if (item.get("code").toString().length() == 0) {
			throw new FieldRequiredException("code");
		}

		if (item.get("lock") == null) {
			throw new FieldRequiredException("lock");
		}

		if (item.get("lock").toString().length() == 0) {
			throw new FieldRequiredException("lock");
		}

		if (parent != null) {
			if (this.parentId == null) {
				throw new FieldRequiredException(parent);
			}

			if (this.parentId == -1) {
				throw new FieldRequiredException(parent);
			}
		}

		List<ModelColumnDTO> notNullColumns = columns.stream().filter(a -> a.getNullable() == false).toList();

		for (ModelColumnDTO columnDTO : notNullColumns) {
			
			if(columnDTO.getDisabled()) {
				continue;
			}
			
			if (item.get(columnDTO.getCode()) == null) {
				throw new FieldRequiredException(columnDTO.getName());
			}

			if (item.get(columnDTO.getCode()).toString().length() == 0) {
				throw new FieldRequiredException(columnDTO.getName());
			}
		}

	}

	private void checkCodebooks(List<ModelColumnDTO> columns, Connection connection) {

		List<ModelColumnDTO> codebooksColumnDTOs = columns.stream()
				.filter(a -> a.getColumnType().equals(ModelColumnType.CODEBOOK.name())).toList();

		for (ModelColumnDTO modelColumnDTO : codebooksColumnDTOs) {
			if (this.item.get(modelColumnDTO.getCode()) == null) {
				continue;
			}

			if (this.item.get(modelColumnDTO.getCode()).toString().length() == 0) {
				continue;
			}

			ModelDTO modelDTO = findModelDTO(modelColumnDTO.getCodebookModelId());
			String parentTable = findParent(modelDTO.getId());

			if (parentTable == null) {
				continue;
			}

			String codebookId = this.item.get(modelColumnDTO.getCode()).toString();
			if (this.item.get(modelColumnDTO.getParentCode()) == null) {
				throw new FieldRequiredException(modelColumnDTO.getParentCode());
			}

			String parentCodebookValue = this.item.get(modelColumnDTO.getParentCode()).toString();

			if (parentCodebookValue.length() == 0) {
				throw new FieldRequiredException(modelColumnDTO.getParentCode());
			}

			String query = "select id from " + modelDTO.getCode() + " where id=" + codebookId;
			query += " and " + parentTable + "=" + parentCodebookValue;
			try {
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(query);

				if (!resultSet.next()) {
					throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "wrongParent", parentTable);
				}

				resultSet.close();
				statement.close();
			} catch (Exception e) {
				throw new WebApplicationException(e);
			}

		}
	}
	
	private void checkListOfValues(Connection connection) {

		LinkedHashMap<String, List<ComboboxDTO>> findListOfValue = this.modelQueryCreatorService
				.createListOfValue(modelID, connection);

		Iterator<String> itr = findListOfValue.keySet().iterator();
		while (itr.hasNext()) {
			String columnCode = itr.next();
			List<ComboboxDTO> listOfValues = findListOfValue.get(columnCode);
			if (item.get(columnCode) != null && item.get(columnCode).toString().length() > 0) {
				String value = item.get(columnCode).toString();

				if (listOfValues.stream().filter(a -> a.getValue().toString().equals(value)).toList().isEmpty()) {
					throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "valueNotOnListOfValue", columnCode);
				}
			}
		}
	}

}
