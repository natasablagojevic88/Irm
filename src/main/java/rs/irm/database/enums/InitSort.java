package rs.irm.database.enums;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
public @interface InitSort {

	int value() default 1;
	
	SortDirection sortDirection() default SortDirection.ASC;
}
