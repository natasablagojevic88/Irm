package rs.irm.common.service;

import rs.irm.common.dto.TrackDTO;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

public interface TrackService {

	TableDataDTO<TrackDTO> getTrackData(TableParameterDTO tableParameterDTO, String table, Long dataid);
}
