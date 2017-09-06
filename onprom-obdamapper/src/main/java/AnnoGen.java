

import org.semanticweb.owlapi.model.IRI;

import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.data.query.AnnotationQuery;
import it.unibz.inf.kaos.data.query.BinaryAnnotationQuery;
import it.unibz.inf.kaos.data.query.UnaryAnnotationQuery;


public class AnnoGen {


	public static void main(String ar[]){

		try {
			
			AnnotationQueries aq = 
				generateAnnotationQueriesRW17Example();

			printAnnotation(aq);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// ANNOTATION QUERIES GENERATOR 
	//////////////////////////////////////////////////////////////////////////////	
		
	public static AnnotationQueries generateAnnotationQueriesRW17Example(){
			
		AnnotationQueries q = new AnnotationQueries();
		
		////////////////////////////////////////////////////////////////////////////////////
		//TRACE ANOTATION - PAPER
		////////////////////////////////////////////////////////////////////////////////////
		
//		String qt1 = "PREFIX : <http://www.example.com/> "+
//				 "SELECT DISTINCT ?Trace (\"literal\" AS ?Type) (\"concept:name\" AS ?Key) (\"Paper\" AS ?Value)" + 
//				 "WHERE {?Trace a :Paper . } ";

		String qt1 = "PREFIX : <http://www.example.com/> "+
					 "SELECT DISTINCT ?Trace (\"literal\" AS ?Type) (\"concept:name\" AS ?Key) (?title AS ?Value)" + 
					 "WHERE {?Trace a :Paper . ?Trace :title ?title} ";

		q.addQuery(
				new BinaryAnnotationQuery(
					qt1, IRI.create("http://onprom.inf.unibz.it/t-has-a"),
					new String[]{"Trace"}, new String[]{"Type", "Key", "Value"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					qt1, IRI.create("http://onprom.inf.unibz.it/attType"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Type"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					qt1, IRI.create("http://onprom.inf.unibz.it/attKey"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Key"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					qt1, IRI.create("http://onprom.inf.unibz.it/attValue"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Value"})
			);

		////////////////////////////////////////////////////////////////////////////////////
		//END OF TRACE ANOTATION - PAPER
		////////////////////////////////////////////////////////////////////////////////////
		
		
		
		////////////////////////////////////////////////////////////////////////////////////
		//EVENT ANNOTATION - SUBMISSION EVENT 
		////////////////////////////////////////////////////////////////////////////////////

		//--------- Event Name annotation
		
		String e1name = 
				"PREFIX : <http://www.example.com/> "+
				"SELECT DISTINCT (?Submission AS ?Event) (\"Submission\" AS ?EventID) (\"literal\" AS ?Type) (\"concept:name\" AS ?Key) (\"Submission\" AS ?Value)"+
				"WHERE {?Submission  a :Submission .  "
				+ "?Submission :Submission_2_Paper  ?Paper . "
				+ "?Paper a :Paper . "
				+ "}";

		q.addQuery(
				new BinaryAnnotationQuery(
					e1name, IRI.create("http://onprom.inf.unibz.it/e-has-a"),
					new String[]{"EventID", "Event"}, new String[]{"Type", "Key", "Value"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					e1name, IRI.create("http://onprom.inf.unibz.it/attType"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Type"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					e1name, IRI.create("http://onprom.inf.unibz.it/attKey"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Key"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					e1name, IRI.create("http://onprom.inf.unibz.it/attValue"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Value"})
			);
		
		//--------- Event Trace annotation

		String e1trace = 			
				"PREFIX : <http://www.example.com/> "+
				"SELECT DISTINCT (?Submission AS ?Event) (\"Submission\" AS ?EventID) (?Paper AS ?Trace) "
				+ "WHERE  { ?Submission  a :Submission . "
					+ "?Submission :Submission_2_Paper  ?Paper . "
					+ "?Paper a :Paper . "
					+ "}";

		q.addQuery(
				new BinaryAnnotationQuery(
						e1trace, IRI.create("http://onprom.inf.unibz.it/t-contains-e"),
					new String[]{"Trace"}, new String[]{"EventID", "Event"})
			);
		
		//--------- Event Timestamp annotation

		String e1timestamp = 
			"PREFIX : <http://www.example.com/> "+
			"SELECT DISTINCT (?Submission AS ?Event) (\"Submission\" AS ?EventID) (\"timestamp\" AS ?Type) (\"time:timestamp\" AS ?Key) (?t AS ?Value)"
			+ "WHERE {"
			+ "?Submission a :Submission . "
			+ "?Submission :Submission_2_Paper  ?Paper . "
			+ "?Paper a :Paper . "
			+ "?Submission :uploadTime  ?t . "
			+ "}";

		q.addQuery(
				new BinaryAnnotationQuery(
					e1timestamp, IRI.create("http://onprom.inf.unibz.it/e-has-a"),
					new String[]{"EventID", "Event"}, new String[]{"Type", "Key", "Value"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					e1timestamp, IRI.create("http://onprom.inf.unibz.it/attType"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Type"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					e1timestamp, IRI.create("http://onprom.inf.unibz.it/attKey"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Key"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					e1timestamp, IRI.create("http://onprom.inf.unibz.it/attValue"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Value"})
			);
		
		//--------- Event Lifecycle annotation

		String e1lifecycle = 
				"PREFIX : <http://www.example.com/> "+
				"SELECT DISTINCT (?Submission AS ?Event) (\"Submission\" AS ?EventID) (\"literal\" AS ?Type) (\"lifecycle:transition\" AS ?Key) (\"Complete\" AS ?Value) "+
				"WHERE {?Submission  a :Submission .  "
				+ "?Submission :Submission_2_Paper  ?Paper . "
				+ "?Paper a :Paper . "
				+ "}";
		
		q.addQuery(
				new BinaryAnnotationQuery(
					e1lifecycle, IRI.create("http://onprom.inf.unibz.it/e-has-a"),
					new String[]{"EventID", "Event"}, new String[]{"Type", "Key", "Value"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					e1lifecycle, IRI.create("http://onprom.inf.unibz.it/attType"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Type"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					e1lifecycle, IRI.create("http://onprom.inf.unibz.it/attKey"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Key"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					e1lifecycle, IRI.create("http://onprom.inf.unibz.it/attValue"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Value"})
			);
		
		////////////////////////////////////////////////////////////////////////////////////
		//END OF EVENT ANNOTATION - SUBMISSION EVENT 
		////////////////////////////////////////////////////////////////////////////////////
		
		
		
		////////////////////////////////////////////////////////////////////////////////////
		//EVENT ANNOTATION - CREATION EVENT 
		////////////////////////////////////////////////////////////////////////////////////

		//--------- Event Name annotation

		String e2name = 
				"PREFIX : <http://www.example.com/> "+
				"SELECT DISTINCT (?Creation AS ?Event) (\"Creation\" AS ?EventID) (\"literal\" AS ?Type) (\"concept:name\" AS ?Key) (\"Creation\" AS ?Value)"+
				"WHERE {?Creation  a :Creation .  "
				+ "?Creation :Submission_2_Paper  ?Paper . "
				+ "?Paper a :Paper . "
				+ "}";

		q.addQuery(
				new BinaryAnnotationQuery(
					e2name, IRI.create("http://onprom.inf.unibz.it/e-has-a"),
					new String[]{"EventID", "Event"}, new String[]{"Type", "Key", "Value"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					e2name, IRI.create("http://onprom.inf.unibz.it/attType"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Type"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					e2name, IRI.create("http://onprom.inf.unibz.it/attKey"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Key"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					e2name, IRI.create("http://onprom.inf.unibz.it/attValue"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Value"})
			);
		
		//--------- Event Trace annotation

		String e2trace = 			
				"PREFIX : <http://www.example.com/> "+
				"SELECT DISTINCT (?Creation AS ?Event) (\"Creation\" AS ?EventID) (?Paper AS ?Trace) "
				+ "WHERE  { ?Creation  a :Creation . "
					+ "?Creation :Submission_2_Paper  ?Paper . "
					+ "?Paper a :Paper . "
					+ "}";
		
		q.addQuery(
				new BinaryAnnotationQuery(
					e2trace, IRI.create("http://onprom.inf.unibz.it/t-contains-e"),
					new String[]{"Trace"}, new String[]{"EventID", "Event"})
			);
		
		//--------- Event Timestamp annotation
		
		String e2timestamp = 
			"PREFIX : <http://www.example.com/> "+
			"SELECT DISTINCT (?Creation AS ?Event) (\"Creation\" AS ?EventID) (\"timestamp\" AS ?Type) (\"time:timestamp\" AS ?Key) (?t AS ?Value)"
			+ "WHERE {"
			+ "?Creation a :Creation . "
			+ "?Creation :Submission_2_Paper  ?Paper . "
			+ "?Paper a :Paper . "
			+ "?Creation :uploadTime  ?t . "
			+ "}";

		q.addQuery(
				new BinaryAnnotationQuery(
					e2timestamp, IRI.create("http://onprom.inf.unibz.it/e-has-a"),
					new String[]{"EventID", "Event"}, new String[]{"Type", "Key", "Value"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					e2timestamp, IRI.create("http://onprom.inf.unibz.it/attType"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Type"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					e2timestamp, IRI.create("http://onprom.inf.unibz.it/attKey"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Key"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					e2timestamp, IRI.create("http://onprom.inf.unibz.it/attValue"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Value"})
			);
		
		//--------- Event Lifecycle annotation

		String e2lifecycle = 
				"PREFIX : <http://www.example.com/> "+
				"SELECT DISTINCT (?Creation AS ?Event) (\"Creation\" AS ?EventID) (\"literal\" AS ?Type) (\"lifecycle:transition\" AS ?Key) (\"Complete\" AS ?Value) "+
				"WHERE {?Creation  a :Creation .  "
				+ "?Creation :Submission_2_Paper  ?Paper . "
				+ "?Paper a :Paper . "
				+ "}";

		q.addQuery(
				new BinaryAnnotationQuery(
					e2lifecycle, IRI.create("http://onprom.inf.unibz.it/e-has-a"),
					new String[]{"EventID", "Event"}, new String[]{"Type", "Key", "Value"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					e2lifecycle, IRI.create("http://onprom.inf.unibz.it/attType"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Type"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					e2lifecycle, IRI.create("http://onprom.inf.unibz.it/attKey"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Key"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
					e2lifecycle, IRI.create("http://onprom.inf.unibz.it/attValue"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Value"})
			);
		
		////////////////////////////////////////////////////////////////////////////////////
		//END OF EVENT ANNOTATION - CREATION EVENT 
		////////////////////////////////////////////////////////////////////////////////////


		
		////////////////////////////////////////////////////////////////////////////////////
		//EVENT ANNOTATION - REVIEW EVENT 
		////////////////////////////////////////////////////////////////////////////////////

		//--------- Event Name annotation

		String e3name = 
				"PREFIX : <http://www.example.com/> "+
				"SELECT DISTINCT (?Review AS ?Event) (\"Review\" AS ?EventID) (\"literal\" AS ?Type) (\"concept:name\" AS ?Key) (\"Review\" AS ?Value) "+
				"WHERE {"
//				+ "?Assignment :leadsTo ?Review . "
//				+ "?Assignment :Assignment_2_Paper ?Paper . "
//				+ "?Review  a :Review .  "
				+ "?Assignment :leadsTo ?Review . "
//				+ "?Assignment a :Assignment . "
				+ "?Assignment :Assignment_2_Paper ?Paper . "
//				+ "?Paper a :Paper . "
				+ "}";

		q.addQuery(
				new BinaryAnnotationQuery(
						e3name, IRI.create("http://onprom.inf.unibz.it/e-has-a"),
					new String[]{"EventID", "Event"}, new String[]{"Type", "Key", "Value"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
						e3name, IRI.create("http://onprom.inf.unibz.it/attType"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Type"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
						e3name, IRI.create("http://onprom.inf.unibz.it/attKey"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Key"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
						e3name, IRI.create("http://onprom.inf.unibz.it/attValue"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Value"})
			);
		
		//--------- Event Trace annotation

		String e3trace = 			
				"PREFIX : <http://www.example.com/> "+
				"SELECT DISTINCT (?Review AS ?Event) (\"Review\" AS ?EventID) (?Paper AS ?Trace) "
				+ "WHERE  { "
//					+ "?Assignment :leadsTo ?Review . "
//					+ "?Assignment :Assignment_2_Paper ?Paper . "
//					+ "?Review  a :Review . "
					+ "?Assignment :leadsTo ?Review . "
//					+ "?Assignment a :Assignment . "
					+ "?Assignment :Assignment_2_Paper ?Paper . "
//					+ "?Paper a :Paper . "
					+ "}";

		q.addQuery(
				new BinaryAnnotationQuery(
						e3trace, IRI.create("http://onprom.inf.unibz.it/t-contains-e"),
					new String[]{"Trace"}, new String[]{"EventID", "Event"})
			);

		//--------- Event Timestamp annotation

		String e3timestamp = 
			"PREFIX : <http://www.example.com/> "+
			"SELECT DISTINCT (?Review AS ?Event) (\"Review\" AS ?EventID) (\"timestamp\" AS ?Type) (\"time:timestamp\" AS ?Key) (?t AS ?Value) "
			+ "WHERE {"
//			+ "?Assignment :leadsTo ?Review . "
//			+ "?Assignment :Assignment_2_Paper ?Paper . "
//			+ "?Review a :Review . "
			+ "?Assignment :leadsTo ?Review . "
//			+ "?Assignment a :Assignment . "
			+ "?Assignment :Assignment_2_Paper ?Paper . "
//			+ "?Paper a :Paper . "
			+ "?Review :subTime ?t . "
			+ "}";


		q.addQuery(
				new BinaryAnnotationQuery(
						e3timestamp, IRI.create("http://onprom.inf.unibz.it/e-has-a"),
					new String[]{"EventID", "Event"}, new String[]{"Type", "Key", "Value"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
						e3timestamp, IRI.create("http://onprom.inf.unibz.it/attType"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Type"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
						e3timestamp, IRI.create("http://onprom.inf.unibz.it/attKey"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Key"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
						e3timestamp, IRI.create("http://onprom.inf.unibz.it/attValue"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Value"})
			);
		
		//--------- Event Lifecycle annotation

		String e3lifecycle = 
				"PREFIX : <http://www.example.com/> "+
				"SELECT DISTINCT (?Review AS ?Event) (\"Review\" AS ?EventID) (\"literal\" AS ?Type) (\"lifecycle:transition\" AS ?Key) (\"Complete\" AS ?Value) "+
				"WHERE {"
//				+ "?Assignment :leadsTo ?Review . "
//				+ "?Assignment :Assignment_2_Paper ?Paper . "
//				+ "?Review  a :Review .  "
				+ "?Assignment :leadsTo ?Review . "
//				+ "?Assignment a :Assignment . "
				+ "?Assignment :Assignment_2_Paper ?Paper . "
//				+ "?Paper a :Paper . "
				+ "}";
		
		q.addQuery(
				new BinaryAnnotationQuery(
						e3lifecycle, IRI.create("http://onprom.inf.unibz.it/e-has-a"),
					new String[]{"EventID", "Event"}, new String[]{"Type", "Key", "Value"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
						e3lifecycle, IRI.create("http://onprom.inf.unibz.it/attType"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Type"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
						e3lifecycle, IRI.create("http://onprom.inf.unibz.it/attKey"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Key"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
						e3lifecycle, IRI.create("http://onprom.inf.unibz.it/attValue"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Value"})
			);
		
		////////////////////////////////////////////////////////////////////////////////////
		//END OF EVENT ANNOTATION - REVIEW EVENT 
		////////////////////////////////////////////////////////////////////////////////////

		
		
		////////////////////////////////////////////////////////////////////////////////////
		//EVENT ANNOTATION - DECISION EVENT 
		////////////////////////////////////////////////////////////////////////////////////

		//--------- Event Name annotation

		String e4name = 
				"PREFIX : <http://www.example.com/> "+
				"SELECT DISTINCT (?Decision AS ?Event) (\"Decision\" AS ?EventID) (\"literal\" AS ?Type) (\"concept:name\" AS ?Key) (\"Decision\" AS ?Value) "+
				"WHERE {?Decision  a :DecidedPaper .  "
//				+ "?Decision a ?Paper . "
				+ "}";
		
		q.addQuery(
				new BinaryAnnotationQuery(
						e4name, IRI.create("http://onprom.inf.unibz.it/e-has-a"),
					new String[]{"EventID", "Event"}, new String[]{"Type", "Key", "Value"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
						e4name, IRI.create("http://onprom.inf.unibz.it/attType"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Type"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
						e4name, IRI.create("http://onprom.inf.unibz.it/attKey"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Key"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
						e4name, IRI.create("http://onprom.inf.unibz.it/attValue"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Value"})
			);

		//--------- Event Trace annotation

		String e4trace = 			
				"PREFIX : <http://www.example.com/> "+
				"SELECT DISTINCT (?Decision AS ?Event) (\"Decision\" AS ?EventID) (?Paper AS ?Trace) "
				+ "WHERE  { ?Decision  a :DecidedPaper . BIND(?Decision AS ?Paper)"
					+ "}";
		
		q.addQuery(
				new BinaryAnnotationQuery(
						e4trace, IRI.create("http://onprom.inf.unibz.it/t-contains-e"),
					new String[]{"Trace"}, new String[]{"EventID", "Event"})
			);

		
		//--------- Event Timestamp annotation
		
		String e4timestamp = 
			"PREFIX : <http://www.example.com/> "+
			"SELECT DISTINCT (?Decision AS ?Event) (\"Decision\" AS ?EventID) (\"timestamp\" AS ?Type) (\"time:timestamp\" AS ?Key) (?t AS ?Value) "
			+ "WHERE {"
			+ "?Decision  a :DecidedPaper . "
			+ "?Decision :decTime ?t . "
			+ "}";
		
		q.addQuery(
				new BinaryAnnotationQuery(
						e4timestamp, IRI.create("http://onprom.inf.unibz.it/e-has-a"),
					new String[]{"EventID", "Event"}, new String[]{"Type", "Key", "Value"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
						e4timestamp, IRI.create("http://onprom.inf.unibz.it/attType"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Type"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
						e4timestamp, IRI.create("http://onprom.inf.unibz.it/attKey"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Key"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
						e4timestamp, IRI.create("http://onprom.inf.unibz.it/attValue"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Value"})
			);
		

		//--------- Event Lifecycle annotation

		String e4lifecycle = 
				"PREFIX : <http://www.example.com/> "+
				"SELECT DISTINCT (?Decision AS ?Event) (\"Decision\" AS ?EventID) (\"literal\" AS ?Type) (\"lifecycle:transition\" AS ?Key) (\"Complete\" AS ?Value)  "+
				"WHERE {?Decision  a :DecidedPaper .  "
				+ "}";
		
		q.addQuery(
				new BinaryAnnotationQuery(
						e4lifecycle, IRI.create("http://onprom.inf.unibz.it/e-has-a"),
					new String[]{"EventID", "Event"}, new String[]{"Type", "Key", "Value"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
						e4lifecycle, IRI.create("http://onprom.inf.unibz.it/attType"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Type"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
						e4lifecycle, IRI.create("http://onprom.inf.unibz.it/attKey"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Key"})
			);

		q.addQuery(
				new BinaryAnnotationQuery(
						e4lifecycle, IRI.create("http://onprom.inf.unibz.it/attValue"),
					new String[]{"Type", "Key", "Value"}, new String[]{"Value"})
			);
		
		////////////////////////////////////////////////////////////////////////////////////
		//END OF EVENT ANNOTATION - DECISION EVENT 
		////////////////////////////////////////////////////////////////////////////////////

		
		
		return q;
	}

	//////////////////////////////////////////////////////////////////////////////
	// END OF ANNOTATION QUERIES GENERATOR 
	//////////////////////////////////////////////////////////////////////////////	

	//////////////////////////////////////////////////////////////////////////////
	// SOME UTILITIES METHODS
	//////////////////////////////////////////////////////////////////////////////	
	
	public static void printAnnotation(AnnotationQueries anno){

		System.out.println("Print annotation\n=================================\n");
		
		for(AnnotationQuery aq : anno.getAllQueries()){
			
			if(aq instanceof BinaryAnnotationQuery){

				BinaryAnnotationQuery baq = (BinaryAnnotationQuery) aq;
				
				System.out.println("Target Component: \t\t"+baq.getTargetURI());
				System.out.println("Source Query: \t\t\t"+baq.getQuery());
				
				StringBuilder str1 = new StringBuilder("[");
				for(String str:baq.getFirstComponent()){
					str1.append(str);
					str1.append(", ");
				}
				str1.delete(str1.length()-2, str1.length());
				str1.append("]");

				StringBuilder str2 = new StringBuilder("[");
				for(String str:baq.getSecondComponent()){
					str2.append(str);
					str2.append(", ");
				}
				str2.delete(str2.length()-2, str2.length());
				str2.append("]");
				
				System.out.println("First AnsVar Component: \t"+str1);
				System.out.println("Second AnsVar Component: \t"+str2);
				
			}else if(aq instanceof UnaryAnnotationQuery){
				
				UnaryAnnotationQuery uaq = (UnaryAnnotationQuery) aq;
				
				StringBuilder str1 = new StringBuilder("[");
				for(String str:uaq.getComponent()){
					str1.append(str);
					str1.append(", ");
				}
				str1.delete(str1.length()-2, str1.length());
				str1.append("]");
				
				System.out.println("Target Component: \t\t"+uaq.getTargetURI());
				System.out.println("Source Query: \t\t\t"+uaq.getQuery());
				System.out.println("AnsVar Component: \t"+str1);
			}
			System.out.println();
		}
	}

	//////////////////////////////////////////////////////////////////////////////
	// END OF SOME UTILITIES METHODS
	//////////////////////////////////////////////////////////////////////////////	
	
	
}
