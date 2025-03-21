package rs.irm.common.exceptions;

import jakarta.ws.rs.WebApplicationException;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MaximumException extends WebApplicationException{

	private static final long serialVersionUID = 1L;
	
	private String field;
	private Long minimum;
	
	public MaximumException(String field, Long minimum) {
		super("maximumValue");
		this.field = field;
		this.minimum = minimum;
	}
}
