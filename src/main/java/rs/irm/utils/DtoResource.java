package rs.irm.utils;

import java.lang.reflect.Field;
import java.util.Arrays;

public class DtoResource {

	public static void main(String[] args) {

		Class<?> inClass = String.class;

		for (int i = 0; i < 2; i++) {
			System.out.println(inClass.getSimpleName() + ".title=");
			for (Field field : Arrays.asList(inClass.getDeclaredFields())) {
				field.setAccessible(true);
				System.out.println(inClass.getSimpleName() + "." + field.getName() + "=");
			}
			System.out.println();
		}

	}

}
