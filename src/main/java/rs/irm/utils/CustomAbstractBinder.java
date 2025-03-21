package rs.irm.utils;

import java.util.Arrays;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import jakarta.inject.Named;
import jakarta.inject.Singleton;
import rs.irm.Irm;

public class CustomAbstractBinder extends AbstractBinder{
	Logger logger=LogManager.getLogger(CustomAbstractBinder.class);

	@Override
	protected void configure() {
		Reflections reflections=new Reflections(Irm.packagePath, Scanners.SubTypes.filterResultsBy(a->true));
		
		Set<Class<? extends Object>> listClasses= reflections.getSubTypesOf(Object.class);
		
		for(Class<? extends Object> inClass:listClasses) {
			if(!inClass.isAnnotationPresent(Named.class)) {
				continue;
			}
			
			try {
				Class<?> namedClass=Class.forName(inClass.getCanonicalName());
				for(Class<?> interfaceClass:Arrays.asList(namedClass.getInterfaces())) {
					bind(namedClass).to(interfaceClass).in(Singleton.class);
				}
			} catch (ClassNotFoundException e) {
				logger.error(e.getMessage(),e);
			}
		}
	}

}
