package lox;

import java.util.Map;
import java.util.HashMap; 

public class LoxInstance {
	private final LoxClass klass ; 
	protected final Map<String,Object> fields = new HashMap<>(); 
	
	LoxInstance(LoxClass klass){
		this.klass = klass; 
	}
	
	public Object get(Token name) {
		if(fields.containsKey(name.lexeme)) {
			return fields.get(name.lexeme); 
		}
		
		LoxFunction fun = klass.findMethod(this, name.lexeme); 
		if(fun != null) return fun; 
		throw new RuntimeError(name,"Undefined property '" + name.lexeme + "' ." ); 
	}
	
	public void set(Token name , Object value) {
		fields.put(name.lexeme, value); 
	}
	
	@Override
	public String toString() {
		return klass.name + " instance"; 
	}
}
