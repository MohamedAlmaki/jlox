package lox;

public enum TokenType{
	//Single Character token
	LEFT_PAREN,RIGHT_PAREN,LEFT_BRACE,RIGHT_BRACE,COMMA,DOT,MINUS,PLUS,SEMICOLON,SLASH,STAR,BITWISE_OR,BITWISE_AND,XOR,
	
	//One or two Character token 
	BANG,BANG_EQUAL,
	EQUAL,EQUAL_EQUAL,
	GREATER,GREATER_EQUAL,
	LESS,LESS_EQUAL,
	COLON,QMARK,
	
	//Literals
	IDENTIFIER,STRING,NUMBER,
	
	//Keywords
	AND,CLASS,ELSE,FALSE,FUN,FOR,IF,NIL,OR,PRINT,RETURN,SUPER,THIS,TRUE,VAR,WHILE,EOF,BREAK,CONTINUE
	
}