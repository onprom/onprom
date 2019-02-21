package it.unibz.inf.kaos.logextractor.exp;

/**
 * OnProm experiment using synthetic conference database
 * 
 * 
 * @author Ario Santoso (santoso.ario@gmail.com)
 */
class Exp1000 extends Exp{
	
	private static String codeName = "exp1000";
	private static String obdaFile = "conference1000.obda";

	//prevent the instantiation of the class Exp
	protected Exp1000(String configFile) throws Exception{
		
		super(configFile, codeName, obdaFile);
	}
	
	
	public static void main(String[] ar){
						
		try{
			Exp1000 e = new Exp1000(ar[0]);
			e.extractXESLog(System.out);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
