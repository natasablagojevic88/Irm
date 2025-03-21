package rs.irm.administration.service;

import rs.irm.administration.dto.SmtpServerDTO;
import rs.irm.administration.dto.TestSmtpDTO;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;

public interface SmtpServerService {

	TableDataDTO<SmtpServerDTO> getTable(TableParameterDTO tableParameterDTO);
	
	SmtpServerDTO getUpdate(SmtpServerDTO smtpServerDTO);
	
	void getDelete(Long id);
	
	void getTestMail(TestSmtpDTO testSmtpDTO,Long id);
}
