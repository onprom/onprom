# onprom

The [**onprom**](https://onprom.inf.unibz.it) is an event log extraction tool suite using ontology-based data access (OBDA). It is developed under [KAOS: knowledge-Aware Operational Support](http://kaos.inf.unibz.it) project to support the various phases of the OBDA-based event log extraction framework. It consists of various plug-ins of the [ProM](http://www.promtools.org) extensible process mining framework and a stand-alone toolkit.

## Modules

This project consists of the following modules:

- **onprom-data**: common data classes for modules
- **onprom-umleditor**: UML editor for ontology design and OWL 2 QL export
- **onprom-annoeditor**: Default annotation editor for annotating ontology with event-data annotations derived from XES event ontology 
- **onprom-dynamiceditor**: Dynamic Annotation editor for annotating domain ontologies with annotations derived from custom event ontologies
- **onprom-logextractor**: Log extraction module using mappings, ontology and annotation queries
- **onprom-obdamapper**: OBDA system mapper
- **onprom-plugin**: ProM plugins
- **onprom-toolkit**: stand-alone toolkit, which provides user interface to use all toolchain
- **onprom-maven**: local maven repository for dependencies that are not available in central maven repository

## References
Please visit [**onprom**](http://onprom.inf.unibz.it) web page for more information. You can check and cite the following papers related with **onprom** methodology and toolchain for more information:

* Diego Calvanese, Tahir Emre Kalayci, Marco Montali, and Wil van der Aalst, [Conceptual Schema Transformation in Ontology-based Data Access](https://doi.org/10.1007/978-3-030-03667-6_4), In Proc. of the 21st Int. Conf. on Knowledge Engineering and Knowledge Management (EKAW 2018), vol. 11313 of Lecture Notes in Artificial Intelligence, Springer, 2018.

* Diego Calvanese, Tahir Emre Kalayci, Marco Montali, and Ario Santoso, [OBDA for Log Extraction in Process Mining](http://doi.org/10.1007/978-3-319-61033-7_9), In Reasoning Web: Semantic Interoperability on the Web — 13th Int. Reasoning Web Summer School (RW 2017), vol. 10370 of Lecture Notes in Computer Science, Springer, 2017.

* Diego Calvanese, Tahir Emre Kalayci, Marco Montali, and Stefano Tinella, [Ontology-based Data Access for Extracting Event Logs from Legacy Data: The onprom Tool and Methodology](http://doi.org/10.1007/978-3-319-59336-4_16), In Proc. of 20th Int. Conf. on Business Information Systems (BIS 2017), vol. 288 of Lecture Notes in Business Information Processing, Springer, 2017.

* Diego Calvanese, Tahir Emre Kalayci, Marco Montali, and Ario Santoso, [The onprom Toolchain for Extracting Business Process Logs using Ontology-based Data Access](http://ceur-ws.org/Vol-1920/BPM_2017_paper_207.pdf), In Proc. of the BPM Demo Track, 2017, vol. 1920 of CEUR Workshop Proceedings, 2017.

### We rely on state-of-the-art [ontop](http://ontop.inf.unibz.it/) system for the OBDA. [Ontop](http://ontop.inf.unibz.it/) is a platform to query relational databases as Virtual RDF Graphs using SPARQL. It is fast and packed with features. It implements the query rewriting and unfolding algorithms together with an extensive set of optimization techniques. Please visit [ontop](http://ontop.inf.unibz.it/) page for more information.