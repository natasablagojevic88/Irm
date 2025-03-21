package rs.irm.common.exceptions;

import jakarta.ws.rs.WebApplicationException;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CommonException extends WebApplicationException{

	private static final long serialVersionUID = 1L;
	private Integer status;
	private String inMessage;
	private Object inObject;
	
	public CommonException(Integer status, String inMessage, Object inObject) {
		super(inMessage);
		this.status = status;
		this.inMessage = inMessage;
		this.inObject = inObject;
	}
	
	
}
