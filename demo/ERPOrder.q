[QueryItem="product"]
PREFIX : <http://www.example.com/dolibarr#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT * {?product a :Product }
[QueryItem="customer"]
PREFIX : <http://www.example.com/dolibarr#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT * {?customer a :Customer}
[QueryItem="salesOrder"]
PREFIX : <http://www.example.com/dolibarr#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT * {?salesOrder a :SalesOrder .
?salesOrder :orderNo ?No
}
[QueryItem="OrderItemAndProduct"]
PREFIX : <http://www.example.com/dolibarr#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT * {?OrderItem a :Product}
[QueryItem="payment"]
PREFIX : <http://www.example.com/dolibarr#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT *  {?payment a :Payment}
[QueryItem="shipmentItem"]
PREFIX : <http://www.example.com/dolibarr#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT *  {?shipmentItem a :ShipmentItem}
[QueryItem="getSPO"]
PREFIX : <http://www.example.com/dolibarr#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT * WHERE {
  ?sub ?pred ?obj .
}
[QueryItem="shipment"]
PREFIX : <http://www.example.com/dolibarr#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT *  {?shipment a :Shipment}
[QueryItem="hasShipmentItem"]
PREFIX : <http://www.example.com/dolibarr#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT * {?shipment :hasShipmentItem ?shipmentItem}
[QueryItem="invoiceToPayment"]
PREFIX : <http://www.example.com/dolibarr#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT * {?invoice :invoiceToPayment ?payment}
[QueryItem="createOrder"]
PREFIX : <http://www.example.com/dolibarr#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

select * {?c :create ?order}
[QueryItem="salesOrder-product"]
PREFIX : <http://www.example.com/dolibarr#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT * {
   ?salesOrder :hasProduct ?product .
   ?product :pName ?productName .
}
[QueryItem="hasInvoice"]
PREFIX : <http://www.example.com/dolibarr#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT * {?customer :hasInvoice ?invoice}
[QueryItem="paymentToSalesOrder"]
PREFIX : <http://www.example.com/dolibarr#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT * {?payment :payTo ?salesOrder .
         ?customer :create ?salesOrder .
         ?payment :payDate ?payDate .
}
[QueryItem="shipmentToCustomer"]
PREFIX : <http://www.example.com/dolibarr#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT  ?Shipment ("Shipment" AS ?_value) ("Shipment" AS ?_label) ?SalesOrder
WHERE
  { ?Shipment  <http://www.example.com/dolibarr#shipTo>  ?Customer .
    ?Customer  <http://www.example.com/dolibarr#create>  ?SalesOrder}
[QueryItem="hasInvoice-salesOrder"]
PREFIX : <http://www.example.com/dolibarr#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT  ?Invoice ("Invoice" AS ?_value) ("Invoice" AS ?_label) ?SalesOrder
WHERE
  { ?Customer  <http://www.example.com/dolibarr#hasInvoice>  ?Invoice ;
              <http://www.example.com/dolibarr#create>  ?SalesOrder
}