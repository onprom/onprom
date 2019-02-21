package it.unibz.inf.kaos.logextractor.exp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONIO {

	
	//read annotation queries files
	public static <T>T importJSON(File annotationFile, Class<T> cls){
				
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
		
		T q = null;
		try {

			q = mapper.readValue(new FileInputStream(annotationFile), cls); //read JSON from URL
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return q;
	}	
	
	//read annotation queries files
	public static <T>T importJSON(String annotationFilePath, Class<T> cls){
		
		return importJSON(new File(annotationFilePath), cls);
	}	
	
	//export annotation queries files
	public static void exportJSON(File annotationFile, Object annotationQueries) throws JsonGenerationException, JsonMappingException, IOException{
		
		System.out.println(
				"Exporting a "+ annotationQueries.getClass().getCanonicalName()+ 
				" to \n\n \""+annotationFile+"\"");
		
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
		
		mapper.writerWithDefaultPrettyPrinter().writeValue(annotationFile, annotationQueries);
	}
	
	//export annotation queries files
	public static void exportJSON(String annotationFilePath, Object annotationQueries) throws JsonGenerationException, JsonMappingException, IOException{
		
		exportJSON(new File(annotationFilePath), annotationQueries);
	}
	

}
