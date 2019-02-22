package it.unibz.inf.kaos.logextractor.exp;

/**
 * OnProm experiment using synthetic conference database
 * 
 * 
 * @author Ario Santoso (santoso.ario@gmail.com)
 */
class Exp50 extends Exp{
	
	private static String codeName = "exp50";
	private static String obdaFile = "conference50.obda";

	//prevent the instantiation of the class Exp
	protected Exp50(String configFile) throws Exception{
		
		super(configFile, codeName, obdaFile);
	}
	
	
	public static void main(String[] ar){
						
		try{
			Exp50 e = new Exp50(ar[0]);
//			Exp50 e = new Exp50("");
			e.extractXESLog(System.out);
//			e.extractXESLogAndMappings(System.out);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
