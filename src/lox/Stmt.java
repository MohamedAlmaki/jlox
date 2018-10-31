package lox;

import java.util.List;

abstract class Stmt {
    interface Visitor<R> {
    R visitExpressionStmt(Expression stmt);
    R visitBlockStmt(Block stmt);
    R visitFunctionStmt(Function stmt);
    R visitClassStmt(Class stmt);
    R visitIfStmt(If stmt);
    R visitPrintStmt(Print stmt);
    R visitVarStmt(Var stmt);
    R visitWhileStmt(While stmt);
    R visitBreakStmt(Break stmt);
    R visitContinueStmt(Continue stmt);
    R visitReturnStmt(Return stmt);
  }

 static class Expression extends Stmt {
    Expression(Expr expression) {
      this.expression=expression;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitExpressionStmt(this);
     }

     final Expr expression;
   }

 static class Block extends Stmt {
    Block(Token paren, List<Stmt> statements) {
      this.paren=paren;
      this.statements=statements;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitBlockStmt(this);
     }

     final Token paren;
     final List<Stmt> statements;
   }

 static class Function extends Stmt {
    Function(Token name , List<Token> parameters , List<Stmt> body) {
      this.name=name;
      this.parameters=parameters;
      this.body=body;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitFunctionStmt(this);
     }

     final Token name ;
     final List<Token> parameters ;
     final List<Stmt> body;
   }

 static class Class extends Stmt {
    Class(Token name , Expr.Variable superClass , List<Stmt.Function> methods, List<Stmt.Function> staticMethods) {
      this.name=name;
      this.superClass=superClass;
      this.methods=methods;
      this.staticMethods=staticMethods;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitClassStmt(this);
     }

     final Token name ;
     final Expr.Variable superClass ;
     final List<Stmt.Function> methods;
     final List<Stmt.Function> staticMethods;
   }

 static class If extends Stmt {
    If(Expr condition, Stmt thenStmt, Stmt elseStmt) {
      this.condition=condition;
      this.thenStmt=thenStmt;
      this.elseStmt=elseStmt;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitIfStmt(this);
     }

     final Expr condition;
     final Stmt thenStmt;
     final Stmt elseStmt;
   }

 static class Print extends Stmt {
    Print(Expr expression) {
      this.expression=expression;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitPrintStmt(this);
     }

     final Expr expression;
   }

 static class Var extends Stmt {
    Var(Token name, Expr initializer) {
      this.name=name;
      this.initializer=initializer;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitVarStmt(this);
     }

     final Token name;
     final Expr initializer;
   }

 static class While extends Stmt {
    While(Expr condition, Stmt Body) {
      this.condition=condition;
      this.Body=Body;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitWhileStmt(this);
     }

     final Expr condition;
     final Stmt Body;
   }

 static class Break extends Stmt {
    Break(Token name) {
      this.name=name;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitBreakStmt(this);
     }

     final Token name;
   }

 static class Continue extends Stmt {
    Continue(Token name) {
      this.name=name;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitContinueStmt(this);
     }

     final Token name;
   }

 static class Return extends Stmt {
    Return(Token keyword, Expr value) {
      this.keyword=keyword;
      this.value=value;
    }

    <R> R accept(Visitor<R> visitor) {
     return visitor.visitReturnStmt(this);
     }

     final Token keyword;
     final Expr value;
   }

abstract <R> R accept(Visitor<R> visitor);
}
