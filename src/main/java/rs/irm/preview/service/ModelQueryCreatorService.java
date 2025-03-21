package rs.irm.preview.service;

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.List;

import rs.irm.common.dto.ComboboxDTO;
import rs.irm.common.enums.TrackAction;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.preview.dto.SubCodebookInfoDTO;

public interface ModelQueryCreatorService {

	List<LinkedHashMap<String, Object>> findAll(TableParameterDTO tableParameterDTO, Long modelId,
			Connection connection);

	LinkedHashMap<String, Object> findByExistingId(Long id, Long modelId, Connection connection);

	LinkedHashMap<String, Object> getDefaultValues(Long modelId, Long parentId, Connection connection);

	TableDataDTO<LinkedHashMap<String, Object>> createTableDataDTO(TableParameterDTO tableParameterDTO, Long modelId,
			Long parentId, Connection connection);

	LinkedHashMap<String, List<ComboboxDTO>> getCodebooks(Long modelId, Connection connection);

	SubCodebookInfoDTO getSubCodebooks(Long modelId, String codebook, Long codebookValue, Connection connection);

	Long getInsert(Long modelId, LinkedHashMap<String, Object> item, Long parentId, Connection connection);

	void getUpdate(Long modelId, LinkedHashMap<String, Object> item, TrackAction trackAction, Connection connection);

	void insertTrack(Long modelId, Long id, TrackAction trackAction, Connection connection);

	void getDelete(Long modelId, Long id, Connection connection);

	void getLock(Long modelId, Long id, Connection connection);

	void getUnlock(Long modelId, Long id, Connection connection);

	Boolean checkLockParent(Long modelId, Long id, Long parentId, Connection connection);

	List<LinkedHashMap<String, Object>> findAllExcelCodebook(Long modelId, Connection connection);

	void checkTotal(TableDataDTO<LinkedHashMap<String, Object>> tableDataDTO, TableParameterDTO tableParameterDTO,
			Long modelId, Connection connection);
	
	LinkedHashMap<String, List<ComboboxDTO>> createListOfValue(Long modelId,Connection connection);
}
