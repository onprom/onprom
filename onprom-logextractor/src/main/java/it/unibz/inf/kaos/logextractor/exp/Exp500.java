package it.unibz.inf.kaos.logextractor.exp;

/**
 * OnProm experiment using synthetic conference database
 * 
 * 
 * @author Ario Santoso (santoso.ario@gmail.com)
 */
class Exp500 extends Exp{
	
	private static String codeName = "exp500";
	private static String obdaFile = "conference500.obda";

	//prevent the instantiation of the class Exp
	protected Exp500(String configFile) throws Exception{
		
		super(configFile, codeName, obdaFile);
	}
	
	
	public static void main(String[] ar){
						
		try{
			Exp500 e = new Exp500(ar[0]);
			e.extractXESLog(System.out);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
