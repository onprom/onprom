package it.unibz.inf.pm.ocel.util;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXValidator;
import org.dom4j.io.XMLWriter;
import org.dom4j.util.XMLErrorHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;


public class XMLValidateUtil {
    /**
     * using XSD（XML Schema）to validate the XML file
     */
    public static boolean validateXMLByXSD(String input_path, String validation_path,String parameters) {
        String xmlFileName = input_path;
        String xsdFileName = validation_path;
        try {
            XMLErrorHandler errorHandler = new XMLErrorHandler();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();
            SAXReader xmlReader = new SAXReader();
            Document xmlDocument = (Document) xmlReader.read(new File(xmlFileName));
            parser.setProperty(
                    "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                    "http://www.w3.org/2001/XMLSchema");
            parser.setProperty(
                    "http://java.sun.com/xml/jaxp/properties/schemaSource",
                    "file:" + xsdFileName);
            SAXValidator validator = new SAXValidator(parser.getXMLReader());
            validator.setErrorHandler(errorHandler);
            validator.validate(xmlDocument);

            XMLWriter writer = new XMLWriter(OutputFormat.createPrettyPrint());
            if (errorHandler.getErrors().hasContent()) {
                System.out.println("Sorry! Validation of XML file fails!");
                writer.write(errorHandler.getErrors());
                return false;
            } else {
                System.out.println("Good! The XML file successfully passed the XSD file validation!");
                return true;
            }
        } catch (Exception ex) {
            System.out.println("XML: " + xmlFileName + " Validation using XSD:" + xsdFileName + " fails. \n Cause： " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        XMLValidateUtil.validateXMLByXSD("ocel/examples/log2.xmlocel","ocel/schemas/schema.xml","");
    }

}
