package lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
	private final Stmt.Function function ; 
	private final Environment closure ; 
	private final Boolean isInit; 
		
	public LoxFunction(Stmt.Function function, Environment closure,Boolean isInit) {
		this.function = function; 
		this.closure = closure; 
		this.isInit = isInit;
	}
	
	public LoxFunction bind(LoxInstance instance) {
		Environment env = new Environment(closure); 
		env.define("this", instance);
		return new LoxFunction(function , env,false); 
	}
	
	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		Environment env = new Environment(closure);
		for(int i=0 ; i<function.parameters.size(); i++) {
			env.define(function.parameters.get(i).lexeme, arguments.get(i));
		}
		try {
	    interpreter.executeBlock(function.body, env);
		} catch(ReturnValue e) {
			if(isInit) return closure.getAt(0,"this"); 
			return e.value; 
		}
		if(isInit) return closure.getAt(0,"this");
		return null;
	}
	
	@Override
	public int arity() {
		// TODO Auto-generated method stub
		return function.parameters.size();
	}
	
	@Override 
	public String toString() {
		return "<fn " + function.name.lexeme + " >"; 
	}
	

}
