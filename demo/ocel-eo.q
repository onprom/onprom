[QueryItem="e-contains-o"]
PREFIX : <http://onprom.inf.unibz.it/ocel/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT  ?event ?object
WHERE
  { ?event :e-contains-o  ?object}
[QueryItem="e-has-a"]
PREFIX : <http://onprom.inf.unibz.it/ocel/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT  ?event ?att
WHERE
  { ?event :e-has-a  ?att}
[QueryItem="allAttributes"]
PREFIX : <http://onprom.inf.unibz.it/ocel/> 
SELECT distinct ?att ?attType ?attKey ?attValue 
WHERE { ?att a <http://onprom.inf.unibz.it/ocel/Attribute>; <http://onprom.inf.unibz.it/ocel/attType> ?attType;  }
[QueryItem="o-has-a"]
PREFIX : <http://onprom.inf.unibz.it/ocel/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT  ?obj ?att
WHERE
  { ?obj :o-has-a  ?att}