package lox;

import java.util.HashMap; 
import java.util.Map ; 

/* this is our memory representation . 
 * variables , functions and classes that are declared in the same scope are stored in a environment (enclosing is the environment of the enclosing scope).   
 */
public class Environment {
	private final Map<String,Object> values = new HashMap<>(); 
	final Environment enclosing; 
	
	Environment(){
		enclosing = null; 
	}
	
	Environment(Environment env){
		enclosing = env; 
	}
	void define(String name,Object value) {
		values.put(name,value); 
	}
	
	Environment ancestor(int dist) {
		Environment env = this; 
		for(int i = 0; i< dist ; i++) {
			env = env.enclosing; 
		}
		return env; 
	}
	
	Object get(Token name) {
		if(values.containsKey(name.lexeme)) {
			return values.get(name.lexeme); 
		}
		if (enclosing != null ) return enclosing.get(name); 
		throw new RuntimeError(name, "Undefined varaible '" + name.lexeme + "'." ); 
	}
	
	Object getAt(int distance,String name) {
		return ancestor(distance).values.get(name); 
	}
	
	void assign(Token name,Object value) {
		if(values.containsKey(name.lexeme)) {
			values.put(name.lexeme,value); 
			return; 
		}
		if(enclosing != null) {
			enclosing.assign(name, value);
			return; 
		}
		throw new RuntimeError(name, "Undefined varaible '" + name.lexeme + "'." ); 
	}
	
	void assignAt(Token name,Object value, int dist) {
		ancestor(dist).values.put(name.lexeme,value); 
	}
}
