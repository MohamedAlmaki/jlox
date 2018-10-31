package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;



public class GenerateAST {
public static void main(String[] args)  {
	if(args.length != 1) {
		System.err.println("Usage: generate_ast <output directory>");
	    System.exit(1);
	} 
	    String outputDir = args[0];//output dir is the directory of the project classes.
	    try {
	    defineAst(outputDir,"Expr",Arrays.asList("Binary : Expr left, Token operator, Expr right",
	    										"Assign : Token name, Expr value",
	    		                                "Grouping : Expr expression",
	    		                                "Literal : Object value",
	    		                                "Unary : Token operator, Expr right",
	    		                                "Ternary : Expr condition, Expr first, Expr second",
	    		                                "Variable : Token name", 
	    		                                "Logical : Expr left, Token operator , Expr right",
	    		                                "Call : Expr calle , Token paren , List<Expr> args",
	    		                                "FunExpr  : Token paren, List<Token> parameters, List<Stmt> body",
	    		                                "Get : Expr object , Token name",
	    		                                "Set : Expr object , Token name , Expr value",
	    		                                "This : Token keyword", 
	    		                                "Super : Token keyword , Token method"
	    		                                ));
	    defineAst(outputDir, "Stmt", Arrays. asList(
	    		"Expression : Expr expression",
	    		"Block : Token paren, List<Stmt> statements",
	    		"Function : Token name , List<Token> parameters , List<Stmt> body",
	    		"Class : Token name , Expr.Variable superClass , List<Stmt.Function> methods, List<Stmt.Function> staticMethods ",
	    		"If : Expr condition, Stmt thenStmt, Stmt elseStmt", 
	    		"Print : Expr expression",
	    		"Var  : Token name, Expr initializer",
	    		 "While : Expr condition, Stmt Body",
	    		 "Break : Token name",
	    		 "Continue: Token name",
	    		 "Return : Token keyword, Expr value"
	    		) ) ;  }
	    	catch (IOException i){
	    		System.out.println(i);
	    	}
   }
public static void defineAst(String outputDir,String baseName,List<String> types) throws IOException {
	String path = outputDir + "/" + baseName + ".java";
	PrintWriter writer = new PrintWriter(path,"UTF-8");
	
	writer.println("package lox;");
	writer.println("");
	writer.println("import java.util.List;");
	writer.println("");
	writer.println("abstract class " + baseName + " {");
	
	defineVisitor(writer,baseName,types);

	
	//The ast classes 
	for(String type : types) {
		String className = type.split(":")[0].trim();
		String fields = type.split(":")[1].trim();
		defineType(writer,baseName,className,fields);
	}
	
	writer.println("") ;
	writer.println("abstract <R> R accept(Visitor<R> visitor);") ;
	
	writer.println("}");
	writer.close();
}

public static void defineVisitor(PrintWriter writer,String baseName,List<String> types) {
    writer.println("    interface Visitor<R> {");
    
    for(String type : types ) {
       String typeName = type.split(":")[0].trim();
       writer.println("    R visit"+ typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
}
    
    writer.println("  }");
}
public static void defineType(PrintWriter writer,String baseName,String className,String fieldList) throws IOException{
	writer.println("");
	writer.println(" static class "+className+ " extends "+baseName + " {");
	
	writer.println("    " + className + "(" + fieldList + ") {");
	
	String[] fields = fieldList.split(", ");
	for (String field : fields ) {
		String name = field.split(" ")[1];
		writer.println("      this." + name + "=" + name + ";");
	}
	
	writer.println("    }");
	writer.println();
	writer.println("    <R> R accept(Visitor<R> visitor) {");
	writer.println("     return visitor.visit" + className + baseName + "(this);");
	writer.println("     }");
	
	writer.println();
	for(String field : fields ) {
		writer.println("     final " + field + ";");
	}
	
	writer.println("   }");
}
}
