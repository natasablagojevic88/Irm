package rs.irm.common.exceptions;

import jakarta.ws.rs.WebApplicationException;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NoDataFoundException extends WebApplicationException{
	
	private static final long serialVersionUID = 1L;
	private Long id;
	private Class<?> inClass;
	
	public NoDataFoundException(Long id, Class<?> inClass) {
		super("nodatafound");
		this.id = id;
		this.inClass = inClass;
	}
	
	

}
