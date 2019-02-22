package it.unibz.inf.kaos.logextractor.exp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.data.query.AnnotationQuery;
import it.unibz.inf.kaos.data.query.BinaryAnnotationQuery;
import it.unibz.inf.kaos.data.query.UnaryAnnotationQuery;
import it.unibz.inf.kaos.logextractor.XESLogExtractorWithEBDAMapping;
import it.unibz.inf.kaos.logextractor.model.EBDAMapping;
import it.unibz.inf.kaos.obdamapper.OBDAMapper;
import it.unibz.inf.kaos.obdamapper.exception.InvalidDataSourcesNumberException;
import it.unibz.inf.kaos.obdamapper.model.OBDAMapping;
import it.unibz.inf.ontop.io.ModelIOManager;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.owlrefplatform.owlapi.MappingLoader;


/**
 * OnProm experiment using synthetic conference database
 * 
 * 
 * @author Ario Santoso (santoso.ario@gmail.com)
 */
abstract class Exp {


	private String codeName = "Exp";
	private String pathTestInput = "src/main/resources/use-case/";
	private String pathTestOutput = "src/main/resources/use-case-out/";

	// INPUT: ONTOLOGY, ANNOTATION QUERIES AND OBDA MAPPING FILE
	private String obdaFile = null;
	private String domainOntologyFile = "conference.owl";
	private String customEventOntologyFile = "custom-eo.owl";
	private String annoQueriesFileName1 = "conference-lvl1.aqr";
	private String annoQueriesFileName2 = "conference-lvl2.aqr";
	
	private String obdaFilePath = null;
	private String domainOntologyFilePath = null;	
	private String customEventOntologyFilePath = null;
	private AnnotationQueries annotation = null;
	private AnnotationQueries annotation2 = null;

	// OUTPUT LOCATION
	private String outputXESFilePath = null;
	private String outputOBDAMapping = null;//mapping from the database to the custom event ontology
	private String outputEBDAMapping = null;//mapping from the database to the event ontology
	
	//prevent the instantiation of the class Exp
	protected Exp(String configFile, String codeName, String obdaFile) throws Exception{
		
		this.codeName = codeName;

		//read config
		BufferedReader reader = new BufferedReader(new FileReader(configFile));
		String inp = reader.readLine();
		String out = reader.readLine();
		reader.close();
		
		StringTokenizer strtok1 = new StringTokenizer(inp, ":");
		StringTokenizer strtok2 = new StringTokenizer(out, ":");
		
		if(strtok1.countTokens() != 2 || strtok2.countTokens() != 2)
			throw new Exception("Invalid Input");
			
		String in1 = strtok1.nextToken().trim();
		String in2 = strtok2.nextToken().trim();
		
		if(!in1.equalsIgnoreCase("INPUT") || !in2.equalsIgnoreCase("OUTPUT"))
			throw new Exception("Invalid Input");
		
		this.pathTestInput = strtok1.nextToken().trim();
		this.pathTestOutput = strtok2.nextToken().trim();		
		
//		this.pathTestInput = "src/main/resources/use-case/";
//		this.pathTestOutput = "src/main/resources/use-case-out/";
		
		//Input
		this.obdaFile = obdaFile;
		this.obdaFilePath = pathTestInput + obdaFile;
		this.domainOntologyFilePath = pathTestInput + domainOntologyFile;
		this.customEventOntologyFilePath = pathTestInput + customEventOntologyFile;
		this.annotation = JSONIO.importJSON(pathTestInput+annoQueriesFileName1, AnnotationQueries.class);
		this.annotation2 = JSONIO.importJSON(pathTestInput+annoQueriesFileName2, AnnotationQueries.class);
		
		//Output
		this.outputXESFilePath = pathTestOutput +codeName+".xes";
		this.outputOBDAMapping = pathTestOutput +codeName+"-mapping1.obda";
		this.outputEBDAMapping = pathTestOutput +codeName+"-mapping2.obda";
	}
	
	//////////////////////////////////////////////////////////////////////////////
	// SOME TESTING METHOD
	//////////////////////////////////////////////////////////////////////////////	

	public  void extractXESLog(PrintStream out){

		
		if(	this.obdaFilePath == null || 
			this.domainOntologyFilePath == null || this.customEventOntologyFilePath == null ||
			this.annotation == null || this.annotation2 == null){
			
			out.println("Invalid Input!");
			return;
		}
		
		//------------------------------------------------------------------------------
		//Uncomment the two rows below in order to make the output less verbose, or
		//Comment the two rows below in order to make the output more verbose
		//------------------------------------------------------------------------------
		//System.setOut(new PrintStream(new ByteArrayOutputStream()));
		//System.setErr(new PrintStream(new ByteArrayOutputStream()));
		//------------------------------------------------------------------------------
		
		
		
		long startTimeAll = System.currentTimeMillis();

		long runningTimeInitialization = 0;
		long runningTimeLogExtractor = 0;
		long runningTimeStartTimeSaveXLog = 0;
		
		try {

			XESLogExtractorWithEBDAMapping xle = new XESLogExtractorWithEBDAMapping();

			//logger setting for xle
			xle.enableAllOntopLogger();
			xle.turnOnMemorySnapshot();
			xle.setVerboseMode(true);
			//xle.setVerboseMode(false);
			//END OF logger setting for xle

			OBDAModel obdaModel = new MappingLoader().loadFromOBDAFile(obdaFilePath);

			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology domainOnto = manager.loadOntologyFromOntologyDocument(new File(domainOntologyFilePath));

			OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
			OWLOntology customEventOnto = manager2.loadOntologyFromOntologyDocument(new File(customEventOntologyFilePath));

			runningTimeInitialization = System.currentTimeMillis() - startTimeAll;
			
			//////////////////////////////////////////////////////////////////////////////////////////
			// LOG EXTRACTOR
			//////////////////////////////////////////////////////////////////////////////////////////

				long startTimeLogExtractor = System.currentTimeMillis();
				XLog xlog = xle.extractXESLog(domainOnto, obdaModel, annotation, customEventOnto, annotation2);
				runningTimeLogExtractor = System.currentTimeMillis() - startTimeLogExtractor;

			//////////////////////////////////////////////////////////////////////////////////////////
			// END OF LOG EXTRACTOR
			//////////////////////////////////////////////////////////////////////////////////////////
			
			//TODO: print XLog statistic

			//xle.printExecutionNote();
			//debugXLog(xlog);
			
			long startTimeSaveXLog = System.currentTimeMillis();
			saveXESLog(xlog, outputXESFilePath);
			runningTimeStartTimeSaveXLog = System.currentTimeMillis() - startTimeSaveXLog;
				
	
			//////////////////////////////////////////////////////////////////////////////////////////
			// Output
			//////////////////////////////////////////////////////////////////////////////////////////
			
			StringBuilder output = new StringBuilder("\n");
			output.append("---------------------------------------------------------------------------------------\n");
			output.append("Experiment code name: "+this.codeName);
			output.append("\n");
			output.append("OBDA file: "+obdaFile);
			output.append("\n");
			output.append("---------------------------------------------------------------------------------------\n");
			output.append(getRunningTimeString("Running time - initialization:", "\t\t", runningTimeInitialization)+"\n");
			output.append(getRunningTimeString("Running time - extractXESLog:", "\t\t", runningTimeLogExtractor)+"\n");
			output.append(getRunningTimeString("Running time - saveXLog:", "\t\t", runningTimeStartTimeSaveXLog)+"\n");
			output.append("---------------------------------------------------------------------------------------\n");
			output.append(getRunningTimeString("Running time - all:", "\t\t\t", System.currentTimeMillis() - startTimeAll)+"\n");
			output.append("---------------------------------------------------------------------------------------\n");
			output.append("Info about the extracted log:\n");
			output.append("---------------------------------------------------------------------------------------\n");
			output.append(getLogInfo(xlog));
			output.append("\n---------------------------------------------------------------------------------------\n");
			
			out.println(output);
			
			//////////////////////////////////////////////////////////////////////////////////////////
			// END OF Output
			//////////////////////////////////////////////////////////////////////////////////////////
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public  void extractXESLogAndMappings(PrintStream out){

		
		if(	this.obdaFilePath == null || 
			this.domainOntologyFilePath == null || this.customEventOntologyFilePath == null ||
			this.annotation == null || this.annotation2 == null)
			return;
		
		//------------------------------------------------------------------------------
		//Uncomment the two rows below in order to make the output less verbose, or
		//Comment the two rows below in order to make the output more verbose
		//------------------------------------------------------------------------------
		//System.setOut(new PrintStream(new ByteArrayOutputStream()));
		//System.setErr(new PrintStream(new ByteArrayOutputStream()));
		//------------------------------------------------------------------------------
		
		long startTimeAll = System.currentTimeMillis();

		long runningTimeInitialization = 0;
		long runningTimeLogExtractor = 0;
		long runningTimePureLogExtractor = 0;
		long runningTimeSaveOBDAMapping = 0;
		long runningTimeSaveEBDAMapping = 0;
		long runningTimeStartTimeSaveXLog = 0;
		
		try {

			XESLogExtractorWithEBDAMapping xle = new XESLogExtractorWithEBDAMapping();

			//logger setting for xle
//			xle.enableAllOntopLogger();
//			xle.turnOnMemorySnapshot();
			xle.setVerboseMode(true);
			//END OF logger setting for xle

			OBDAModel obdaModel = new MappingLoader().loadFromOBDAFile(obdaFilePath);

			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology domainOnto = manager.loadOntologyFromOntologyDocument(new File(domainOntologyFilePath));

			OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
			OWLOntology customEventOnto = manager2.loadOntologyFromOntologyDocument(new File(customEventOntologyFilePath));

			runningTimeInitialization = System.currentTimeMillis() - startTimeAll;
			
			//////////////////////////////////////////////////////////////////////////////////////////
			// LOG EXTRACTOR
			//////////////////////////////////////////////////////////////////////////////////////////

				XLog xlog = null;
				long startTimeLogExtractor = System.currentTimeMillis();
				
				//==========================================================================================
				//Create an OBDA mapping to the custom event ontology
				//==========================================================================================
				OBDAMapping obdaMapping = null;
				try {
					
					obdaMapping = new OBDAMapper().createOBDAMapping(domainOnto, customEventOnto, obdaModel, annotation);
					
				} catch (InvalidDataSourcesNumberException e) {
					e.printStackTrace();
				}
				
				//save OBDA Mapping to a file
				long startTimeSaveOBDAMapping = System.currentTimeMillis();
				if(obdaMapping != null){
					try {
						new ModelIOManager(obdaMapping).save(outputOBDAMapping);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				long endTimeSaveOBDAMapping = System.currentTimeMillis();
				runningTimeSaveOBDAMapping = endTimeSaveOBDAMapping - startTimeSaveOBDAMapping;
				//END OF saving OBDA Mapping to a file
				
				//==========================================================================================
				//END OF creating an OBDA mapping to the custom event ontology
				//==========================================================================================

				//==========================================================================================
				//Create an EBDA mapping to the XES event ontology
				//==========================================================================================

				EBDAMapping ebdaModel = xle.createEBDAMapping(customEventOnto, obdaMapping, annotation2);
				
				//save EBDA Mapping to a file
				startTimeSaveOBDAMapping = System.currentTimeMillis();
				if(ebdaModel != null){
					try {
						new ModelIOManager(ebdaModel).save(outputEBDAMapping);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				endTimeSaveOBDAMapping = System.currentTimeMillis();
				runningTimeSaveEBDAMapping = endTimeSaveOBDAMapping - startTimeSaveOBDAMapping;
				//END OF saving EBDA Mapping to a file

				//==========================================================================================
				//END OF Create an EBDA mapping to the XES event ontology
				//==========================================================================================

				//==========================================================================================
				//Extract XES Log
				//==========================================================================================
				
				//xlog = xle.extractXESLog(customEventOnto, obdaMapping, annotation2);
				xlog = xle.extractXESLogUsingOnlyAtomicQueriesMO(ebdaModel);
				
				//==========================================================================================
				//END OF Extract XES Log
				//==========================================================================================
				
				runningTimeLogExtractor = System.currentTimeMillis() - startTimeLogExtractor;
				runningTimePureLogExtractor = runningTimeLogExtractor - (runningTimeSaveOBDAMapping+runningTimeSaveEBDAMapping);
				
			//////////////////////////////////////////////////////////////////////////////////////////
			// END OFLOG EXTRACTOR
			//////////////////////////////////////////////////////////////////////////////////////////
			
			//TODO: print XLog statistic

			//xle.printExecutionNote();
			//debugXLog(xlog);
			
			long startTimeSaveXLog = System.currentTimeMillis();
			saveXESLog(xlog, outputXESFilePath);
			//saveXESLogIntoGZip(xlog, outputXESFileGzipPath);
			runningTimeStartTimeSaveXLog = System.currentTimeMillis() - startTimeSaveXLog;
				
	
			//////////////////////////////////////////////////////////////////////////////////////////
			// Output
			//////////////////////////////////////////////////////////////////////////////////////////
			
			StringBuilder output = new StringBuilder("\n");
			output.append("---------------------------------------------------------------------------------------\n");
			output.append(obdaFile);
			output.append("\n");
			output.append("---------------------------------------------------------------------------------------\n");
			output.append(getRunningTimeString("Running time - initialization:", "\t\t", runningTimeInitialization)+"\n");
			output.append(getRunningTimeString("Running time - extractXESLog:", "\t\t", runningTimeLogExtractor)+"\n");
			output.append(getRunningTimeString("Running time - extractXESLog (Pure):", "\t", runningTimePureLogExtractor)+"\n");
			output.append(getRunningTimeString("Running time - saveXLog:", "\t\t", runningTimeStartTimeSaveXLog)+"\n");
			output.append("---------------------------------------------------------------------------------------\n");
			output.append(getRunningTimeString("Running time - all:", "\t\t\t", System.currentTimeMillis() - startTimeAll)+"\n");
			output.append("---------------------------------------------------------------------------------------\n");
			output.append("Info about the extracted log:\n");
			output.append("---------------------------------------------------------------------------------------\n");
			output.append(getLogInfo(xlog));
			output.append("\n---------------------------------------------------------------------------------------\n");
			
			out.println(output);
			
			//////////////////////////////////////////////////////////////////////////////////////////
			// END OF Output
			//////////////////////////////////////////////////////////////////////////////////////////
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Deprecated
	public  void extractXESLogAndOBDAMapping(PrintStream out){

		
		if(	this.obdaFilePath == null || 
			this.domainOntologyFilePath == null || this.customEventOntologyFilePath == null ||
			this.annotation == null || this.annotation2 == null)
			return;
		
		//------------------------------------------------------------------------------
		//Uncomment the two rows below in order to make the output less verbose, or
		//Comment the two rows below in order to make the output more verbose
		//------------------------------------------------------------------------------
		//System.setOut(new PrintStream(new ByteArrayOutputStream()));
		//System.setErr(new PrintStream(new ByteArrayOutputStream()));
		//------------------------------------------------------------------------------
		
		long startTimeAll = System.currentTimeMillis();

		long runningTimeInitialization = 0;
		long runningTimeLogExtractor = 0;
		long runningTimePureLogExtractor = 0;
		long runningTimeSaveOBDAMapping = 0;
		long runningTimeStartTimeSaveXLog = 0;
		
		try {

			XESLogExtractorWithEBDAMapping xle = new XESLogExtractorWithEBDAMapping();

			//logger setting for xle
//			xle.enableAllOntopLogger();
//			xle.turnOnMemorySnapshot();
			xle.setVerboseMode(true);
			//END OF logger setting for xle

			OBDAModel obdaModel = new MappingLoader().loadFromOBDAFile(obdaFilePath);

			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology domainOnto = manager.loadOntologyFromOntologyDocument(new File(domainOntologyFilePath));

			OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
			OWLOntology customEventOnto = manager2.loadOntologyFromOntologyDocument(new File(customEventOntologyFilePath));

			runningTimeInitialization = System.currentTimeMillis() - startTimeAll;
			
			//////////////////////////////////////////////////////////////////////////////////////////
			// LOG EXTRACTOR
			//////////////////////////////////////////////////////////////////////////////////////////

				XLog xlog = null;
				long startTimeLogExtractor = System.currentTimeMillis();
				
				xlog = xle.extractXESLog(domainOnto, obdaModel, annotation, customEventOnto, annotation2);

				//////////////////////////////////////////////////////////////////////////////////////////
				//Create an OBDA mapping to the custom event ontology
				OBDAMapping obdaMapping = null;
				try {
					
					obdaMapping = new OBDAMapper().createOBDAMapping(domainOnto, customEventOnto, obdaModel, annotation);
					
				} catch (InvalidDataSourcesNumberException e) {
					e.printStackTrace();
				}
				
				//save OBDA Mapping to a file
				long startTimeSaveOBDAMapping = System.currentTimeMillis();
				if(obdaMapping != null){
					try {
						new ModelIOManager(obdaMapping).save(outputOBDAMapping);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				long endTimeSaveOBDAMapping = System.currentTimeMillis();
				runningTimeSaveOBDAMapping = endTimeSaveOBDAMapping - startTimeSaveOBDAMapping;
				//END OF saving OBDA Mapping to a file
				
				//Finish creating an OBDA mapping to the custom event ontology

				xlog = xle.extractXESLog(customEventOnto, obdaMapping, annotation2);
				//////////////////////////////////////////////////////////////////////////////////////////
				
				runningTimeLogExtractor = System.currentTimeMillis() - startTimeLogExtractor;
				runningTimePureLogExtractor = runningTimeLogExtractor - runningTimeSaveOBDAMapping;
				
			//////////////////////////////////////////////////////////////////////////////////////////
			// END OFLOG EXTRACTOR
			//////////////////////////////////////////////////////////////////////////////////////////
			
			//TODO: print XLog statistic

			//xle.printExecutionNote();
			//debugXLog(xlog);
			
			long startTimeSaveXLog = System.currentTimeMillis();
			saveXESLog(xlog, outputXESFilePath);
			//saveXESLogIntoGZip(xlog, outputXESFileGzipPath);
			runningTimeStartTimeSaveXLog = System.currentTimeMillis() - startTimeSaveXLog;
				
	
			//////////////////////////////////////////////////////////////////////////////////////////
			// Output
			//////////////////////////////////////////////////////////////////////////////////////////
			
			StringBuilder output = new StringBuilder("\n");
			output.append("---------------------------------------------------------------------------------------\n");
			output.append(obdaFile);
			output.append("\n");
			output.append("---------------------------------------------------------------------------------------\n");
			output.append(getRunningTimeString("Running time - initialization:", "\t\t", runningTimeInitialization)+"\n");
			output.append(getRunningTimeString("Running time - extractXESLog:", "\t\t", runningTimeLogExtractor)+"\n");
			output.append(getRunningTimeString("Running time - extractXESLog (Pure):", "\t", runningTimePureLogExtractor)+"\n");
			output.append(getRunningTimeString("Running time - saveXLog:", "\t\t", runningTimeStartTimeSaveXLog)+"\n");
			output.append("---------------------------------------------------------------------------------------\n");
			output.append(getRunningTimeString("Running time - all:", "\t\t\t", System.currentTimeMillis() - startTimeAll)+"\n");
			output.append("---------------------------------------------------------------------------------------\n");
			output.append("Info about the extracted log:\n");
			output.append("---------------------------------------------------------------------------------------\n");
			output.append(getLogInfo(xlog));
			output.append("\n---------------------------------------------------------------------------------------\n");
			
			out.println(output);
			
			//////////////////////////////////////////////////////////////////////////////////////////
			// END OF Output
			//////////////////////////////////////////////////////////////////////////////////////////
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	public  void createEBDAMapping(PrintStream out){

		//------------------------------------------------------------------------------
		//Uncomment the two rows below in order to make the output less verbose, or
		//Comment the two rows below in order to make the output more verbose
		//------------------------------------------------------------------------------
		//System.setOut(new PrintStream(new ByteArrayOutputStream()));
		//System.setErr(new PrintStream(new ByteArrayOutputStream()));
		//------------------------------------------------------------------------------
		
		try {

			XESLogExtractorWithEBDAMapping xle = new XESLogExtractorWithEBDAMapping();

			//logger setting for xle
//			xle.enableAllOntopLogger();
//			xle.turnOnMemorySnapshot();
			xle.setVerboseMode(true);
			//END OF logger setting for xle

			OBDAModel obdaModel = new MappingLoader().loadFromOBDAFile(obdaFilePath);
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(domainOntologyFilePath));

			EBDAMapping ebdaModel = xle.createEBDAMapping(ontology, obdaModel, annotation);
				
			//save OBDA Mapping to a file
			try {
				new ModelIOManager(ebdaModel).save(outputOBDAMapping);
			} catch (IOException e) {
				e.printStackTrace();
			}
			//END OF save OBDA Mapping to a file
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//////////////////////////////////////////////////////////////////////////////
	// END OF SOME TESTING METHOD
	//////////////////////////////////////////////////////////////////////////////	
	

	
	//////////////////////////////////////////////////////////////////////////////
	// SOME METHODS FOR XES Log I/O
	//////////////////////////////////////////////////////////////////////////////	
	
	public void saveXESLog(XLog xlog, String filePath){

		//save XES Log
		try {
			new XesXmlSerializer().serialize(xlog, new FileOutputStream(new File(filePath)));
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public void saveXESLogIntoGZip(XLog xlog, String filePath){

		//save XES Log
		try {
			new XesXmlGZIPSerializer().serialize(xlog, new FileOutputStream(new File(filePath)));
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public XLog readXESLog(String fileAbsolutePath) throws Exception{
		
		FileInputStream fis = new FileInputStream(fileAbsolutePath);
		XesXmlParser xxp = new XesXmlParser();	
		List<XLog> xlogs = xxp.parse(fis);
		
		xxp = null;
		fis.close();

		XLog xlog = null;
		
		if(xlogs.size() > 0){
			xlog = xlogs.get(0);
		}
	
		return xlog;
	}
	
	//////////////////////////////////////////////////////////////////////////////
	// END OF SOME METHODS FOR XES Log I/O
	//////////////////////////////////////////////////////////////////////////////	

	
	
	//////////////////////////////////////////////////////////////////////////////
	// SOME METHODS FOR DEBUGGING
	//////////////////////////////////////////////////////////////////////////////	
	
	public void debugXLog(XLog xlog){
		
		StringBuilder msg = new StringBuilder("");
		
		msg.append("\n\n\n#########################################\n");
		msg.append("#DEBUGA: Result of XLog Extractions\n\n");
		
		msg.append("xlog.getAttributes() => "+xlog.getAttributes()+"\n");
		msg.append("xlog.getExtensions() => "+xlog.getExtensions()+"\n");
		msg.append("xlog.getClassifiers() => "+xlog.getClassifiers()+"\n");
		msg.append("xlog.getGlobalTraceAttributes() => "+xlog.getGlobalTraceAttributes()+"\n");
		msg.append("xlog.getGlobalEventAttributes() => "+xlog.getGlobalEventAttributes()+"\n");
				
		//note: in the end XLog is actually an array list of trace XD 
		for(int ii = 0; ii < xlog.size(); ii++){
			
			XTrace xt = xlog.get(ii);
			msg.append("------------------------------\n");
			msg.append("XTrace: "+xt+"\n");
			
			Iterator<XAttribute> itat = xt.getAttributes().values().iterator();
			while(itat.hasNext()){
				XAttribute xa = itat.next();
				msg.append("\t XAttribute Key: "+xa.getKey()+"; XAttribute value: "+xa+"; XAttribute extension: "+xa.getExtension()+"; XAttribute extensions: "+xa.getExtensions()+"\n");
			}

			Iterator<XEvent> ite = xt.iterator();
			while(ite.hasNext()){
				XEvent xe = ite.next();
				msg.append("\t XEvent"+xe+"\n");

				Iterator<XAttribute> ita = xe.getAttributes().values().iterator();
				while(ita.hasNext()){
					XAttribute xa = ita.next();
					msg.append("\t\t XAttribute Key: "+xa.getKey()+"; XAttribute value: "+xa+"; XAttribute extension: "+xa.getExtension()+"; XAttribute extensions: "+xa.getExtensions()+"\n");
				}
			}
		}
		
		msg.append("\n# DEBUGA: END OF Result of XLog Extractions\n");
		msg.append("#########################################\n\n\n\n");	
		
		System.out.println(msg);
		
//		//save XES Log
//		XesXmlSerializer s = new XesXmlSerializer();		
//
//		try {
//			File f = new File(outputXES);
//			s.serialize(xlog, new FileOutputStream(f));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}		
	}
	
	public String getRunningTimeString(String label, String tab, long time){
		
		DecimalFormat f = new DecimalFormat("###,###.###");

		return label + tab + 
				String.format("%20s", f.format(time)) + " msec. / ~"+ 
				String.format("%3.2f", (double)time/1000)+ " sec. / ~"+ 
				String.format("%3.2f", (double)time/60000)+" min. ";
	}

	public String getLogInfo(XLog xlog){
		
		int numOfTraces = 0;
		int numOfEvents = 0;
		int numOfTraceAtts = 0;
		int numOfEventAtts = 0;
		
		for(XTrace xtrace: xlog){
			
			numOfTraces++;
			numOfTraceAtts += xtrace.getAttributes().size();
			
			for(XEvent xevent: xtrace){
				
				numOfEvents++;
				numOfEventAtts += xevent.getAttributes().size();
			}
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("Number of Traces: "+ numOfTraces+"\n");
		sb.append("Number of Events: "+ numOfEvents+"\n");
		sb.append("Number of Trace Attributes: "+ numOfTraceAtts+"\n");
		sb.append("Number of Event Attributes: "+ numOfEventAtts);
		
		return sb.toString();
	}
	
	public void debugAnno(AnnotationQueries annotation){
		
		for(AnnotationQuery aq: annotation.getAllQueries()){
			
			
			if(aq instanceof BinaryAnnotationQuery){

				System.out.println("==========================================");
				System.out.println("BinaryAnnotationQuery\n--------------------------------");
				System.out.println("Query: \n"+ aq.getQuery());
				System.out.println("Target URI: \n"+ aq.getTargetIRI());

				String[] f = ((BinaryAnnotationQuery) aq).getFirstComponent();
				System.out.print("\nFirstComponent(s): [ ");

				for(int ii = 0; ii < f.length; ii++)
					System.out.print(f[ii]+", ");
				
				System.out.print("]\nSecondComponent(s): [ ");
				String[] s = ((BinaryAnnotationQuery) aq).getSecondComponent();
				for(int ii = 0; ii < s.length; ii++)
					System.out.print(s[ii]+", ");

				System.out.println("]");
			}else if(aq instanceof UnaryAnnotationQuery){
				
				System.out.println(" skip Unary Annotation Query");

			}
 
			System.out.println("==========================================\n");

		}
	}
	
	//////////////////////////////////////////////////////////////////////////////
	// END OF SOME METHODS FOR DEBUGGING
	//////////////////////////////////////////////////////////////////////////////	

}
