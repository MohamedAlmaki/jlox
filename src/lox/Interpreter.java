package lox;

import lox.Expr.Assign;
import lox.Expr.Binary;
import lox.Expr.Call;
import lox.Expr.FunExpr;
import lox.Expr.Get;
import lox.Expr.Grouping;
import lox.Expr.Literal;
import lox.Expr.Logical;
import lox.Expr.Set;
import lox.Expr.Super;
import lox.Expr.Ternary;
import lox.Expr.This;
import lox.Expr.Unary;
import lox.Expr.Variable;
import lox.Stmt.Block;
import lox.Stmt.Break;
import lox.Stmt.Class;
import lox.Stmt.Continue;
import lox.Stmt.Expression;
import lox.Stmt.Function;
import lox.Stmt.If;
import lox.Stmt.Print;
import lox.Stmt.Var;
import lox.Stmt.While;
import lox.TokenType;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap; 
import java.util.Map; 



public class Interpreter implements Expr.Visitor<Object>,Stmt.Visitor<Void>{
	
    Environment global = new Environment(); 
    Environment env = global ; 
    private final Map<Expr,Integer> locals = new HashMap<>(); 
    private boolean isBreak = false; 
    private boolean isCont = false; 
    private Object result = null; 
    private boolean isExpr = true; 
    
    public Interpreter() {
    	global.define("clock", new LoxCallable() {

			@Override
			public Object call(Interpreter interpreter, List<Object> arguments) {
				return (double) System.currentTimeMillis()/1000.0 ;
			}

			@Override
			public int arity() {
				return 0;
			}
    		
    	}
    	);
    }
	public Object interpret(List<Stmt> statements) {
		this.result = null ; 
		this.isExpr = true; 
		try {
			for(Stmt stat : statements) {
				stat.accept(this); 
				if(!(stat instanceof Stmt.Expression)) {
					this.isExpr = false;
				}
			}
		} catch (RuntimeError e) {
			Lox.runtimeError(e);
		} 
		return this.result; 
	}
	
	public void resolve(Expr expr, int value) {
		locals.put(expr, value); 
	}
	
	private String Stringfy(Object value) {
		if(value == null) return "nil"; 
		if (value instanceof Double) {
			String text = value.toString(); 
			if(text.endsWith("0")){
				text.substring(0,text.length()-2); 
			}
			return text; 
		}
		return value.toString(); 
	}
	
	public Object lookupVariable(Token name,Expr expr) {
		Integer distance = locals.get(expr); 
		if(distance != null) {
			return env.getAt(distance,name.lexeme); 
		} else {
			return global.get(name); 
		}
	}

	
	@Override
	public Object visitBinaryExpr(Binary expr) {
		Object left = expr.left.accept(this); 
		Object right = expr.right.accept(this); 
		
		switch(expr.operator.type) {
		case MINUS: 
			checkNumberOperand(expr.operator,right,left);
			return (double)left - (double) right; 
		case SLASH:
			checkNumberOperand(expr.operator,right,left);
			if ((double) right == 0 )
				throw new RuntimeError(expr.operator,"Division by zero is not allowed."); 
			return (double)left / (double) right; 
		case STAR:
			checkNumberOperand(expr.operator,right,left);
			return (double)left * (double) right ; 
		case PLUS: 
			if(left instanceof Double && right instanceof Double)
				return (double)left + (double) right; 
			if(left instanceof String && right instanceof String)
				return (String)left + (String) right; 
			if((left instanceof String && right instanceof Double) ) {
				return (String) left + (double) right ; 
			}
			if(left instanceof Double && right instanceof String) {
				return (double) left + (String) right ; 
			}
			return null; 
		case GREATER: 
			checkNumberOperand(expr.operator,right,left);
			return (double) left > (double) right; 
		case GREATER_EQUAL: 
			checkNumberOperand(expr.operator,right,left);
			return (double) left >= (double) right; 
		case LESS: 
			checkNumberOperand(expr.operator,right,left);
			return (double) left < (double) right; 
		case LESS_EQUAL : 
			checkNumberOperand(expr.operator,right,left);
			return (double) left <= (double) right; 
		case EQUAL_EQUAL : 
			return isEqual(left,right); 
		case BANG_EQUAL: 
			return !isEqual(left,right); 
		case COMMA : 
			return right;
		default : 
			checkNumberOperand(expr.operator,right,left);
			return  bitwise(expr.operator,left,right) ;
		}
	}

	@Override
	public Object visitGroupingExpr(Grouping expr) {
		return expr.expression.accept(this);
	}

	@Override
	public Object visitLiteralExpr(Literal expr) {
		return expr.value;
	}

	@Override
	public Object visitUnaryExpr(Unary expr){
		Object value = expr.right.accept(this); 
		switch(expr.operator.type) {
			case BANG : 
				return !isTruthy(value); 
			case MINUS: 
				checkNumberOperand(expr.operator,value);  
				return -(double) value; 
			default: 
				Lox.error(expr.operator, "Undefined operator for unary expression.");
		}
		return null ; 
	}

	@Override
	public Object visitTernaryExpr(Ternary expr) {	
		Boolean condition = isTruthy(expr.condition.accept(this)); 
		if(condition)
			return expr.first.accept(this); 
		else 
			return expr.second.accept(this); 
	}
	
	private Object bitwise(Token operator,Object left,Object right) {
		Double l = (Double) left; 
		Double r = (Double) right; 
		switch(operator.type) {
		case BITWISE_AND: 
			return (double) ( (l.intValue()) & (r.intValue()) ); 
		case BITWISE_OR: 
			return (double) ( (l.intValue()) | (r.intValue()) ); 
		case XOR: 
			return (double) ( (l.intValue()) ^ (r.intValue()) ); 
		default: 
			Lox.error(operator, "Undefined operator for binary expression.");
		} 
		return null; 
	}
	
	private boolean isTruthy(Object value) {
		if (value == null)
			return false; 
		if (value instanceof Boolean)
			return (boolean)value; 
		return true; 
	}
		
	private boolean isEqual(Object x,Object y) {
		if (x == null && y == null) 
			return true;
		if (x  == null)
			return false;
		return x.equals(y); 
	}
	
	private void checkNumberOperand(Token operator,Object ...operands) {
		for(Object operand : operands) {
			if (operand instanceof Double)
				continue; 
			else {
				if (operands.length == 1)
				  throw new RuntimeError(operator,"Operand must be a number"); 
				else 
				  throw new RuntimeError(operator,"Operands must be two numbers or two strings . "); 
			}
		}
		return ; 
	}

	@Override
	public Void visitExpressionStmt(Expression stmt) {
		if(isExpr)
			this.result = stmt.expression.accept(this); 
		return null; 
	}

	@Override
	public Void visitPrintStmt(Print stmt) {
		
		System.out.println(Stringfy(stmt.expression.accept(this)));
		return null;
	}

	@Override
	public Void visitVarStmt(Var stmt) {
		Object value = null; 
		if(stmt.initializer != null) {
			value = stmt.initializer.accept(this); 
		}
		env.define(stmt.name.lexeme, value);
		return null;
	}

	@Override
	public Object visitVariableExpr(Variable expr) {
		return lookupVariable(expr.name,expr); 
	}

	@Override
	public Object visitAssignExpr(Assign expr) {
		Object value = expr.value.accept(this); 
		
		Integer dist = locals.get(expr); 
		if(dist != null) {
			env.assignAt(expr.name,value,dist); 
		} else {
			global.assign(expr.name, value);
		}
		return value; 
	}

	@Override
	public Void visitBlockStmt(Block stmt) {
		executeBlock(stmt.statements, new Environment(env)); 
		return null;
	}
	
	Object executeBlock(List<Stmt> statements, Environment envi) {
		Environment previous = this.env; 
		try {
			this.env = envi; 
			for(Stmt stmt: statements) {
				stmt.accept(this); 
				if (isCont) {
					isCont = false; 
					break; 
				}
				if(isBreak) {
					break; 
				}
			}
		}finally {
			this.env = previous; 
		}
		return null; 
	}

	@Override
	public Void visitIfStmt(If stmt) {
		boolean condition = isTruthy(stmt.condition.accept(this)); 
		if(condition) { 
			return stmt.thenStmt.accept(this); 
		}
		else if (stmt.elseStmt != null )
			return stmt.elseStmt.accept(this);  
		return null; 
	}

	@Override
	public Object visitLogicalExpr(Logical expr) {
		Object left = expr.left.accept(this); 
		if(expr.operator.type == TokenType.OR) {
			if (isTruthy(left)) return left; 
		} else {
			if(!isTruthy(left)) return left; 
		}
		return expr.right.accept(this); 
	}

	@Override
	public Void visitWhileStmt(While stmt) {
		while(isTruthy(stmt.condition.accept(this)) && !isBreak) {
				stmt.Body.accept(this); 
		}
		isBreak = false; 
		return null;
	}

	@Override
	public Void visitBreakStmt(Break stmt) {
		isBreak = true; 
		return null;
	}

	@Override
	public Void visitContinueStmt(Continue stmt) {
		isCont = true; 
		return null;
	}

	@Override
	public Object visitCallExpr(Call expr) {
		Object function = expr.calle.accept(this);
	
		if(!(function instanceof LoxCallable)) {
			throw new RuntimeError(expr.paren,"only functions and classed are callable."); 
		}
		
		List<Object> arguments = new ArrayList<>(); 
		for(Expr arg : expr.args) {
			arguments.add(arg.accept(this)); 
		}
		
		
		LoxCallable fun = (LoxCallable) function; 

		if(fun.arity() != arguments.size()) {
			throw new RuntimeError(expr.paren, "Expect " + fun.arity() + " arguments but got " + arguments.size() + " arguments."); 
		} 
		
		return fun.call(this,arguments);
	}
	
	@Override
	public Void visitFunctionStmt(Function stmt) {
		LoxFunction function = new LoxFunction(stmt,env,false); 
		env.define(stmt.name.lexeme,function);
		return null;
	}
	
	@Override
	public Void visitReturnStmt(Stmt.Return stmt) {
		Object value = null; 
		if(stmt.value != null) value = stmt.value.accept(this); 
		throw new ReturnValue(value); 
	}
	
	@Override
	public Object visitFunExprExpr(FunExpr expr) {
		return new LoxFunction(new Stmt.Function(new Token(TokenType.IDENTIFIER,"",null,expr.paren.line),expr.parameters, expr.body) , env,false);
	}
	
	@Override
	public Void visitClassStmt(Class stmt) {
		Object superClass = null; 
		if(stmt.superClass != null) {
			superClass = stmt.superClass.accept(this); 
			if(!(superClass instanceof LoxClass)) {
				throw new RuntimeError(stmt.superClass.name, "Super Class must be a class. "); 
			}
		}
		env.define(stmt.name.lexeme, null);
		if(stmt.superClass != null) {
			env = new Environment(env); 
			env.define("super", superClass);
		}
		Map<String,LoxFunction> methods = new HashMap<>(); 
		Map<String,LoxFunction> statMethods = new HashMap<>(); 
		for(Stmt.Function method : stmt.methods) {
			LoxFunction fun = new LoxFunction(method,env,method.name.lexeme.equals("init")); 
			methods.put(method.name.lexeme,fun); 
		}
		for(Stmt.Function method : stmt.staticMethods) {
			LoxFunction fun = new LoxFunction(method,env,method.name.lexeme.equals("init")); 
			statMethods.put(method.name.lexeme,fun); 
		}
		LoxClass klass = new LoxClass(stmt.name.lexeme,(LoxClass) superClass, methods,statMethods); 
		if(stmt.superClass != null) {
			env = env.enclosing;
		}
		env.assign(stmt.name,klass);
		return null;
	}
	
	@Override
	public Object visitGetExpr(Get expr) { 
		Object object = expr.object.accept(this); 
		if(object instanceof LoxInstance ) {
			return ((LoxInstance) object).get(expr.name); 
		}
		throw new RuntimeError(expr.name, "Only Instances has fields ."); 
	}
	
	@Override
	public Object visitSetExpr(Set expr) {
		Object object = expr.object.accept(this); 
		Object value = expr.value.accept(this); 
		if(object instanceof LoxInstance) {
			((LoxInstance) object).set(expr.name,value); 
		} else {
			throw new RuntimeError(expr.name,"Only instances have fields."); 
		}
		return value; 
	}
	
	@Override
	public Object visitThisExpr(This expr) {
		return lookupVariable(expr.keyword,expr);
	}
	
	@Override
	public Object visitSuperExpr(Super expr) {
		int dist = locals.get(expr); 
		LoxClass superclass = (LoxClass) env.getAt(dist,"super"); 
		LoxInstance thisclass = (LoxInstance) env.getAt(dist - 1, "this"); 
		LoxFunction method = superclass.findMethod(thisclass,expr.method.lexeme); 
		if(method == null ) {
			throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme +"'.") ;
		}
		return method; 
	}
}
