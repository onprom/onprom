package it.unibz.inf.kaos.obdamapper.ontopext;

import java.util.HashMap;

/**
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class SQLWithVarMap {

	private String sql;
	private HashMap<String, String> variableMap; //a mapping between answer variables to URI Template
	
	SQLWithVarMap(String SQL, HashMap<String, String> variableMap){
		this.sql = SQL;
		this.variableMap = variableMap;
	}
	
	public String getSQL() {
		return sql;
	}
	public void setSQL(String sQL) {
		sql = sQL;
	}
	public HashMap<String, String> getVariableMap() {
		return variableMap;
	}
	public void setVariableMap(HashMap<String, String> variableMap) {
		this.variableMap = variableMap;
	}
	
	
}
