package lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;



public class Lox {
	static boolean hadError = false;
	static boolean hadRuntimeError = false; 
	public static final Interpreter interpreter = new Interpreter(); 
	
	//if there is one argument consider it as a path for a lox script else run the shell (where we execute line by line e.g python shell). 
	public static void main(String[] args) throws IOException {
		if(args.length > 1) {
			System.out.println("Usage: java lox");
		}else if(args.length == 1) {
			runFile(args[0]);
		}else {
			runPrompt();
		}
	}
	
	//running methods runFile and runPrompt they are a wrapper for the run method 
	private static void runFile(String path) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String (bytes,Charset.defaultCharset()));
		if(hadError) System.exit(65);
		if(hadRuntimeError) System.exit(70);
	}
	
	private static void runPrompt() throws IOException{
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);
		for(;;) {
			System.out.print("> "); 
			Object value; 
			String command = reader.readLine(); 
			int i = 0;
			while(command.charAt(command.length()-1 - i) == ' ') {
				i++; 
			}
			command = command.substring(0,command.length() - i); 
			if(command.endsWith(";")) {
				value = run(command); 
			} else if (command.endsWith("}")) {
				value = run(command); 
			} else {
				value = run(command + ";");  
			}
			
			if(value != null) {
				if(value instanceof String) {
					System.out.println((String)value);  
				} else if(value instanceof Double) {
					System.out.println((double) value) ;
				} else if (value instanceof LoxInstance) {
					System.out.println(((LoxInstance)value).toString()); 
				} else if (value instanceof LoxFunction) {
					System.out.println(((LoxFunction)value).toString()); 
				} else if (value instanceof LoxClass) {
					System.out.println(((LoxClass)value).toString()); 
				} else if (value instanceof Boolean) {
					System.out.println((Boolean)value);
				}
			}
			hadError = false;
		}
	}
	
	/* brief explanation : 
	 * first the scanner take a string of a source code and produce a list of tokens which is a representation for every operator, literals and reversed words (check Token and TokenType classes).  .
	 * second the list of tokens is passed to the parser and it will produce a syntax tree -AST- . 
	 * in the syntax tree we have two kinds of nodes expression and statement ( check Expr and Stmt classes).
	 * we produce this classes using GenerateAST class (metaprogramming). 
	 * we have many tree nodes and every one is evaluated in a different way so we implement visitor pattern to add behavior easily to every class and hence every tree node implements Visitor interface . 
	 * next syntax trees are passed to the resolver which will bind local variables and reports static errors (like using return outside a fuction) . 
	 * finally we interpret the syntax trees (every tree node has a visit function that will evaluate that node).
	 * error handling : if there is a error in parsing or in resolving we stop there and report the error . 
	 * Runtime errors : errors produced in the interpreting stage will be a runtime errors and will stop the program . 
	 */
	private static Object run(String source) {	
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();
		Parser parser = new Parser(tokens);
		List<Stmt> statements = parser.parse();
		
		if(hadError) return null; 
		Resolver resolver = new Resolver(interpreter); 
	    resolver.resolve(statements); 
		
		if(hadError) return null; 
		
		return interpreter.interpret(statements);
	}
	
	//Error Handling 
	static void error(int line,String message) {
		report(line,"",message);
	}
	
	static void error(Token token,String message) {
		if(token.type == TokenType.EOF) {
			report(token.line," at end ",message); 
		} else {
			report(token.line, " at " + token.lexeme + " " , message); 
		}
	}
	
	static void runtimeError(RuntimeError error) {
		System.err.println(error.getMessage() + "\n[line " + error.token.line  + "]"); 
		hadRuntimeError = true; 
	}
	
	static void report(int line,String where,String message) {
		System.out.println("[line: " + line+"] Error:" + where + ": " + message);
		hadError = true;
	}
	
	
	
}
