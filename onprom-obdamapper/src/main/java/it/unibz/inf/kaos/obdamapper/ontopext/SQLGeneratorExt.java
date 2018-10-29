/*
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibz.inf.kaos.obdamapper.ontopext;

import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.DatalogNormalizer;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.EQNormalizer;
import it.unibz.inf.ontop.owlrefplatform.core.queryevaluation.SQLDialectAdapter;
import it.unibz.inf.ontop.owlrefplatform.core.sql.SQLGenerator;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.SQLQueryGenerator;
import it.unibz.inf.ontop.sql.DBMetadata;
import org.openrdf.model.Literal;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is based on "it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.SQLQueryGenerator"
 * of Ontop version 1.18 (see https://github.com/ontop/ontop).
 * 
 * Modified by Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 */
public class SQLGeneratorExt extends SQLGenerator implements SQLQueryGenerator {

	private static final Logger logger = Logger.getLogger(SQLGeneratorExt.class.getCanonicalName());
	private static String logMsgTemplate = "DEBUGA: \n\t%s";

	private static final long serialVersionUID = 1684192893296440455L;

	//arsa: modif SQL Generator 
	//(modify view name to avoid clashes with Ontop reserved keyword)
	private static final String VIEW_NAME = "ZXCVB%s";
	private static final String VIEW_NAME_PREFIX = "ZXCVB";

	//unique variable name generator
	private static int varCounter = 1;
	private static final String varNameTemplate = "QWERT%s";
	//arsa: END OF modif SQL Generator

	private boolean isDistinct = false;
	private boolean isOrderBy = false;
	private final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();

	/**
	 * 
	 * @param metadata
	 * @param sqladapter
	 * @param sqlGenerateReplace
	 * @param uriid is null in case we are not in the SI mode
	 */
	public SQLGeneratorExt(DBMetadata metadata, SQLDialectAdapter sqladapter, boolean sqlGenerateReplace, boolean distinctResultSet, SemanticIndexURIMap uriid) {
		
		super(metadata, sqladapter, sqlGenerateReplace, distinctResultSet, uriid);
		
		SQLGenerator.setVIEW_NAME(SQLGeneratorExt.VIEW_NAME);
		SQLGenerator.setVIEW_NAME_PREFIX(SQLGeneratorExt.VIEW_NAME_PREFIX);
		
		this.setVerboseMode(false);
	}

	public List<SQLWithVarMap> generateSourceQueries(DatalogProgram query, List<String> signature) throws OBDAException {

		return generateSourceQueries(query, signature, true, false);
	}

	public List<SQLWithVarMap> generateSourceQueries(DatalogProgram query, List<String> signature, boolean removeIsNotNULL, boolean forceDistinct) throws OBDAException {

		
		isDistinct = hasSelectDistinctStatement(query);
		isOrderBy = hasOrderByClause(query);
		
		if (query.getQueryModifiers().hasModifiers()) {
			
			final String indent = "   ";
			//final String outerViewName = "SUB_QVIEW";
			List<SQLWithVarMap> subquery = generateQuery2(query, signature, indent, removeIsNotNULL, forceDistinct);

			logger.info(
				String.format(logMsgTemplate, "\n\nNote: we don't support query with modifiers such as LIMIT/OFFSET/ORDERBY. \nHence, we ignore those modifiers\n"));

			return subquery;
			
			// arsa: butuh cek ini nanti
			// kyanya cuma perlu append "modifier" ke setiap generated SQL
			
			/* NOTE: arsa - 2017.03.18
			 * - We can't support modifier because we split the UNION
			 * - We also can't propagate them into inside the query because putting LIMIT/OFFSET/ORDER BY to the result of 
			 *   a UNION of SELECT query is not the same as putting LIMIT/OFFSET/ORDER BY to each query inside the UNION.
			 * - 
			 */

//			String modifier = "";
//			List<OrderCondition> conditions = query.getQueryModifiers().getSortConditions();
//			long limit = query.getQueryModifiers().getLimit();
//			long offset = query.getQueryModifiers().getOffset();
//			modifier = sqladapter.sqlOrderByAndSlice(conditions,outerViewName,limit, offset) + "\n";
//
//			String sql = "SELECT *\n";
//			sql += "FROM (\n";
//			sql += subquery + "\n";
//			sql += ") " + outerViewName + "\n";
//			sql += modifier;
//			
//			return sql;
			//arsa: END OF butuh cek ini nanti
			
		} else {
			return generateQuery2(query, signature, "", removeIsNotNULL, forceDistinct);
		}
	}

	private List<SQLWithVarMap> generateQuery2(DatalogProgram query, List<String> signature, String indent, boolean removeIsNotNULL, boolean forceDistinct) throws OBDAException {

		int numberOfQueries = query.getRules().size();

		List<SQLWithVarMap> queriesStrings = new LinkedList<>();
		/* Main loop, constructing the SPJ query for each CQ */
		for (CQIE cq : query.getRules()) {

			/*
			 * Here we normalize so that the form of the CQ is as close to the
			 * form of a normal SQL algebra as possible, particularly, no shared
			 * variables, only joins by means of equality. Also, equalities in
			 * nested expressions (JOINS) are kept at their respective levels to
			 * generate correct ON and wHERE clauses.
			 */
//			log.debug("Before pushing equalities: \n{}", cq);

			EQNormalizer.enforceEqualities(cq);

//			log.debug("Before folding Joins: \n{}", cq);

			DatalogNormalizer.foldJoinTrees(cq);

//			log.debug("Before pulling out equalities: \n{}", cq);
			
			DatalogNormalizer.pullOutEqualities(cq);
			
//			log.debug("Before pulling out Left Join Conditions: \n{}", cq);
			
			DatalogNormalizer.pullOutLeftJoinConditions(cq);
			
//			log.debug("Before pulling up nested references: \n{}", cq);

			DatalogNormalizer.pullUpNestedReferences(cq);

//			log.debug("Before adding trivial equalities: \n{}, cq);", cq);

			DatalogNormalizer.addMinimalEqualityToLeftJoin(cq);

//			log.debug("Normalized CQ: \n{}", cq);

			Predicate headPredicate = cq.getHead().getFunctionSymbol();
			if (!headPredicate.getName().equals(OBDAVocabulary.QUEST_QUERY)) {
				// not a target query, skip it.
				continue;
			}

			QueryAliasIndex index = new QueryAliasIndex(cq);

			boolean innerdistincts = false;
			if (isDistinct && !this.hasDistinctResultSet() && numberOfQueries == 1) {
				innerdistincts = true;
			}

			//arsa: note: perlu ngelakuin sesuatu terkait distinct buat di push ke dalem
			
			//[arsa modify 2017.03.18]
			//since we split the UNION, we need to add the following in order to propagate the DISTINCT into inner query
			//Note that this case is not covered by the previous (above) check
			if (isDistinct && !this.hasDistinctResultSet()) {
				innerdistincts = true;
			}
			
			//if forceDistinct == true, then we force each generated query to use DISTINCT
			if(forceDistinct){
				innerdistincts = true;				
			}
			//[END OF arsa modify 2017.03.18]

			logger.info(String.format(logMsgTemplate, "Generate SELECT \n"));
			logger.info(String.format(logMsgTemplate, "Signature: "+signature));
			logger.info(String.format(logMsgTemplate, "cq: "+cq));
			logger.info(String.format(logMsgTemplate, "index: "+ index));
			logger.info(String.format(logMsgTemplate, "innerdistincts: "+ innerdistincts));
			
			//arsa: modify concat
			//String SELECT = getSelectClause2(signature, cq, index, innerdistincts);
			ObjectPair<String, HashMap<String,String>> selectClause = getSelectClause2(signature, cq, index, innerdistincts);
			String SELECT = cleanQuotes(selectClause.getFirstComponent());
			//arsa: END OF modify concat

			logger.info(String.format(logMsgTemplate, "String SELECT: \n"+SELECT+"\n"));

			String FROM = getFROM2(cq, index);

			logger.info(String.format(logMsgTemplate, "String FROM: \n"+FROM+"\n"));

			String WHERE = getWHERE2(cq, index, removeIsNotNULL);

			logger.info(String.format(logMsgTemplate, "String WHERE: \n"+WHERE+"\n"));

			String querystr = "";
			
			//[arsa modify 2017.03.18]
			// just to handle the case where there is no WHERE condition in the query
			if(WHERE.equalsIgnoreCase(""))
				querystr = "\t\t"+ SELECT + "\n" + FROM;
			else
				querystr = "\t\t"+ SELECT + "\n" + FROM + "\n" + WHERE;
			//[END OF arsa modify 2017.03.18]

			//arsa: modify concat
			//queriesStrings.add(querystr);
			queriesStrings.add(new SQLWithVarMap(querystr, selectClause.getSecondComponent()));
			
			logger.info(String.format(logMsgTemplate, "SQL: \n"+querystr+"\n"));

			logger.info(String.format(logMsgTemplate, "Variable Map:"));
			Iterator<String> it = selectClause.getSecondComponent().keySet().iterator();
			while(it.hasNext()){
				String key = it.next();
				logger.info(String.format(logMsgTemplate, "\t Var map - Key: "+key+", "
									+ "Value: "+selectClause.getSecondComponent().get(key)));				
			}
			logger.info(String.format(logMsgTemplate, "END OF Variable Map \n"));
			//arsa: END OF modify concat
			
		}

		return queriesStrings;
		
		/*
		//arsa: I removed the following codes in order to split UNION
		//arsa: TODO: check how do we handle distinct (since we drop UNION ALL). I guess we need to push it inside
		Iterator<String> queryStringIterator = queriesStrings.iterator();
		StringBuilder result = new StringBuilder();
		if (queryStringIterator.hasNext()) {
			result.append(queryStringIterator.next());
		}

		//lakuin sesuatu di sini buat mecah UNION. Pada dasarnya dia process tiap rule di Datalog Program trus digabung pake UNION, nah diganti jadi set of query aja
		
		
		String UNION;
		if (isDistinct && !distinctResultSet) {
			UNION = "\nUNION\n";
		} else {
			UNION = "\nUNION ALL\n";
		}
		while (queryStringIterator.hasNext()) {
			result.append(UNION);
			result.append(queryStringIterator.next());
		}

		return result.toString();
		//arsa: END OF I removed the following codes in order to split UNION
		*/
		
	}
		
	private ObjectPair<String, HashMap<String,String>> getSelectClause2(List<String> signature, CQIE query,
			QueryAliasIndex index, boolean distinct) throws OBDAException {

		logger.info(String.format(logMsgTemplate, "getSelectClause2: "+query));

		HashMap<String, String> varMap = new HashMap<String, String>();

		/*
		 * If the head has size 0 this is a boolean query.
		 */
		List<Term> headterms = query.getHead().getTerms();
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT \n");
		if (distinct && !this.hasDistinctResultSet()) {
			sb.append("DISTINCT ");
		}
		//Only for ASK
		if (headterms.size() == 0) {
			sb.append("'true' as x");
			
			//arsa: modify concat
			//return sb.toString();
			varMap.put("x", "'true'");
			return new ObjectPair<String, HashMap<String, String>>(sb.toString(), varMap);
			//arsa: END OF modify concat
		}

		/**
		 * Set that contains all the variable names created on the top query.
		 * It helps the dialect adapter to generate variable names according to its possible restrictions.
		 * Currently, this is needed for the Oracle adapter (max. length of 30 characters).
		 */
		Set<String> sqlVariableNames = new HashSet<>();

		Iterator<Term> hit = headterms.iterator();
		int hpos = 0;
		String mainColumn = "";
		
		//loop for each selected variable
		while (hit.hasNext()) {
			
			Term ht = hit.next();
			//arsa:remove type and lang answer variables
			//String typeColumn = getTypeColumnForSELECT(ht, signature, hpos, sqlVariableNames);
			//String langColumn = getLangColumnForSELECT(ht, signature, hpos,	index, sqlVariableNames);
			//arsa:END OF remove type and lang answer variables

			//arsa: modify concat
			//String mainColumn = getMainColumnForSELECT2(ht, signature, hpos, index, sqlVariableNames);
			
			ObjectPair<String, HashMap<String,String>> mainColumnForSELECT = getMainColumnForSELECT2(ht, signature, hpos, index, sqlVariableNames);

			mainColumn = mainColumnForSELECT.getFirstComponent();
			
			logger.info(String.format(logMsgTemplate, "## main column: "+mainColumn));
			
			if(mainColumnForSELECT.getSecondComponent() != null)
				varMap.putAll(mainColumnForSELECT.getSecondComponent());
			
			//arsa: END OF modify concat

			//sb.append("\n   ");
			
			//arsa:remove type and lang answer variables
			//sb.append(typeColumn);
			//sb.append(", ");
			//sb.append(langColumn);
			//sb.append(", ");
			//arsa:END OF remove type and lang answer variables
			
			if(!mainColumn.equals("")){
				sb.append(mainColumn);
				if (hit.hasNext()) {
					sb.append(", ");
				}
			}
			
			//System.out.println("sb: "+sb);
			
			hpos++;
		}
		
		//if there is nothing to be concatenated (e.g., the case where the answer variable is bind to constant)
		if(mainColumn.equals(""))
			sb.delete(sb.length()-2, sb.length()-1);

		
		//arsa: modify concat
		//return sb.toString();
		return new ObjectPair<String, HashMap<String, String>>(sb.toString(), varMap);
		//arsa: END OF modify concat
	}

	private ObjectPair<String, HashMap<String,String>> getMainColumnForSELECT2(Term ht,
			List<String> signature, int hpos, QueryAliasIndex index, Set<String> sqlVariableNames) {

		logger.info(String.format(logMsgTemplate, "getMainColumnForSELECT2: Term => "+ht));

		//arsa: modify concat - purpose: prevent concatenation in the generated SQL (solution: make the string to be concatenated as a URI template)
		//arsa: this HashMap newVarMap will contain the mapping between 
		//		an answer variable name and the URI Template for instantiating the corresponding answer variable
		HashMap<String, String> newVarMap = new HashMap<String, String>();
		boolean buildAlias = true;
		//arsa: END OF modify concat
		
		
		/**
		 * Creates a variable name that fits to the restrictions of the SQL dialect.
		 */
		//arsa: modify var name
		String variableNameOri = signature.get(hpos);
		//String variableName = sqladapter.nameTopVariable(signature.get(hpos), "", sqlVariableNames);
		String variableName = variableNameOri;
		//arsa: END OF modify var name
		sqlVariableNames.add(variableName);

		String mainColumn = null;

		String mainTemplate = "%s AS %s";

		if (ht instanceof URIConstant) {
			logger.info(String.format(logMsgTemplate, "ht ("+ht+") is instance of URIConstant"));
			
			URIConstant uc = (URIConstant) ht;
			mainColumn = this.getSqladapter().getSQLLexicalFormString(uc.getURI());
		} 
		else if (ht == OBDAVocabulary.NULL) {
			logger.info(String.format(logMsgTemplate, "ht ("+ht+") is OBDAVocabulary.NULL"));

			mainColumn = "NULL";
		} 
		else if (ht instanceof Function) {
			logger.info(String.format(logMsgTemplate, "ht ("+ht+") is instance of Function"));
			/*
			 * if it's a function we need to get the nested value if its a
			 * datatype function or we need to do the CONCAT if its URI(....).
			 */
			Function ov = (Function) ht;
			Predicate function = ov.getFunctionSymbol();

			/*
			 * Adding the column(s) with the actual value(s)
			 */
			if (ov.isDataTypeFunction()) {
				logger.info(String.format(logMsgTemplate, "ov ("+ov+") is Data Type Function"));
				/*
				 * Case where we have a typing function in the head (this is the
				 * case for all literal columns
				 */
				int size = ov.getTerms().size();
				if ((function instanceof Literal) || size > 2 ) {
					logger.info(String.format(logMsgTemplate, "function ("+function+") is instanceof Literal"));
					
					//arsa: modif concat
					//mainColumn = getSQLStringForTemplateFunction2(ov, index);
					ObjectPair<String,String> sqlForTemplateFunc = getSQLStringForTemplateFunction2(ov, index);
					mainColumn = sqlForTemplateFunc.getFirstComponent();
					
					if(sqlForTemplateFunc.getSecondComponent() != null){
						newVarMap.put(variableNameOri, sqlForTemplateFunc.getSecondComponent());
						buildAlias = false;
					}
					//arsa: END OF modif concat
					
				}
				else {
					Term term = ov.getTerms().get(0);
					if (term instanceof ValueConstant) {
						//arsa: push constant to target query
						
						logger.info(String.format(logMsgTemplate, "term ("+term+") is instanceof ValueConstant"));
						//mainColumn = getSQLLexicalForm((ValueConstant) term);
						newVarMap.put(variableNameOri, ((ValueConstant) term).getValue());
						mainColumn = "";
						buildAlias = false;
						logger.info(String.format(logMsgTemplate, mainColumn));
						
						//arsa: END OF push constant to target query
					} else {
						mainColumn = getSQLString(term, index, false);
					}
				}
			}
			else if (function instanceof URITemplatePredicate) {
				logger.info(String.format(logMsgTemplate, "function ("+function+") is URI Template Predicate"));
				// New template based URI building functions
				
				//arsa: modify concat
				//mainColumn = getSQLStringForTemplateFunction2(ov, index);
				ObjectPair<String,String> sqlForTemplateFunc = getSQLStringForTemplateFunction2(ov, index);
				mainColumn = sqlForTemplateFunc.getFirstComponent();
				
				if(sqlForTemplateFunc.getSecondComponent() != null){
					newVarMap.put(variableNameOri, sqlForTemplateFunc.getSecondComponent());
					buildAlias = false;
				}
				//arsa: END OF modify concat
			}
			else if (function instanceof BNodePredicate) {
				logger.info(String.format(logMsgTemplate, "function ("+function+") is BNodePredicate"));
				// New template based BNODE building functions
				
				//arsa: modif concat
				//mainColumn = getSQLStringForTemplateFunction2(ov, index);
				ObjectPair<String,String> sqlForTemplateFunc = getSQLStringForTemplateFunction2(ov, index);
				mainColumn = sqlForTemplateFunc.getFirstComponent();
				if(sqlForTemplateFunc.getSecondComponent() != null){
					newVarMap.put(variableNameOri, sqlForTemplateFunc.getSecondComponent());
					buildAlias = false;
				}
				//arsa: END OF modif concat
			}
			else if (ov.isOperation()) {
				logger.info(String.format(logMsgTemplate, "ov ("+ov+")  isOperation"));
				mainColumn = getSQLString(ov, index, false); 
			}
            else 
				throw new IllegalArgumentException("Error generating SQL query. Found an invalid function during translation: " + ov);
		} 
		else 
			throw new RuntimeException("Cannot generate SELECT for term: " + ht);

		
		logger.info(String.format(logMsgTemplate, 
						"\n------------------------------------------\n"+
						"mainColumn: "+mainColumn+"\n"+
						"variableName: "+variableName+"\n"+
						"------------------------------------------"
				));
		
		/*
		 * If we have a column we need to still CAST to VARCHAR
		 */
		//arsa: remove CAST
		//if (mainColumn.charAt(0) != '\'' && mainColumn.charAt(0) != '(') {
			//if (!isStringColType(ht, index)) {
			//	mainColumn = sqladapter.sqlCast(mainColumn, Types.VARCHAR);
			//}
		//}
		//arsa: END OF remove CAST

		//arsa: modify concat
		//return String.format(mainTemplate, mainColumn, variableName);
		
		// the normal case where the main column simply an answer variable
		if(buildAlias){
			newVarMap.put(variableNameOri, "{"+variableName+"}");
			return new ObjectPair<String, HashMap<String, String>>(
					String.format(mainTemplate, mainColumn, variableName), newVarMap);
		}
		
		//the case where the mainColumn needs to be concatenated into URI or is a constant
		return new ObjectPair<String, HashMap<String, String>>(mainColumn, newVarMap);
		//arsa: END OF modify concat
	}

    private ObjectPair<String,String> getSQLStringForTemplateFunction2(Function ov, QueryAliasIndex index) {
		/*
		 * The first inner term determines the form of the result
		 */
		Term t = ov.getTerms().get(0);
		
		SQLDialectAdapter sqladapter = this.getSqladapter();
	
		if (t instanceof ValueConstant || t instanceof BNode) {
			
			logger.info(String.format(logMsgTemplate, t+" is instanceof: ValueConstant || BNode"));
			
			/*
			 * The function is actually a template. The first parameter is a
			 * string of the form http://.../.../ or empty "{}" with place holders of the form
			 * {}. The rest are variables or constants that should be put in
			 * place of the place holders. We need to tokenize and form the
			 * CONCAT
			 */
			String literalValue;			
			if (t instanceof BNode) {
				literalValue = ((BNode) t).getName();
			} else {
				literalValue = ((ValueConstant) t).getValue();
			}		


            String template = trimLiteral(literalValue);
            
			String[] split = template.split("[{][}]");
			
			List<String> vex = new LinkedList<>();
			if (split.length > 0 && !split[0].isEmpty()) {
				
				//arsa: remove concat
				logger.info(String.format(logMsgTemplate, "split after getSQLlex: "+sqladapter.getSQLLexicalFormString(split[0])+", split ori: "+split[0]));
//				vex.add(sqladapter.getSQLLexicalFormString(split[0]));
				vex.add(split[0]);
				//arsa: END OF remove concat
			}
			
			/*
			 * New we concat the rest of the function, note that if there is only 1 element
			 * there is nothing to concatenate
			 */
			if (ov.getTerms().size() > 1) {
				int size = ov.getTerms().size();
				Predicate pred = ov.getFunctionSymbol();
				if (dtfac.isLiteral(pred)) {
					size--;
				}
				for (int termIndex = 1; termIndex < size; termIndex++) {
					Term currentTerm = ov.getTerms().get(termIndex);
					String repl;

					if (isStringColType(currentTerm, index)) {
						//empty place holders: the correct uri is in the column of DB no need to replace
						if(split.length == 0)
						{
							repl = getSQLString(currentTerm, index, false) ;
						}
						else
						{
							//repl = replace1 + (getSQLString(currentTerm, index, false)) + replace2;
							repl = (getSQLString(currentTerm, index, false));
						}

					} else {
						if(split.length == 0)
						{
							//arsa: remove CAST
							//repl = sqladapter.sqlCast(getSQLString(currentTerm, index, false), Types.VARCHAR) ;
							repl = getSQLString(currentTerm, index, false) ;
							//arsa: END OF remove CAST
						}
						else {
							//arsa: remove CAST
							//repl = replace1 + sqladapter.sqlCast(getSQLString(currentTerm, index, false), Types.VARCHAR) + replace2;
							repl = getSQLString(currentTerm, index, false);

							//arsa: END OF remove CAST
						}
					}
					logger.info(String.format(logMsgTemplate, "repl: "+repl));
					vex.add(repl);
					if (termIndex < split.length) {

						//arsa: remove concat
						logger.info(String.format(logMsgTemplate, "split2 after getSQLlex: "+sqladapter.getSQLLexicalFormString(split[termIndex])+
								", split2 ori: "+split[termIndex]));

						//vex.add(sqladapter.getSQLLexicalFormString(split[termIndex]));
						vex.add(split[termIndex]);
						//arsa: remove concat
					}
				}
			}
		
			if (vex.size() == 1) {	
				//arsa: modify concat
				//return vex.get(0);
				
				//arsa: modify - bug fix - 2017.08.07 - handling the situation when there is nothing to be concatenated
				
				//return new ObjectPair<String,String>(vex.get(0), null);
				
				if(!vex.get(0).equals("{}"))
					return new ObjectPair<String,String>("'"+vex.get(0)+"'", null);
				else
					return new ObjectPair<String,String>(vex.get(0), null);
				//arsa: END OF modify - bug fix - 2017.08.07
				
				//arsa: END OF modify concat
			}

			return getStringConcatenation2(vex.toArray(new String[]{}));
		} 
		else if (t instanceof Variable) {
			
			logger.info(String.format(logMsgTemplate, t+" is instanceof: Variable"));

			/*
			 * The function is of the form uri(x), we need to simply return the
			 * value of X
			 */
			//arsa: remove CAST
			//return sqladapter.sqlCast(getSQLString(t, index, false), Types.VARCHAR);
			return new ObjectPair<String,String>(getSQLString(t, index, false), null);
			//arsa: END OF remove CAST
			
		} 
		else if (t instanceof URIConstant) {
			
			logger.info(String.format(logMsgTemplate, t+" is instanceof URI Constant"));

			/*
			 * The function is of the form uri("http://some.uri/"), i.e., a
			 * concrete URI, we return the string representing that URI.
			 */
			URIConstant uc = (URIConstant) t;
			return new ObjectPair<String,String>(sqladapter.getSQLLexicalFormString(uc.getURI()), null);
		}
		else if (t instanceof Function) {
			
			logger.info(String.format(logMsgTemplate, t+" is instanceof Function"));

			/*
			 * The function is of the form uri(CONCAT("string",x)),we simply return the value from the database.
			 */
			//arsa: remove CAST
			//return sqladapter.sqlCast(getSQLString(t, index, false), Types.VARCHAR);
			return new ObjectPair<String,String>(getSQLString(t, index, false), null);
			//arsa: END OF remove CAST
		}

		/*
		 * Unsupported case
		 */
		throw new IllegalArgumentException("Error, cannot generate URI constructor clause for a term: " + ov);

	}

	private ObjectPair<String,String> getStringConcatenation2(String[] params) {
		
		logger.info(String.format(logMsgTemplate, ":-------------------------------------------"));
		logger.info(String.format(logMsgTemplate, "private String getStringConcatenation2(String[] params)"));

		StringBuilder sb= new StringBuilder();
		for(int ii = 0; ii < params.length; ii++)
			sb.append(params[ii]+", ");

		logger.info(String.format(logMsgTemplate, "params: "+ sb));
		
		
//		String toReturn = sqladapter.strConcat(params);
//		if (sqladapter instanceof DB2SQLDialectAdapter) {
//			/*
//			 * A work around to handle DB2 (>9.1) issue SQL0134N: Improper use of a string column, host variable, constant, or function name.
//			 * http://publib.boulder.ibm.com/infocenter/db2luw/v9r5/index.jsp?topic=%2Fcom.ibm.db2.luw.messages.sql.doc%2Fdoc%2Fmsql00134n.html
//			 */
//			if (isDistinct || isOrderBy) {
//				//arsa: remove CAST
//				//return sqladapter.sqlCast(toReturn, Types.VARCHAR);
//				return toReturn;
//				//arsa: END OF remove CAST
//			}
//		}

		StringBuffer URITemplate = new StringBuffer();
		HashMap<String,String> ansVarSet = new HashMap<String,String>();// just to check whether it already contains a particular answer variable
		StringBuffer ansVar = new StringBuffer();
		boolean addComma = false;
		
		for(int ii = 0; ii < params.length; ii++){
			String curStr = params[ii];
			
			//if ((curStr.substring(0, this.VIEW_NAME_PREFIX.length())).equalsIgnoreCase(this.VIEW_NAME_PREFIX))

			
			String curStrUC = curStr.toUpperCase();
			if (curStrUC.matches(VIEW_NAME_PREFIX+".*")){ 
				//if the current string to concatenate is an answer variable
			
				String aliasName;
				if(ansVarSet.containsKey(curStr))
					aliasName = ansVarSet.get(curStr);
				else{
					aliasName = getUniqueVarName();
					ansVarSet.put(curStr, aliasName);
				}
					
				URITemplate.append(String.format("{%s}", aliasName));

				if(addComma){
					if(ansVarSet.containsKey(curStr))//check if it is already added to the select clause
						ansVar.append(String.format(", %s", curStr+" AS "+aliasName));
				}else{
					if(ansVarSet.containsKey(curStr)){ //check if it is already added to the select clause
						ansVar.append(curStr+" AS "+aliasName);
						addComma = true;
					}
				}
				
			}else URITemplate.append(curStr);
		}

		logger.info(String.format(logMsgTemplate, "String Answer Variable: "+ansVar));
		logger.info(String.format(logMsgTemplate, "String URITemplate: "+URITemplate));
		
		logger.info(String.format(logMsgTemplate, ":-------------------------------------------"));

		//toReturn.append(sqladapter.strConcat(params));
		//return toReturn.toString();
		//return ansVar.toString();
		return new ObjectPair<String, String>(ansVar.toString(), this.cleanQuotes(URITemplate.toString()));
	}

	private String getFROM2(CQIE query, QueryAliasIndex index) {
		String tableDefinitions = getTableDefinitions(query.getBody(), index, true, false, "");
		tableDefinitions = tableDefinitions.replaceAll("\n", " ");
		return "FROM \n" + tableDefinitions;
	}

	private String getWHERE2(CQIE query, QueryAliasIndex index, boolean removeIsNotNULL) {
		
		String conditions;
		
		//arsa: modify where - remove IS NOT NULL
		if(removeIsNotNULL){
			List<Function> body = query.getBody();
			List<Function> newBody = new ArrayList<Function>(); 
			
			for(int ii = 0; ii <  body.size(); ii++){
				Function f = body.get(ii);
				if(!f.getFunctionSymbol().getName().equals("IS_NOT_NULL"))
					newBody.add(f);
			}
	
			conditions = getConditionsString(newBody, index, false, "");
		}else{
			conditions = getConditionsString(query.getBody(), index, false, "");
		}
		//arsa: END OF modify where - remove IS NOT NULL

		if (conditions.length() == 0) {
			return "";
		}
		
		//conditions = conditions.replaceAll("\n", " ");

		return "WHERE \n" + conditions;
	}
	
	private String cleanQuotes(String str){
		//str = str.replaceAll("'", "");
		str = str.replaceAll("`", "");
		
		return str;
	}

	private String getUniqueVarName(){
		
		return String.format(varNameTemplate, varCounter++);
	}

	private class ObjectPair<X,Y> {
	    private X x;
	    private Y y;

	    public ObjectPair(X x, Y y) {
	        this.x = x;
	        this.y = y;
	    }

		public X getFirstComponent() {
			return x;
		}

		public Y getSecondComponent() {
			return y;
		}
	}

	///////////////////////////////////////////////////////////////////////////////////
	// LOGGER RELATED STUFF
	///////////////////////////////////////////////////////////////////////////////////
	
	public void setVerboseMode(boolean verbose){
		if(verbose)
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.OFF);
	}

	public void setVerboseMode(Level level){
		logger.setLevel(level);
	}
	
	///////////////////////////////////////////////////////////////////////////////////
	// END OF LOGGER RELATED STUFF
	///////////////////////////////////////////////////////////////////////////////////

}
