package rs.irm.administration.utils;

import org.codehaus.janino.SimpleCompiler;

public class ExecuteClass {

	private Long id;
	private String javaCodeClass;
	private String className;
	private String classMethod;

	public ExecuteClass(Long id, String javaCodeClass, String className, String classMethod) {
		this.id = id;
		this.javaCodeClass = javaCodeClass;
		this.className = className;
		this.classMethod = classMethod;
	}

	public ExecuteClass(String javaCodeClass, String className, String classMethod) {
		this.javaCodeClass = javaCodeClass;
		this.className = className;
		this.classMethod = classMethod;
	}

	public void execute() throws Exception {

		if(this.id!=null) {
			this.javaCodeClass=this.javaCodeClass.replace("{id}", this.id+"L");
		}
		
		SimpleCompiler compiler = new SimpleCompiler();
        compiler.cook(this.javaCodeClass);

        Class<?> clazz = compiler.getClassLoader().loadClass(this.className);
        Object obj = clazz.getDeclaredConstructor().newInstance(); 
        clazz.getMethod(this.classMethod).invoke(obj);
	}
}
