package lox;

import java.util.List;

abstract class Expr {
    interface Visitor<R> {
    R visitBinaryExpr(Binary expr);
    R visitAssignExpr(Assign expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitUnaryExpr(Unary expr);
    R visitTernaryExpr(Ternary expr);
    R visitVariableExpr(Variable expr);
    R visitLogicalExpr(Logical expr);
    R visitCallExpr(Call expr);
    R visitFunExprExpr(FunExpr expr);
    R visitGetExpr(Get expr);
    R visitSetExpr(Set expr);
    R visitThisExpr(This expr);
    R visitSuperExpr(Super expr);
  }

 static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
      this.left=left;
      this.operator=operator;
      this.right=right;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitBinaryExpr(this);
     }

     final Expr left;
     final Token operator;
     final Expr right;
   }

 static class Assign extends Expr {
    Assign(Token name, Expr value) {
      this.name=name;
      this.value=value;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitAssignExpr(this);
     }

     final Token name;
     final Expr value;
   }

 static class Grouping extends Expr {
    Grouping(Expr expression) {
      this.expression=expression;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitGroupingExpr(this);
     }

     final Expr expression;
   }

 static class Literal extends Expr {
    Literal(Object value) {
      this.value=value;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitLiteralExpr(this);
     }

     final Object value;
   }

 static class Unary extends Expr {
    Unary(Token operator, Expr right) {
      this.operator=operator;
      this.right=right;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitUnaryExpr(this);
     }

     final Token operator;
     final Expr right;
   }

 static class Ternary extends Expr {
    Ternary(Expr condition, Expr first, Expr second) {
      this.condition=condition;
      this.first=first;
      this.second=second;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitTernaryExpr(this);
     }

     final Expr condition;
     final Expr first;
     final Expr second;
   }

 static class Variable extends Expr {
    Variable(Token name) {
      this.name=name;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitVariableExpr(this);
     }

     final Token name;
   }

 static class Logical extends Expr {
    Logical(Expr left, Token operator , Expr right) {
      this.left=left;
      this.operator=operator;
      this.right=right;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitLogicalExpr(this);
     }

     final Expr left;
     final Token operator ;
     final Expr right;
   }

 static class Call extends Expr {
    Call(Expr calle , Token paren , List<Expr> args) {
      this.calle=calle;
      this.paren=paren;
      this.args=args;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitCallExpr(this);
     }

     final Expr calle ;
     final Token paren ;
     final List<Expr> args;
   }

 static class FunExpr extends Expr {
    FunExpr(Token paren, List<Token> parameters, List<Stmt> body) {
      this.paren=paren;
      this.parameters=parameters;
      this.body=body;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitFunExprExpr(this);
     }

     final Token paren;
     final List<Token> parameters;
     final List<Stmt> body;
   }

 static class Get extends Expr {
    Get(Expr object , Token name) {
      this.object=object;
      this.name=name;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitGetExpr(this);
     }

     final Expr object ;
     final Token name;
   }

 static class Set extends Expr {
    Set(Expr object , Token name , Expr value) {
      this.object=object;
      this.name=name;
      this.value=value;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitSetExpr(this);
     }

     final Expr object ;
     final Token name ;
     final Expr value;
   }

 static class This extends Expr {
    This(Token keyword) {
      this.keyword=keyword;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitThisExpr(this);
     }

     final Token keyword;
   }

 static class Super extends Expr {
    Super(Token keyword , Token method) {
      this.keyword=keyword;
      this.method=method;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitSuperExpr(this);
     }

     final Token keyword ;
     final Token method;
   }

abstract <R> R accept(Visitor<R> visitor);
}
