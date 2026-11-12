package it.unibz.inf.onprom.cli;

import it.unibz.inf.onprom.logextractor.ocel.OCELEBDAReasoner;
import it.unibz.inf.onprom.logextractor.ocel.OCELFactory;
import it.unibz.inf.onprom.obdamapper.utility.OntopUtility;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.OWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import org.semanticweb.owlapi.model.OWLException;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name = "use")
public class UseCommand implements Callable<Integer> {

    @Spec
    Model.CommandSpec spec;

    @Option(names = {"--domain", "-D"}, required = true, description =
            "Key-value pairs in KEY=VALUE format (no spaces around '=')")
    Map<String, String> defs = new HashMap<>();

    @Option(names={"--query-type", "-T"}, defaultValue = "Select")
    OCELEBDAReasoner.QueryType queryType;

    @Parameters(index = "0", description = "Query")
    String query;

    @Override
    public Integer call() throws Exception {
        DomainPaths domainPaths = DomainPaths.resolveDomainPaths(defs, spec);
        AnnotationCommand.SourceManifestations genCommandArgs = AnnotationCommand.loadCommandArgs(domainPaths,
                true);
        SQLPPMapping combinedMapping = genCommandArgs.domainOBDA();
        OCELFactory factory = new OCELFactory();
        OCELEBDAReasoner reasoner = OCELEBDAReasoner.from(combinedMapping, genCommandArgs.dsProperties(), factory);
        try (OWLResultSet resultSet = reasoner.getResults(query, queryType)) {
            showResults(resultSet);
        }
        return 0;
    }

    private void showResults(OWLResultSet resultSet) {
        switch (queryType) {
            case Select -> {
                TupleOWLResultSet tupleOWLResultSet = (TupleOWLResultSet) resultSet;
                List<Map<String, String>> bindings = new ArrayList<>();
                while (true) {
                    try {
                        if (!tupleOWLResultSet.hasNext()) break;
                    } catch (OWLException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        OWLBindingSet next = tupleOWLResultSet.next();
                        Map<String, String> oneMap = new HashMap<>();
                        if (next != null) {
                            Set<String> bindingNames = next.getBindingNames();
                            for (String bindingName : bindingNames) {
                                String name = next.getBinding(bindingName).getName();
                                String value = next.getBinding(bindingName).getValue().toString();
                                oneMap.put(name, value);
                            }
                            bindings.add(oneMap);
                        } else {
                            break;
                        }
                    } catch (OWLException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println(bindings);
            }
            default -> throw new UnsupportedOperationException("Unsupported query type: " + queryType);
        }
    }
}
