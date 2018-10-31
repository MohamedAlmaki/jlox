package lox;

import java.util.List;
import java.util.Map; 


public class LoxClass extends LoxInstance implements LoxCallable  {
	final String name ; 
	final LoxClass superClass; 
	private final Map<String,LoxFunction> methods; 
	private final Map<String,LoxFunction> staticMethods; 
	
	
	LoxClass(String name,LoxClass superClass, Map<String,LoxFunction> methods,Map<String,LoxFunction> staticMethods){
		super(null); 
		this.name = name; 
		this.methods = methods; 
		this.staticMethods = staticMethods; 
		this.superClass = superClass; 
	}
	
	public LoxFunction findMethod(LoxInstance instance , String name) {
		if(methods.containsKey(name)) {
			return methods.get(name).bind(instance); 
		}
		if(superClass != null) {
			return superClass.findMethod(instance, name); 
		}
		return null;
	}
	
	@Override 
	public String toString() {
		return name; 
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		LoxInstance instance = new LoxInstance(this);
		LoxFunction init = methods.get("init");
		if(init != null) {
			init.bind(instance).call(interpreter, arguments);
		}
		return instance; 
	}

	@Override
	public int arity() {
		LoxFunction init = methods.get("init"); 
		if(init != null ) return init.arity(); 
		return 0;
	}
	

	public Object get(Token name) {
		if(fields.containsKey(name.lexeme)) {
			return fields.get(name.lexeme); 
		}
		if(staticMethods.containsKey(name.lexeme)) {
			return staticMethods.get(name.lexeme); 
		}
		if(superClass != null) {
			return superClass.get(name); 
		}
		throw new RuntimeError(name,"Undefined static property " + name.lexeme + " .");
	}
	
}
