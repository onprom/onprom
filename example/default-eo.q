[QueryItem="trace-event"]
PREFIX : <http://onprom.inf.unibz.it/>
SELECT distinct ?trace ?event
              WHERE {
                    ?trace <http://onprom.inf.unibz.it/t-contains-e> ?event . 
              }

[QueryItem="trace-attribute"]
PREFIX : <http://onprom.inf.unibz.it/>
SELECT distinct ?trace ?attribute
              WHERE {
                    ?trace <http://onprom.inf.unibz.it/t-has-a> ?attribute . 
              }

[QueryItem="event-attribute"]
PREFIX : <http://onprom.inf.unibz.it/>
SELECT distinct ?event ?attribute
              WHERE {
                    ?event <http://onprom.inf.unibz.it/e-has-a> ?attribute . 
              }

[QueryItem="attributes"]
PREFIX : <http://onprom.inf.unibz.it/>
SELECT distinct ?attribute ?attType ?attKey ?attValue
              WHERE {
                    ?attribute a <http://onprom.inf.unibz.it/Attribute>; 
		    <http://onprom.inf.unibz.it/attType> ?attType; 
		    <http://onprom.inf.unibz.it/attKey> ?attKey; 
		    <http://onprom.inf.unibz.it/attValue> ?attValue
              }

[QueryItem="event-attributes"]
PREFIX : <http://onprom.inf.unibz.it/>
SELECT distinct ?event ?attribute
              WHERE {
                    ?event <http://onprom.inf.unibz.it/e-has-a> ?attribute . 
                    ?attribute a <http://onprom.inf.unibz.it/Attribute>; 
		    <http://onprom.inf.unibz.it/attType> ?attType; 
		    <http://onprom.inf.unibz.it/attKey> ?attKey; 
		    <http://onprom.inf.unibz.it/attValue> ?attValue
              }
