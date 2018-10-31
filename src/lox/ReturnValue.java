package lox;

@SuppressWarnings("serial")
public class ReturnValue extends RuntimeException {
	public Object value; 

	ReturnValue(Object value){
		super(null,null,false,false); 
		this.value = value; 
	}
}
