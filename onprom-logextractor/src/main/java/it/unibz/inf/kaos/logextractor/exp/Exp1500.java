package it.unibz.inf.kaos.logextractor.exp;

/**
 * OnProm experiment using synthetic conference database
 * 
 * 
 * @author Ario Santoso (santoso.ario@gmail.com)
 */
class Exp1500 extends Exp{
	
	private static String codeName = "exp1500";
	private static String obdaFile = "conference1500.obda";

	//prevent the instantiation of the class Exp
	protected Exp1500(String configFile) throws Exception{
		
		super(configFile, codeName, obdaFile);
	}
	
	
	public static void main(String[] ar){
						
		try{
			Exp1500 e = new Exp1500(ar[0]);
			e.extractXESLog(System.out);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
