package lox;

import java.util.List;
import java.util.ArrayList; 
import java.util.Arrays; 


import static lox.TokenType.*;
import lox.Lox;

public class Parser {
	private final List<Token> tokens;
	private int current = 0;
	
	@SuppressWarnings("serial")
	private static class ParseError extends RuntimeException {}
	
	Parser(List<Token> to)
	{
		this.tokens = to;
	}
	
	List<Stmt> parse() {
		List<Stmt> statements = new ArrayList<>(); 
		while(!isAtEnd()) {
			statements.add(declartion()); 
		}
		return statements; 
	}
	
	private Stmt declartion() {
		try {
			if(match(VAR)) return varDeclartion();
			if(match(FUN)) return function("function"); 
			if(match(CLASS)) return classDeclartion();  
			return statement(); 
		} catch(ParseError error) {
			synchronize(); 
			return null; 
		}
	}
	
	private Stmt classDeclartion() {
		Token name = consume(IDENTIFIER , "Expect class name.");
		Expr.Variable superClass = null; 
		if(match(LESS)) {
			Token superName = consume(IDENTIFIER , "Expect superclass name."); 
			superClass = new Expr.Variable(superName); 
		}
		
		consume(LEFT_BRACE , "Expecte '{' after class name."); 
		
		List<Stmt.Function> funs = new ArrayList<>(); 
		List<Stmt.Function> statFuns = new ArrayList<>(); 
		while(!check(RIGHT_BRACE) && !isAtEnd()) {
			if(match(CLASS)) {
				statFuns.add(function("static method"));
				continue; 
			}
			funs.add(function("method")); 
		}
		consume(RIGHT_BRACE, "Expect '}' after class body."); 
		return new Stmt.Class(name,superClass,funs,statFuns);
	}
	
	private Stmt.Function function(String kind) {
		Token name = consume(IDENTIFIER , "Expect " + kind + " name"); 
		List<Token> params = new ArrayList<>(); 
		consume(LEFT_PAREN , "Expect'(' after the name of " + kind ); 
		if(!check(RIGHT_PAREN)) {
			do {
				if(params.size() >= 8 ) {
					error(peek(), "Cannot have more than 8 parameters."); 
				}
				
				Token parm = consume(IDENTIFIER,"Expect identifier for the parameter. "); 
				params.add(parm); 
			} while (match(COMMA)); 
		}
		consume(RIGHT_PAREN , "Expect ')' after parameters ."); 
		consume(LEFT_BRACE, "Expect ')' after parameters ."); 
		List<Stmt> funBlock = block(); 
		
		return new Stmt.Function(name,params,funBlock); 
	}
	

	
	private Stmt varDeclartion() {
		Token name = consume(IDENTIFIER, "Expected Variable name.");
		Expr init = null; 
		if(match(EQUAL)) {
			init = expression();  
		}
		consume(SEMICOLON, "Expect ';' after value."); 
		return new Stmt.Var(name, init);	
	}
	private Stmt statement() {
		if(match(PRINT)) return printStmt(); 
		if(match(LEFT_BRACE)) return new Stmt.Block(previous(),block()); 
		if(match(IF)) return ifStmt(); 
		if(match(WHILE)) return whileStmt(); 
		if(match(FOR)) return forStmt();
		if(match(BREAK)) return breakStmt(); 
		if(match(CONTINUE)) return contStmt(); 
		if(match(RETURN)) return returnStmt() ; 
		return exprStmt(); 
	}
	
	Stmt returnStmt() {
		Token paren = previous(); 
		Expr value = null; 
		if(!check(SEMICOLON)) {
			value = expression(); 
		}
		consume(SEMICOLON, "Expect';' at the end."); 
		return new Stmt.Return(paren, value); 
	}
	
	private Stmt contStmt() {
		Token name = previous(); 
		consume(SEMICOLON,"Expect ';' after continue."); 
		return new Stmt.Continue(name); 
	}
	private Stmt breakStmt() {
		Token name = previous(); 
		consume(SEMICOLON,"Expect ';' after break."); 
		return new Stmt.Break(name); 
	}
	private Stmt forStmt() {
		Token paren = consume(LEFT_PAREN,"Expect '(' after 'for' . "); 
		Stmt init ; 
		if(match(SEMICOLON)) {
			init = null;  
		} else if (match(VAR)) {
			init = varDeclartion(); 
		} else {
			init = exprStmt(); 
		}
		Expr condition = null ; 
		if(!check(SEMICOLON)) {
			condition = expression(); 
			consume(SEMICOLON,"Expect SEMICOLON after condition."); 
		}
		Expr inc = null; 
		if(!check(RIGHT_PAREN)) {
			inc = expression();  
			consume(RIGHT_PAREN,"Expect ')' after increment."); 
		}
		
		Stmt body = statement(); 
		if(inc != null)
			body = new Stmt.Block(paren,Arrays.asList(body,new Stmt.Expression(inc)));
		
		if(condition == null) {
			condition = new Expr.Literal(true); 
		}
		
		body = new Stmt.While(condition, body); 
		body = new Stmt.Block(paren,Arrays.asList(init,body)); 
		return body; 
	}
	
	private Stmt whileStmt() {
		consume(LEFT_PAREN,"Expect '(' after 'while'."); 
		Expr condition = expression(); 
		consume(RIGHT_PAREN, "Expect ')' after 'while' condition."); 
		Stmt body = statement(); 
		return new Stmt.While(condition,body); 
	}
	
	private Stmt ifStmt() {
		consume(LEFT_PAREN,"Expect '(' after 'if' ."); 
		Expr condition = expression(); 
		consume(RIGHT_PAREN,"Expect ')' after condition.") ;
		Stmt Block = statement(); 
		Stmt elseBlock = null; 
		if(match(ELSE)) { 
			elseBlock = statement(); 
		}
		return new Stmt.If (condition,Block,elseBlock); 
	}
 	
	private List<Stmt> block(){
		List<Stmt> statements = new ArrayList<>(); 
		while(!check(RIGHT_BRACE) && !isAtEnd()) {
			statements.add(declartion()); 
		}
		consume(RIGHT_BRACE,"Expect '}' after a block."); 
		return statements; 
	}
	
	private Stmt printStmt() {
		Expr value = expression();
		consume(SEMICOLON, "Expect ';' after value."); 
		return new Stmt.Print(value); 
	}
	
	private Stmt exprStmt() {
		Expr value = expression();
		consume(SEMICOLON,"Expect ';' after value."); 
		return new Stmt.Expression(value); 
	}
	
	private ParseError error(Token token,String message) {
		Lox.error(token, message);
		return new ParseError(); 
	}
	
	private Expr expression()
	{
		if(match(FUN)) {
			return funExpr(); 
		}
		Expr expr = assignment(); 
		return expr;
	}
	
	private Expr funExpr() {
		Token paren = consume(LEFT_PAREN , "Expect '(' after fun. "); 
		List<Token> arguments = new ArrayList<>(); 
		if(!check(RIGHT_PAREN))
			do {
				if(arguments.size() >= 8) {
					error(peek() ,"number of parameters should not exceed 8."); 
				}  
				arguments.add(consume(IDENTIFIER , "Expect a name of a parameter.")); 
			} while(match(COMMA));
		consume(RIGHT_PAREN , "Expect ')' after parameters.");
		consume(LEFT_BRACE , "Expect '{' after function expression body."); 
		List<Stmt> body = block(); 
		return new Expr.FunExpr(paren,arguments, body); 
	}
	
	private Expr assignment() {
		Expr expr = or(); 
		if (match(EQUAL)) { 
			Expr left = assignment() ;
			if(expr instanceof Expr.Variable) {
				Token name = ((Expr.Variable)expr).name; 
				return new Expr.Assign(name,left); 
			} else if (expr instanceof Expr.Get) {
				Expr.Get get = (Expr.Get) expr; 
				return new Expr.Set(get.object,get.name,left); 
			}
		}
		return expr; 	
	}
	
	private Expr or() {
		Expr expr = and(); 
		while(match(OR)) {
			Token operator = previous();
			Expr right = and(); 
			expr = new Expr.Logical(expr, operator, right); 
		}
		return expr; 
	}
	
	private Expr and() {
		Expr expr = ternary(); 
		while(match(AND)) {
			Token operator = previous(); 
			Expr right = ternary(); 
			expr = new Expr.Logical(expr, operator, right); 
		}
		return expr ; 
	}
	
	private Expr ternary() {
		Expr expr = equality(); 
		if(match(QMARK)) {
			Expr first = equality(); 
			if(match(COLON)) {
				Expr second = equality();
				return new Expr.Ternary(expr,first,second); 
			}
		}
		return expr; 
	}
	
	private Expr equality()
	{
		Expr expr = bitOr();
		
		while(match(BANG_EQUAL , EQUAL_EQUAL))
		{
			Token operator = previous();
			Expr right = bitOr();
			expr = new Expr.Binary(expr,operator,right);
		}
		
		return expr;
	}
	
	private Expr bitOr() {
		Expr expr = xor(); 
		while(match(BITWISE_OR)) {
			Token operator = previous(); 
			Expr right = xor(); 
			expr = new Expr.Binary(expr, operator, right); 
		}
		return expr; 
	}
	
	private Expr xor() {
		Expr expr = bitAnd(); 
		while(match(XOR)) {
			Token operator = previous(); 
			Expr right = bitAnd(); 
			expr = new Expr.Binary(expr, operator, right); 
		}
		return expr; 
	}
	
	private Expr bitAnd() {
		Expr expr = comparison(); 
		while(match(BITWISE_AND)) {
			Token operator = previous(); 
			Expr right = comparison (); 
			expr = new Expr.Binary(expr, operator, right); 
		}
		return expr; 
	}
	
	private Expr comparison() {
		Expr expr = addition(); 
		while(match(GREATER,GREATER_EQUAL,LESS,LESS_EQUAL)){
			Token operator = previous(); 
			Expr right = addition(); 
			expr = new Expr.Binary(expr, operator, right); 
		}
		return expr; 
	}
	
	private Expr addition() {
		Expr expr = multiplication(); 
		while(match(PLUS,MINUS)) {
			Token operator = previous(); 
			Expr right = multiplication(); 
			expr = new Expr.Binary(expr, operator, right); 
		}
		return expr; 
	}
	
	private Expr multiplication() {
		Expr expr = unary(); 
		while(match(STAR,SLASH)) {
			Token operator = previous(); 
			Expr right = unary(); 
			expr = new Expr.Binary(expr, operator, right); 
		}
		return expr; 
	}
	
	private Expr unary() {
		if(match(MINUS,BANG)) {
			Expr expr; 
			Token operator = previous(); 
			Expr exp = unary(); 
			expr = new Expr.Unary(operator,exp);
			return expr; 
		} 
		return call(); 
	}
	
	private Expr call() {
		Expr expr = primary(); 
		while(true) {
			if(match(LEFT_PAREN)) {
				expr = args(expr); 
			} else if(match(DOT)) {
					Token name = consume(IDENTIFIER, "Expect property name after '.'.") ; 
					expr = new Expr.Get(expr,name); 
			} else {
				break; 
			}
		}	
		return expr ; 
	}
	
	Expr args(Expr expr) {
		List<Expr> ar = new ArrayList<>();
		if(!check(RIGHT_PAREN)) {
			if(ar.size() >= 8) {
				error(peek(),"Cannot have more than 8 arguments. "); 
			}
			do {
				ar.add(expression()); 
			} while(match(COMMA)); 
		}
		Token paren = consume(RIGHT_PAREN , "Expected ')' after arguments."); 
		return new Expr.Call(expr, paren, ar); 
	}
	
	private Expr primary() {
		if(match(FALSE)) return new Expr.Literal(false); 
		if(match(TRUE))  return new Expr.Literal(true); 
		if(match(NIL)) return new Expr.Literal(null); 
		
		if(match(NUMBER,STRING)) {
			return new Expr.Literal(previous().literal); 
		}
		
		if(match(IDENTIFIER)) {
			return new Expr.Variable(previous()); 
		}
		
		if(check(PLUS,MINUS,SLASH,STAR,GREATER,GREATER_EQUAL,LESS,LESS_EQUAL,BANG_EQUAL,EQUAL_EQUAL,COMMA,XOR,BITWISE_OR,BITWISE_AND)) {
			Lox.error(peek(), "An operator in a begining of a binary expression.");
			return null; 
		}
		
		if(match(THIS)) return new Expr.This(previous()); 
		
		if(match(SUPER)) {
			Token _super = previous(); 
			consume(DOT,"Expect '.' after super."); 
			Token method = consume(IDENTIFIER , "Expect method name after '.' ."); 
			return new Expr.Super(_super,method); 
		}
		
		if(match(LEFT_PAREN)) {
			Expr expr = expression(); 
			consume(RIGHT_PAREN,"Expect ' ) ' after expression. "); 
			return new Expr.Grouping(expr); 
		}
		
		throw error(peek() , "Expect expression. ") ;
	}
	
	private Token consume(TokenType type,String message) {
		if(check(type)) return advance(); 
		throw error(peek(),message); 
	}
	
	private boolean match(TokenType ...tokens) { 
		for(TokenType  type : tokens) {
			if (check(type)) {
				advance(); 
				return true; 
			}
		}
		return false; 
	}
	
	private Token peek() {
		return this.tokens.get(current); 
	}
	
	private boolean check(TokenType type) {
		if(isAtEnd()) return false ; 
		return peek().type == type; 
	}
	
	private boolean check(TokenType ...tokens) {
		for(TokenType type : tokens) {
			if(check(type)) {
				return true; 
			}
		}
		return false;
	}
	
	private Token advance() {
		if(!isAtEnd()) current++; 
		return previous(); 
	}
	
	private boolean isAtEnd() {
		return peek().type == EOF; 
	}
	
	private Token previous() {
		return this.tokens.get(current-1); 
	}
	
	void synchronize() {
		advance();
		switch(peek().type) {
		case CLASS: 
		case FUN:
		case VAR:
		case FOR:
		case IF:
		case WHILE:
		case PRINT:
		case RETURN:
			return ;
		default:
		}
		advance(); 
	}
}