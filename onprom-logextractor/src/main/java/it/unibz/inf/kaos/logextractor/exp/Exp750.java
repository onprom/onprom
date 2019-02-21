package it.unibz.inf.kaos.logextractor.exp;

/**
 * OnProm experiment using synthetic conference database
 * 
 * 
 * @author Ario Santoso (santoso.ario@gmail.com)
 */
class Exp750 extends Exp{
	
	private static String codeName = "exp750";
	private static String obdaFile = "conference750.obda";

	//prevent the instantiation of the class Exp
	protected Exp750(String configFile) throws Exception{
		
		super(configFile, codeName, obdaFile);
	}
	
	
	public static void main(String[] ar){
						
		try{
			Exp750 e = new Exp750(ar[0]);
			e.extractXESLog(System.out);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
