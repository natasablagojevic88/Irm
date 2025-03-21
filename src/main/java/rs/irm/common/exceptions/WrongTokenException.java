package rs.irm.common.exceptions;

import jakarta.ws.rs.WebApplicationException;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WrongTokenException extends WebApplicationException{

	private static final long serialVersionUID = 1L;

	private String token;
	public WrongTokenException(String token) {
		super("wrongtoken");
		this.token = token;
	}
	
	

}
