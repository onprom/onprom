/*
 * Copyright (C) 2017 Free University of Bozen-Bolzano
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

package it.unibz.inf.kaos.logextractor.example;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unibz.inf.kaos.data.query.AnnotationQueries;
import it.unibz.inf.kaos.logextractor.XESLogExtractor;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.owlrefplatform.owlapi.MappingLoader;

/*
 * Note: before using this example, make sure that you have set up the database and the OBDA file correctly
 */

public class LogExtractorUsageExample2 {

	//------- Some Paths----------
	private static String inputPath = "src/main/resources/_INPUT/";
	private static String outputPath = "src/main/resources/_OUTPUT/";

	//------- Input Ontology ----------
	private static String ontologyFilePath = inputPath + "conference.owl";

	//------- Input OBDA Mapping File ----------
	private static String obdaFile = "h2/conference10.obda";
	private static String obdaFilePath = inputPath + obdaFile;

	//------- Output XES Log----------
	private static String outputXESFilePath = outputPath + obdaFile+".xes";

	//------- Annotation Queries----------
	private static AnnotationQueries annotation = importAnnotationQueries(inputPath + "conference.aqr"); 

	
	public static void main(String[] ar){
		
		PrintStream out = System.out;
		
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

			XESLogExtractor xle = new XESLogExtractor();

			//logger setting for xle
			//xle.enableAllOntopLogger();
			//xle.turnOnMemorySnapshot();
			xle.setVerboseMode(true);
			//END OF logger setting for xle

			OBDAModel obdaModel = new MappingLoader().loadFromOBDAFile(obdaFilePath);
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(ontologyFilePath));

			runningTimeInitialization = System.currentTimeMillis() - startTimeAll;
			
			//////////////////////////////////////////////////////////////////////////////////////////
			// LOG EXTRACTOR
			//////////////////////////////////////////////////////////////////////////////////////////

				long startTimeLogExtractor = System.currentTimeMillis();
				XLog xlog =  xle.extractXESLog(ontology, obdaModel, annotation);
				runningTimeLogExtractor = System.currentTimeMillis() - startTimeLogExtractor;

			//////////////////////////////////////////////////////////////////////////////////////////
			// END OFLOG EXTRACTOR
			//////////////////////////////////////////////////////////////////////////////////////////
			
			//xle.printExecutionNote();

			long startTimeSaveXLog = System.currentTimeMillis();
			saveXESLog(xlog, outputXESFilePath);
			//saveXESLogIntoGZip(xlog, outputXESFileGzipPath);
			runningTimeStartTimeSaveXLog = System.currentTimeMillis() - startTimeSaveXLog;
			
		} catch (Exception e) {
			out.println(e.getMessage());
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		// PROFILING RUNNING TIME
		//////////////////////////////////////////////////////////////////////////////////////////
		
		StringBuilder runningTime = new StringBuilder("\n");
		runningTime.append("---------------------------------------------------------------------------------------\n");
		runningTime.append(obdaFile);
		runningTime.append("\n");
		runningTime.append("---------------------------------------------------------------------------------------\n");
		runningTime.append(getRunningTimeString("Running time - initialization:", "\t", runningTimeInitialization)+"\n");
		runningTime.append(getRunningTimeString("Running time - extractXESLog:", "\t", runningTimeLogExtractor)+"\n");
		runningTime.append(getRunningTimeString("Running time - saveXLog:", "\t", runningTimeStartTimeSaveXLog)+"\n");
		runningTime.append("---------------------------------------------------------------------------------------\n");
		runningTime.append(getRunningTimeString("Running time - all:", "\t\t", System.currentTimeMillis() - startTimeAll)+"\n");
		runningTime.append("---------------------------------------------------------------------------------------\n");
		
		out.println("Finish Extracting a XES log");
		out.println(runningTime);

		//////////////////////////////////////////////////////////////////////////////////////////
		// END OF PROFILING RUNNING TIME
		//////////////////////////////////////////////////////////////////////////////////////////
	}

		
	//////////////////////////////////////////////////////////////////////////////
	// SOME UTILITY METHODS
	//////////////////////////////////////////////////////////////////////////////	
	
	//read annotation queries files
	private static AnnotationQueries importAnnotationQueries(String annotationFile){
		
        //initialize JSON-Object mapper
		ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        //use all fields
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        //only include not null & non empty fields
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        //store type of classess also
        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");
        //ignore unknown properties
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);

		AnnotationQueries q = null;
		
		try {

			q = mapper.readValue(new FileInputStream(new File(annotationFile)), AnnotationQueries.class); //read JSON from URL
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return q;
	}

	private static void saveXESLog(XLog xlog, String filePath){

		//save XES Log
		try {
			new XesXmlSerializer().serialize(xlog, new FileOutputStream(new File(filePath)));
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	private static void saveXESLogIntoGZip(XLog xlog, String filePath){

		//save XES Log
		try {
			new XesXmlGZIPSerializer().serialize(xlog, new FileOutputStream(new File(filePath)));
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	private static String getRunningTimeString(String label, String tab, long time){
		
		DecimalFormat f = new DecimalFormat("###,###.###");

		return label + tab + 
				String.format("%20s", f.format(time)) + " msec. / ~"+ 
				String.format("%3.2f", (double)time/1000)+ " sec. / ~"+ 
				String.format("%3.2f", (double)time/60000)+" min. ";
	}

	//////////////////////////////////////////////////////////////////////////////
	// END OF SOME UTILITY METHODS
	//////////////////////////////////////////////////////////////////////////////	

}
