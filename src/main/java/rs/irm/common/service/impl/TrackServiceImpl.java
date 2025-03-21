package rs.irm.common.service.impl;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import rs.irm.common.dto.TrackDTO;
import rs.irm.common.service.TrackService;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.utils.TableFilter;

@Named
public class TrackServiceImpl implements TrackService {
	
	@Inject
	private DatatableService datatableService;

	@Override
	public TableDataDTO<TrackDTO> getTrackData(TableParameterDTO tableParameterDTO, String table, Long dataid) {
		TableFilter tableFilter = new TableFilter();
		tableFilter.setField("tableName");
		tableFilter.setParameter1(table);
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableParameterDTO.getTableFilters().add(tableFilter);
		
		tableFilter = new TableFilter();
		tableFilter.setField("dataid");
		tableFilter.setParameter1(String.valueOf(dataid));
		tableFilter.setSearchOperation(SearchOperation.equals);
		tableParameterDTO.getTableFilters().add(tableFilter);

		return datatableService.getTableDataDTO(tableParameterDTO, TrackDTO.class);
	}

}
