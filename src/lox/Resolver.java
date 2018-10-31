package lox;

import java.util.HashMap;
import java.util.List; 
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

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
import lox.Stmt.Return;
import lox.Stmt.Var;
import lox.Stmt.While; 


public class Resolver implements Stmt.Visitor<Void>,Expr.Visitor<Object> {
	private final Interpreter interpreter; 
	private final Stack<Map<String,VariableState>> scopes = new Stack<>(); 
	private FunctionType currentFunction = FunctionType.NONE; 
	private inLoop currentBlock = inLoop.NO;
	private ClassType currentClass = ClassType.NONE;
	
	Resolver(Interpreter interpreter){
		this.interpreter = interpreter; 
	}
	
	
	private enum FunctionType {
		NONE,
		FUNCTION,
		METHOD,
		INIT
	}
	
	private enum VariableState {
		DECLARED , 
		DEFINED,
		USED , 
	}

	private enum inLoop {
		YES , 
		NO
	}
	
	private enum ClassType{
		NONE, 
		CLASS,
		SUBCLASS
	}
	
	@Override
	public Object visitBinaryExpr(Binary expr) {
		resolve(expr.left); 
		resolve(expr.right); 
		return null;
	}

	@Override
	public Object visitAssignExpr(Assign expr) {
		resolve(expr.value);  
		resolveLocal(expr,expr.name); 
		return null;
	}

	@Override
	public Object visitGroupingExpr(Grouping expr) {
		resolve(expr.expression); 
		return null;
	}

	@Override
	public Object visitLiteralExpr(Literal expr) {
		return null;
	}

	@Override
	public Object visitUnaryExpr(Unary expr) {
		resolve(expr.right); 
		return null;
	}

	@Override
	public Object visitTernaryExpr(Ternary expr) {
		resolve(expr.condition); 
		resolve(expr.first) ;
		resolve(expr.second); 
		return null;
	}

	@Override
	public Object visitVariableExpr(Variable expr) {
		if( !scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == VariableState.DECLARED)
				Lox.error(expr.name, "Cannot read local Variable on its own initalizer .");
		resolveLocal(expr,expr.name); 
		return null;
	}

	@Override
	public Object visitLogicalExpr(Logical expr) {
		resolve(expr.left); 
		resolve(expr.right); 
		return null;
	}

	@Override
	public Object visitCallExpr(Call expr) {
		resolve(expr.calle); 
		for (Expr e : expr.args) {
			resolve(e); 
		}
		return null;
}

	@Override
	public Object visitFunExprExpr(FunExpr expr) {
		resolveFunction(expr,FunctionType.FUNCTION); 
		return null;
	}

	@Override
	public Void visitExpressionStmt(Expression stmt) {
		resolve(stmt.expression); 
		return null;
	}

	@Override
	public Void visitBlockStmt(Block stmt) {
		beginScope(); 
		resolve(stmt.statements); 
		endScope(stmt.paren);
		return null;
	}

	@Override
	public Void visitFunctionStmt(Function stmt) {
		declare(stmt.name); 
		define(stmt.name); 
		used(stmt.name); 
		resolveFunction(stmt,FunctionType.FUNCTION); 
		return null;
	}

	@Override
	public Void visitIfStmt(If stmt) {
		resolve(stmt.condition); 
		resolve(stmt.thenStmt); 
		if(stmt.elseStmt != null) resolve(stmt.elseStmt); 
		return null;
	}

	@Override
	public Void visitPrintStmt(Print stmt) {
		resolve(stmt.expression); 
		return null;
	}

	@Override
	public Void visitVarStmt(Var stmt) {
		declare(stmt.name); 
		if(stmt.initializer != null) 
			resolve(stmt.initializer); 
		define(stmt.name); 
		if(stmt.initializer != null)
			used(stmt.name); 
		return null;
	}

	@Override
	public Void visitWhileStmt(While stmt) {
		resolve(stmt.condition); 
		inLoop enclosing = currentBlock; 
		currentBlock = inLoop.YES; 
		resolve(stmt.Body); 
		currentBlock = enclosing; 
		return null;
	}

	@Override
	public Void visitBreakStmt(Break stmt) {
		if(currentBlock == inLoop.NO) {
			Lox.error(stmt.name,"Break cannot be used outside a loop.");
		}
		return null;
	}

	@Override
	public Void visitContinueStmt(Continue stmt) {
		if(currentBlock == inLoop.NO) {
			Lox.error(stmt.name, "Contine cannot be used outside a loop.");
		}
		return null;
	}

	@Override
	public Void visitReturnStmt(Return stmt) {
		if(currentFunction == FunctionType.NONE) {
			Lox.error(stmt.keyword,"Cannot return from a top-level code .");
		}
		if(stmt.value != null) {
			if(currentFunction == FunctionType.INIT) {
				Lox.error(stmt.keyword, "Cannot return a value from init.");
			}
			resolve(stmt.value); 
		}
		return null;
	}
	
	public void resolve(List<Stmt> statements) {
		for(Stmt stat : statements) {
			resolve(stat); 
		}
	}
	
	private void resolve(Stmt stat) {
		stat.accept(this); 
	}
	
	private void resolve(Expr expr) {
		expr.accept(this); 
	}
	
	private void resolveLocal(Expr expr,Token name) {
		for(int i=scopes.size()-1; i>= 0; i--) {
			if(scopes.get(i).containsKey(name.lexeme)) {
				used(name); 
				interpreter.resolve(expr,scopes.size()-1-i); 
			}
		}
	}
	
	private void resolveFunction(Stmt.Function stmt,FunctionType type) {
		FunctionType enclosingFunction = currentFunction ; 
		currentFunction = type; 
		beginScope();
		for(Token parm : stmt.parameters ) {
			declare(parm); 
			define(parm); 
			used(parm); 
		}
		resolve(stmt.body); 
		endScope(stmt.name); 
		currentFunction = enclosingFunction ;  
	}
	
	private void resolveFunction(Expr.FunExpr stmt,FunctionType type) {
		FunctionType enclosingFunction = currentFunction; 
		currentFunction = type ; 
		beginScope();
		for(Token parm : stmt.parameters ) {
			declare(parm); 
			define(parm); 
			used(parm); 
		}
		resolve(stmt.body); 
		endScope(stmt.paren); 
		currentFunction = enclosingFunction; 
		return; 
	}
	
	private void beginScope() {
		scopes.push(new HashMap<String,VariableState>()); 
	}
	
	private void endScope(Token name) { 
		usedVariable(name);
		scopes.pop(); 
	}
	
	private void usedVariable(Token name) {
		Map<String,VariableState> scope = scopes.peek(); 
	
		for (Entry<String,VariableState> entr: scope.entrySet()) {
			String key = entr.getKey();
			if(scope.get(key) != VariableState.USED ){
				Lox.error(name.line, "Local Variable " + key + " in this block is never used.");
			}
		}
	}
	
	private void declare(Token name) {
		if(scopes.isEmpty()) return ; 
		 
		Map<String,VariableState> scope = scopes.peek();
		
		if(scope.containsKey(name.lexeme)) {
			Lox.error(name.line, "a Variable with the same name already declared in this scope. ");
		}
		scope.put(name.lexeme, VariableState.DECLARED); 
	}
	
	private void define(Token name) {
		if(scopes.isEmpty()) return ; 
		scopes.peek().put(name.lexeme,VariableState.DEFINED); 
	}
	
	private void used(Token name) {
		if(scopes.isEmpty()) return; 
		scopes.peek().put(name.lexeme, VariableState.USED); 
	}

	@Override
	public Void visitClassStmt(Class stmt) {
		ClassType enclosingClass = currentClass; 
		currentClass = ClassType.CLASS;
		declare(stmt.name); 
		if(stmt.superClass != null) {
			currentClass = ClassType.SUBCLASS; 
			resolve(stmt.superClass); 
		}
		define(stmt.name); 
		if(stmt.superClass != null) {
			beginScope(); 
			scopes.peek().put("super",VariableState.USED);
		}
		beginScope();
		scopes.peek().put("this", VariableState.USED);
		for(Stmt.Function method : stmt.methods) {
			FunctionType declartion = FunctionType.METHOD; 
			if(method.name.lexeme.equals("init")) {
				declartion = FunctionType.INIT;
			}
			resolveFunction(method,declartion); 
		}
		for(Stmt.Function method : stmt.staticMethods) {
			FunctionType declartion = FunctionType.METHOD; 
			if(method.name.lexeme.equals("init")) {
				Lox.error(stmt.name, "init function of the class " + stmt.name.lexeme + " cannot be a static function 'remove class'.");
			}
			resolveFunction(method,declartion); 
		}
		used(stmt.name); 
		endScope(stmt.name);
		if(stmt.superClass != null) endScope(stmt.name); 
		currentClass = enclosingClass; 
		return null;
	}

	@Override
	public Object visitGetExpr(Get expr) {
		resolve(expr.object); 
		return null;
	}

	@Override
	public Object visitSetExpr(Set expr) {
		resolve(expr.value); 
		resolve(expr.object); 
		return null;
	}

	@Override
	public Object visitThisExpr(This expr) {
		if(currentClass == ClassType.NONE) {
			Lox.error(expr.keyword, "Cannot use 'this' outside a class.");
		}
		resolveLocal(expr,expr.keyword); 
		return null;
	}

	@Override
	public Object visitSuperExpr(Super expr) {
		if(currentClass == ClassType.NONE) {
			Lox.error(expr.keyword, "Cannot use 'super' outside a class.");
		} else if (currentClass == ClassType.CLASS) {
			Lox.error(expr.keyword, "Cannot use 'super' in a class with no superclass.");
		}
		resolveLocal(expr,expr.keyword); 
		return null;
	}
	
}
