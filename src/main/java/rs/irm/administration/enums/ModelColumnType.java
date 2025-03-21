package rs.irm.administration.enums;

import rs.irm.database.enums.BaseType;

public enum ModelColumnType {

	STRING(BaseType.varchar, "String"), 
	INTEGER(BaseType.int4, "Integer"), 
	LONG(BaseType.numeric, "Long"), 
	BIGDECIMAL(BaseType.numeric, "BigDecimal"),
	LOCALDATE(BaseType.date, "LocalDate"), 
	LOCALDATETIME(BaseType.timestamp, "LocalDateTime"),
	BOOLEAN(BaseType.bool, "Boolean"), 
	CODEBOOK(BaseType.int8, "Long");

	public BaseType baseType;
	public String type;

	private ModelColumnType(BaseType baseType, String type) {
		this.baseType = baseType;
		this.type = type;
	}

}
