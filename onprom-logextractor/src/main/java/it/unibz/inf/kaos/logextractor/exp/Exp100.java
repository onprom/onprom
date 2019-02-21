package it.unibz.inf.kaos.logextractor.exp;

/**
 * OnProm experiment using synthetic conference database
 * 
 * 
 * @author Ario Santoso (santoso.ario@gmail.com)
 */
class Exp100 extends Exp{
	
	private static String codeName = "exp100";
	private static String obdaFile = "conference100.obda";

	//prevent the instantiation of the class Exp
	protected Exp100(String configFile) throws Exception{
		
		super(configFile, codeName, obdaFile);
	}
	
	
	public static void main(String[] ar){
						
		try{
			Exp100 e = new Exp100(ar[0]);
//			Exp100 e = new Exp100("");
			e.extractXESLog(System.out);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
