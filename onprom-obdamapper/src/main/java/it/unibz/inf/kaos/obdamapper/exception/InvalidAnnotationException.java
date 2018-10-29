package it.unibz.inf.kaos.obdamapper.exception;

public class InvalidAnnotationException extends Exception{

	private static final long serialVersionUID = 1L;
	private String additionalMsg = "";
	
	public InvalidAnnotationException(String additionalMsg){
		this.additionalMsg = additionalMsg;
	}
	
	@Override
	public String getMessage() {
		//TODO still need to implement more informative message		 
		if(this.additionalMsg.equals(""))
			return "Invalid Annotation";

		return "Invalid Annotation\n"+this.additionalMsg;
	}
}
