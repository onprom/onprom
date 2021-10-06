[QueryItem="findAllTriple"]
PREFIX : <http://www.example.com/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT *  {
?s ?p ?o .
}
[QueryItem="find'Decided'Paper"]
PREFIX : <http://www.example.com/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT  ?DecidedPaper ?_I0_682971820 ("Decision" AS ?_I1_682971820) ?Paper
WHERE
  { ?DecidedPaper
              a                     <http://www.example.com/DecidedPaper> ;
              <http://www.example.com/decTime>  ?_I0_682971820
    BIND(?DecidedPaper AS ?Paper)
  }
[QueryItem="CRUpload"]
PREFIX : <http://www.example.com/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT  ?CRUpload ?_I0_654993233 ("CameraReady" AS ?_I1_654993233) ?Paper
WHERE
  { ?CRUpload  <http://www.example.com/uploadTime>  ?_I0_654993233 ;
              a                     <http://www.example.com/CRUpload> ;
              <http://www.example.com/Submission_2_Paper>  ?Paper}
[QueryItem="review"]
PREFIX : <http://www.example.com/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT  ?Review ?_I0_1748687462 ?Paper
WHERE
  { ?Assignment  <http://www.example.com/leadsTo>  ?Review ;
              <http://www.example.com/Assignment_2_Person>  ?Person .
    ?Person   <http://www.example.com/pName>  ?_I0_1748687462 .
    ?Assignment  <http://www.example.com/leadsTo>  ?Review ;
              <http://www.example.com/Assignment_2_Paper>  ?Paper}
[QueryItem="Assignment"]
PREFIX : <http://www.example.com/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT  ?Assignment ?_I0_637997939 ?_I1_637997939 ("ReviewInvitation" AS ?_I2_637997939) ?Review
WHERE
  { ?Assignment  <http://www.example.com/accTime>  ?_I0_637997939 ;
              <http://www.example.com/invTime>  ?_I1_637997939 ;
              <http://www.example.com/leadsTo>  ?Review}
[QueryItem="event"]
PREFIX : <http://www.example.com/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT  ?Event ("literal" AS ?_I0_2002637982) ("concept:name" AS ?_I1_2002637982) ?_I2_2002637982 ("CaseComplete" AS ?_L_331750039) ?_E1_331750039 ?Case
WHERE
  { ?Event  a                     <http://onprom.inf.unibz.it/Event> ;
            <http://onprom.inf.unibz.it/eName>  ?_I2_2002637982 ;
            <http://onprom.inf.unibz.it/endTS>  ?_E1_331750039 .
    ?Case   <http://onprom.inf.unibz.it/c-contains-e>  ?Event
}