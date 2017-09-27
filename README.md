# onprom

The **onprom** is an OBDA-based event log extraction tool suite. It is developed under [KAOS: knowledge-Aware Operational Support](http://kaos.inf.unibz.it) project to support the various phases of the OBDA-based event log extraction framework. It consists of various plug-ins of the [ProM](http://www.promtools.org) extensible process mining framework.

## Modules

It consists of the following modules:

- **onprom-data**: common data classes for various projects
- **onprom-umleditor**: UML editor for ontology design and export
- **onprom-annoeditor**: Default annotation editor for annotating ontology with event-data annotations for default XES event ontology 
- **onprom-varianteditor**: Annotation editor for annotating XES event ontology variant with case and event annotations
- **onprom-logextractor**: Log extraction project using mappings, ontology and annotation queries
- **onprom-plugin**: ProM plugins
- **onprom-toolkit**: stand-alone toolkit, which implements all toolchain
- **onprom-maven**: local maven repository for dependencies that are not available in maven repository

## References
Please visit [**onprom**](http://onprom.inf.unibz.it) web page for more information. You can check and cite the following papers related with **onprom** methodology and toolchain for more information:


* Diego Calvanese, Tahir Emre Kalayci, Marco Montali, and Ario Santoso, [OBDA for Log Extraction in Process Mining](http://doi.org/10.1007/978-3-319-61033-7_9), In Reasoning Web: Semantic Interoperability on the Web — 13th Int. Reasoning Web Summer School (RW 2017), volume 10370 of Lecture Notes in Computer Science, Springer, 2017. DOI: [10.1007/978-3-319-61033-7_9](http://doi.org/10.1007/978-3-319-61033-7_9)

* Diego Calvanese, Tahir Emre Kalayci, Marco Montali, and Stefano Tinella, [Ontology-based Data Access for Extracting Event Logs from Legacy Data: The onprom Tool and Methodology](http://doi.org/10.1007/978-3-319-59336-4_16), In Proc. of 20th Int. Conf. on Business Information Systems (BIS 2017), volume 288 of Lecture Notes in Business Information Processing, pages 220-236, Springer, 2017. DOI: [10.1007/978-3-319-59336-4_16](http://doi.org/10.1007/978-3-319-59336-4_16)