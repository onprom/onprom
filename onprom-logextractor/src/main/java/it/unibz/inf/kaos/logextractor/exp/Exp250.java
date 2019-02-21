package it.unibz.inf.kaos.logextractor.exp;

/**
 * OnProm experiment using synthetic conference database
 * 
 * 
 * @author Ario Santoso (santoso.ario@gmail.com)
 */
class Exp250 extends Exp{
	
	private static String codeName = "exp250";
	private static String obdaFile = "conference250.obda";

	//prevent the instantiation of the class Exp
	protected Exp250(String configFile) throws Exception{
		
		super(configFile, codeName, obdaFile);
	}
	
	
	public static void main(String[] ar){
						
		try{
			Exp250 e = new Exp250(ar[0]);
//			Exp250 e = new Exp250("");
			e.extractXESLog(System.out);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
