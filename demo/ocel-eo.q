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