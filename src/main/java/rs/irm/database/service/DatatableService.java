package rs.irm.database.service;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import rs.irm.database.dto.TableDataDTO;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.utils.ExecuteMethod;
import rs.irm.database.utils.ExecuteMethodWithReturn;

public interface DatatableService {
	
	<C> TableDataDTO<C> getTableDataDTO(TableParameterDTO tableParameterDTO, Class<C> inClass);
	
	<C> TableDataDTO<C> getTableDataDTO(TableParameterDTO tableParameterDTO, Class<C> inClass, Connection connection);

	<C> List<C> findAll(TableParameterDTO tableParameterDTO, Class<C> inClass);

	<C> List<C> findAll(TableParameterDTO tableParameterDTO, Class<C> inClass, Connection connection);
	
	<C> Optional<C> findById(Long id, Class<C> inClass);
	
	<C> Optional<C> findById(Long id, Class<C> inClass, Connection connection);
	
	<C> C findByExistingId(Long id, Class<C> inClass);
	
	<C> C findByExistingId(Long id, Class<C> inClass, Connection connection);
	
	<C> C save(C entity);
	
	<C> C save(C entity, Connection connection);
	
	<C> void delete(C entity);
	
	<C> void delete(C entity, Connection connection);

	void executeMethod(ExecuteMethod executeMethod);
	
	void executeMethod(ExecuteMethod executeMethod,Connection connection);
	
	<C> C executeMethodWithReturn(ExecuteMethodWithReturn<C> executeMethodWithReturn);
	
	<C> C executeMethodWithReturn(ExecuteMethodWithReturn<C> executeMethodWithReturn,Connection connection);

}
