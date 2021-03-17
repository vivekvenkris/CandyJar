package exceptions;

public class InvalidInputException extends Exception{
	
	String message;
	
	
	public InvalidInputException(String message) {
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
	
	

}
