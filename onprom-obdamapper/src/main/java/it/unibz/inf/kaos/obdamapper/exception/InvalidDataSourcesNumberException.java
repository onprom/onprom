package it.unibz.inf.kaos.obdamapper.exception;

public class InvalidDataSourcesNumberException extends Exception{

	private static final long serialVersionUID = 1L;
	private int numOfDataSources = 1;
	
	public InvalidDataSourcesNumberException(int numOfDataSources){
		this.numOfDataSources = numOfDataSources;
	}
	
	@Override
	public String getMessage() {
		//TODO still need to implement more informative message		 
		return "We don't support an OBDA Model with "+numOfDataSources+ " data source(s)\n";
	}
}
