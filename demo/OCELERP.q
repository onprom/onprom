[QueryItem="hasInvoice"]
PREFIX : <http://www.example.com/dolibarr/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>
SELECT ?paymentItem ?invoice WHERE {
  ?paymentItem :hasInvoice ?invoice .
} 
LIMIT 10
[QueryItem="orderInfo"]
PREFIX : <http://www.example.com/dolibarr/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>
SELECT  ?Order ?_value
WHERE
  { ?Order  <http://www.example.com/dolibarr/orderId>  ?_value}
[QueryItem="payment"]
PREFIX : <http://www.example.com/dolibarr/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT  ?Payment ?_I2_1118660728 ?_I0_1118660728
WHERE
  { ?Payment  <http://www.example.com/dolibarr/paymentId>  ?_I2_1118660728 ;
              <http://www.example.com/dolibarr/paymentDate>  ?_I0_1118660728
    BIND(?Payment AS ?_I1_1118660728)
  }
[QueryItem="invoice"]
PREFIX : <http://www.example.com/dolibarr/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT  ?Invoice ?_I1_522715196 ("createInvoice" AS ?_L_522715196) ?_I3_522715196
WHERE
  { ?Invoice  <http://www.example.com/dolibarr/invoiceDate>  ?_I1_522715196
    BIND(?Invoice AS ?_I2_522715196)
    ?Invoice  <http://www.example.com/dolibarr/invoiceId>  ?_I3_522715196
  }
[QueryItem="orderItem"]
PREFIX : <http://www.example.com/dolibarr/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT  ?Product ?Order ("product" AS ?_L_38472302) ?_I1_38472302 ("createOrder" AS ?_L_1317011138) ?_I1_1317011138 ("Order" AS ?_I2_1317011138) ?_I3_1317011138
WHERE
  { ?OrderItem  <http://www.example.com/dolibarr/hasProduct>  ?Product ;
              <http://www.example.com/dolibarr/relatedOrder>  ?Order .
    ?Product  <http://www.example.com/dolibarr/productId>  ?_I1_38472302
    BIND(?Product AS ?_I2_38472302)
    ?Order  <http://www.example.com/dolibarr/orderDate>  ?_I1_1317011138 ;
            <http://www.example.com/dolibarr/orderId>  ?_I3_1317011138
  }
[QueryItem="customerAndShipment"]
PREFIX : <http://www.example.com/dolibarr/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT  ?Customer ?Shipment ("shipmentCustomer" AS ?_L_135455264) ("createShipment" AS ?_L_1377234448)
WHERE
  { ?Shipment  <http://www.example.com/dolibarr/shipTo>  ?Customer}
[QueryItem="customerAndOrder"]
PREFIX : <http://www.example.com/dolibarr/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT  ?Customer ?Order ("orderCustomer" AS ?_L_16156007) ?_I2_16156007 ("createOrder" AS ?_L_597664905) ?_I1_597664905 ("createOrder" AS ?_I2_597664905) ?_I3_597664905
WHERE
  { ?Customer  <http://www.example.com/dolibarr/createOrder>  ?Order
    BIND(?Customer AS ?_I1_16156007)
    ?Customer  <http://www.example.com/dolibarr/customerId>  ?_I2_16156007 .
    ?Order    <http://www.example.com/dolibarr/orderId>  ?_I1_597664905 ;
              <http://www.example.com/dolibarr/orderDate>  ?_I3_597664905
  }
[QueryItem="customerAndInvoice"]
PREFIX : <http://www.example.com/dolibarr/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>
SELECT  ?Customer ?Invoice ?_I1_199587608 ("createInvoice" AS ?_L_606228744) ?_I1_606228744 ?_I3_606228744
WHERE
  {
    ?Customer  <http://www.example.com/dolibarr/customerId>  ?_I1_199587608 .
    ?Invoice  <http://www.example.com/dolibarr/invoiceDate>  ?_I1_606228744
    BIND(?Invoice AS ?_I2_606228744)
    ?Invoice  <http://www.example.com/dolibarr/invoiceId>  ?_I3_606228744
  }
[QueryItem="productAndOrder"]
SELECT  ?Product ?Order ?_I0_1667645517 ?_I1_1667645517 ("createOrder" AS ?_L_1507000671) ?_I1_1507000671 ?_I3_1507000671
WHERE
  { ?OrderItem  <http://www.example.com/dolibarr/hasProduct>  ?Product ;
              <http://www.example.com/dolibarr/relatedOrder>  ?Order .
    ?Product  <http://www.example.com/dolibarr/productId>  ?_I0_1667645517 ;
              <http://www.example.com/dolibarr/productId>  ?_I1_1667645517
    BIND(?Product AS ?_I2_1667645517)
    ?Order  <http://www.example.com/dolibarr/orderDate>  ?_I1_1507000671
    BIND(?Order AS ?_I2_1507000671)
    ?Order  <http://www.example.com/dolibarr/orderId>  ?_I3_1507000671
  }
[QueryItem="hasProduct"]
PREFIX : <http://www.example.com/dolibarr/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

select ?oi ?p ?o where 
{
   ?oi 	:relatedOrder ?o ;
         :hasProduct ?p .
}
[QueryItem="InvoiceActivity"]
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>
PREFIX : <http://onprom.inf.unibz.it/> 
SELECT  ?Invoice ?_I1_1978716820 ("invoice" AS ?_L_1978716820) ?_I2_1978716820 ?_I3_1978716820
WHERE
  { ?Invoice  a                     <http://www.example.com/dolibarr/Invoice>
    BIND(?Invoice AS ?_I1_1978716820)
    ?Invoice  <http://www.example.com/dolibarr/invoiceDate>  ?_I2_1978716820 ;
              <http://www.example.com/dolibarr/invoiceId>  ?_I3_1978716820
  }
[QueryItem="invoiceToCustomer"]
PREFIX : <http://www.example.com/dolibarr/>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX data: <http://www.example.com/dolibarr/data/>
PREFIX obda: <https://w3id.org/obda/vocabulary#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX onprom: <http://kaos.inf.unibz.it/onprom/>

SELECT  ?Customer ?Invoice
WHERE
  { ?Invoice  <http://www.example.com/dolibarr/invoiceTo>  ?Customer}
[QueryItem="e-contains-o"]
SELECT  ?Customer ?Order ("customer" AS ?_L_1234456398) ?_I1_1234456398 ("order" AS ?_L_267449831) ?_I2_267449831 ?_I3_267449831
WHERE
  { ?Customer  <http://www.example.com/dolibarr/createOrder>  ?Order ;
              <http://www.example.com/dolibarr/customerId>  ?_I1_1234456398
    BIND(?Customer AS ?_I2_1234456398)
    BIND(?Order AS ?_I1_267449831)
    ?Order  <http://www.example.com/dolibarr/orderDate>  ?_I2_267449831 ;
            <http://www.example.com/dolibarr/orderId>  ?_I3_267449831
  }