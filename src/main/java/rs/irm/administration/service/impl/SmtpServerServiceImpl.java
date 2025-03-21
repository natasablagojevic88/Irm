package rs.irm.administration.service.impl;

import org.modelmapper.ModelMapper;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import rs.irm.administration.dto.SmtpServerDTO;
import rs.irm.administration.dto.TestSmtpDTO;
import rs.irm.administration.entity.SmtpServer;
import rs.irm.administration.service.SmtpServerService;
import rs.irm.common.exceptions.FieldRequiredException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.common.service.SendMailService;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.service.DatatableService;

@Named
public class SmtpServerServiceImpl implements SmtpServerService {

	@Inject
	private DatatableService datatableService;
	
	@Inject
	private CommonService commonService;
	
	@Inject
	private SendMailService sendMail;
	
	@Inject
	private ResourceBundleService resourceBundleService;
	
	private ModelMapper modelMapper=new ModelMapper();

	@Override
	public TableDataDTO<SmtpServerDTO> getTable(TableParameterDTO tableParameterDTO) {
		TableDataDTO<SmtpServerDTO> tableDataDTO=datatableService.getTableDataDTO(tableParameterDTO, SmtpServerDTO.class);
		tableDataDTO.getList().forEach(c->{
			c.setPassword(null);
		});
		return tableDataDTO;
	}

	@Override
	public SmtpServerDTO getUpdate(SmtpServerDTO smtpServerDTO) {

		SmtpServer smtpServer = smtpServerDTO.getId() == 0 ? new SmtpServer()
				: this.datatableService.findByExistingId(smtpServerDTO.getId(), SmtpServer.class);
		
		if(smtpServerDTO.getAuthentication()) {
			if(!commonService.hasText(smtpServerDTO.getUsername())) {
				throw new FieldRequiredException("SmtpServerDTO.username");
			}
			if(!commonService.hasText(smtpServerDTO.getPassword())) {
				if(smtpServerDTO.getId() == 0) {
					throw new FieldRequiredException("SmtpServerDTO.password");
				}else {
					smtpServerDTO.setPassword(smtpServer.getPassword());
					if(!commonService.hasText(smtpServerDTO.getPassword())) {
						throw new FieldRequiredException("SmtpServerDTO.password");
					}
				}
			}
		}else {
			smtpServerDTO.setUsername(null);
			smtpServerDTO.setPassword(null);
		}
		
		modelMapper.map(smtpServerDTO, smtpServer);
		
		smtpServer=this.datatableService.save(smtpServer);
		smtpServerDTO=modelMapper.map(smtpServer, SmtpServerDTO.class);
		smtpServerDTO.setPassword(null);
		return smtpServerDTO;
	}

	@Override
	public void getDelete(Long id) {
		SmtpServer smtpServer=this.datatableService.findByExistingId(id, SmtpServer.class);
		
		this.datatableService.delete(smtpServer);
		
	}

	@Override
	public void getTestMail(TestSmtpDTO testSmtpDTO,Long id) {
		SmtpServer smtpServer=this.datatableService.findByExistingId(id, SmtpServer.class);
		this.sendMail.sendMail(smtpServer, testSmtpDTO.getToAddress(), "IRM TEST", resourceBundleService.getText("mailServerWorks", null),null);
	}

}
