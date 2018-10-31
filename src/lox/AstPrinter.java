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

//chapter 5 challenge 3 RPN implementation 
public class AstPrinter implements Expr.Visitor<String>{
	String print(Expr expr) {
		return expr.accept(this); 
	}

	@Override
	public String visitBinaryExpr(Binary expr) {
		return parnethesize(expr.operator.lexeme,expr.left,expr.right); 
	}

	@Override
	public String visitGroupingExpr(Grouping expr) {
		 return parnethesize("group",expr.expression); 
	}

	@Override
	public String visitLiteralExpr(Literal expr) {
		if(expr.value == null)
			return "nil"; 
		return expr.value.toString(); 
	}

	@Override
	public String visitUnaryExpr(Unary expr) {
		return parnethesize(expr.operator.lexeme,expr.right); 
	}
	
	//
	private String parnethesize(String operator,Expr ...exprs) {
		StringBuilder str = new StringBuilder(); 
		//str.append("("); 
		for(Expr exp : exprs) {
			str.append(exp.accept(this)); 
			str.append(" "); 
		}
		str.append(operator); 
		
		return str.toString(); 
	}

	@Override
	public String visitTernaryExpr(Ternary expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitAssignExpr(Assign expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitVariableExpr(Variable expr) {
		// TODO Auto-generated method stub
	
		return null;
	}

	@Override
	public String visitLogicalExpr(Logical expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitCallExpr(Call expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitFunExprExpr(FunExpr expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitGetExpr(Get expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitSetExpr(Set expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitThisExpr(This expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitSuperExpr(Super expr) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}

class A {
	public static void hello() {
		System.out.print("Hello");
	}
}

class B extends A {
}
	

