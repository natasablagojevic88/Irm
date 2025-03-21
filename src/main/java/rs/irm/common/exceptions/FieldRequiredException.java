package rs.irm.common.exceptions;

import jakarta.ws.rs.WebApplicationException;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FieldRequiredException extends WebApplicationException{

	private static final long serialVersionUID = 1L;
	private String field;

	public FieldRequiredException(String field) {
		super("fieldRequired");
		this.field = field;
	}
	
	
}
