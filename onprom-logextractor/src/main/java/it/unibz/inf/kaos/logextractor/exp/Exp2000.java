package it.unibz.inf.kaos.logextractor.exp;

/**
 * OnProm experiment using synthetic conference database
 * 
 * 
 * @author Ario Santoso (santoso.ario@gmail.com)
 */
class Exp2000 extends Exp{
	
	private static String codeName = "exp2000";
	private static String obdaFile = "conference2000.obda";

	//prevent the instantiation of the class Exp
	protected Exp2000(String configFile) throws Exception{
		
		super(configFile, codeName, obdaFile);
	}
	
	
	public static void main(String[] ar){
						
		try{
			Exp2000 e = new Exp2000(ar[0]);
			e.extractXESLog(System.out);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
