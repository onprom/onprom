package it.unibz.inf.kaos.logextractor.exp;

/**
 * OnProm experiment using synthetic conference database
 * 
 * 
 * @author Ario Santoso (santoso.ario@gmail.com)
 */
class Exp10 extends Exp{
	
	private static String codeName = "exp10";
	private static String obdaFile = "conference10.obda";

	//prevent the instantiation of the class Exp
	protected Exp10(String configFile) throws Exception{
		
		super(configFile, codeName, obdaFile);
	}
	
	
	public static void main(String[] ar){
						
		try{
//			Exp10 e = new Exp10(ar[0]);
			Exp10 e = new Exp10("");
			e.extractXESLog(System.out);
//			e.extractXESLogAndMappings(System.out);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
