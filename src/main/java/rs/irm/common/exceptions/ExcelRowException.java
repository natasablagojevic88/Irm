package rs.irm.common.exceptions;

import jakarta.ws.rs.WebApplicationException;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExcelRowException extends WebApplicationException{

	private static final long serialVersionUID = 1L;
	private Integer row;
	private String inMessage;
	
	public ExcelRowException(Integer row, String inMessage) {
		super(inMessage);
		this.row = row;
		this.inMessage = inMessage;
	}
	
	
	
}
