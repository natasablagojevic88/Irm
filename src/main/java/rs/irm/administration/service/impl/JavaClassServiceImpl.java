package rs.irm.administration.service.impl;

import java.net.HttpURLConnection;

import org.codehaus.janino.SimpleCompiler;
import org.modelmapper.ModelMapper;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import rs.irm.administration.dto.JavaClassDTO;
import rs.irm.administration.entity.JavaClass;
import rs.irm.administration.service.JavaClassService;
import rs.irm.common.exceptions.CommonException;
import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.service.DatatableService;

@Named
public class JavaClassServiceImpl implements JavaClassService {

	@Inject
	private DatatableService datatableService;

	private ModelMapper modelMapper = new ModelMapper();

	@Override
	public TableDataDTO<JavaClassDTO> getTable(TableParameterDTO tableParameterDTO) {

		return datatableService.getTableDataDTO(tableParameterDTO, JavaClassDTO.class);
	}

	@Override
	public JavaClassDTO getUpdate(JavaClassDTO javaClassDTO) {
		JavaClass javaClass = javaClassDTO.getId() == 0 ? new JavaClass()
				: this.datatableService.findByExistingId(javaClassDTO.getId(), JavaClass.class);
		try {
			SimpleCompiler sc = new SimpleCompiler();
            sc.cook(javaClassDTO.getClassText()); 
		}catch(Exception e) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "wrongCode", e.getMessage());
		}
		modelMapper.map(javaClassDTO, javaClass);
		javaClass=this.datatableService.save(javaClass);
		return modelMapper.map(javaClass, JavaClassDTO.class);
	}

	@Override
	public void getDelete(Long id) {
		JavaClass javaClass = this.datatableService.findByExistingId(id, JavaClass.class);
		this.datatableService.delete(javaClass);
		
	}

}
