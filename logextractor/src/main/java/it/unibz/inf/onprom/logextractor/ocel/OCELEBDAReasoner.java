/*
 * onprom-logextractor
 *
 * SimpleEBDAReasoner.java
 *
 * Copyright (C) 2016-2019 Free University of Bozen-Bolzano
 *
 * This product includes software developed under
 * KAOS: Knowledge-Aware Operational Support project
 * (https://kaos.inf.unibz.it).
 *
 * Please visit https://onprom.inf.unibz.it for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibz.inf.onprom.logextractor.ocel;

import it.unibz.inf.onprom.logextractor.EBDAReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.OWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.pm.ocel.entity.OcelAttribute;
import it.unibz.inf.pm.ocel.entity.OcelEvent;
import it.unibz.inf.pm.ocel.entity.OcelObject;
import lombok.Getter;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.time.ZonedDateTime;
import java.util.*;

public class OCELEBDAReasoner extends EBDAReasoner<OcelAttribute, OcelEvent, OcelObject> {
    private static final Logger logger = LoggerFactory.getLogger(OCELEBDAReasoner.class);

    private final OCELFactory factory;

    private final List<ZonedDateTime> timestamps = new ArrayList<>(); //for sorting all the timestamps
    @Getter
    private final Set<String> objectTypes = new HashSet<>();   //for getting all the types of objects
    @Getter
    private final Set<String> attributeNames = new HashSet<>();   //for getting all the attributeNames

    OCELEBDAReasoner(SQLPPMapping obdaModel, Properties dataSourceProperties, OCELFactory factory) throws OWLOntologyCreationException {
        super(obdaModel, dataSourceProperties, OCELConstants.getDefaultEventOntology());
        this.factory = factory;
    }

    public static OCELEBDAReasoner from(SQLPPMapping combinedMapping, Properties properties, OCELFactory factory) {
        try {
            return new OCELEBDAReasoner(combinedMapping, properties, factory);
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
    }

    void printUnfoldedQueries() {
        super.printUnfoldedQueries(new String[]{
                OCELConstants.qAttTypeKeyVal_Simple,
                OCELConstants.qEventAtt_Simple,
                OCELConstants.qObjectAtt_Simple,
                OCELConstants.qEvtObj_Simple
        });
    }

    protected Map<String, OcelAttribute> getAttributes() {
        Map<String, OcelAttribute> attributes = new HashMap<>();
        try {
            OntopOWLStatement st = getStatement();
            long start = System.currentTimeMillis();
            TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qAttTypeKeyVal_Simple);

            logger.info("Finished executing attributes query in {}ms", System.currentTimeMillis() - start);

            start = System.currentTimeMillis();
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                try {
                    String attributeKey = asUnquotedString(result.getOWLObject(OCELConstants.qAtt));
                    String type = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttType).getLiteral();
                    String key = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttKey).getLiteral();
                    String value = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttVal).getLiteral();
                    attributeNames.add(key);
                    if (!attributes.containsKey(attributeKey)) {
                        OcelAttribute attribute = factory.createAttribute(type, key, value);
                        if (attribute != null) {
                            attributes.put(attributeKey, attribute);
                        }
                    }
                    if (attributes.size() % 1000000 == 0) logger.info(attributes.size() + " attributes added!");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            logger.info("Finished extracting {} attributes in {}ms", attributes.size(), System.currentTimeMillis() - start);
            resultSet.close();
            st.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return attributes;
    }

    public Map<String, OcelObject> getObjects(Set<String> interesting, Set<String> staticTypes) throws Exception {
        Map<String, OcelObject> objects = new HashMap<>();
        long start = System.currentTimeMillis();
        extractObjectsAndAttributes(objects, interesting, staticTypes);
        extractObjectAndType(objects, interesting, staticTypes);
        logger.info("Finished extracting {} objects in {}ms", objects.size(), System.currentTimeMillis() - start);
        return objects;
    }

    public Map<String, OcelEvent> getEvents(Set<String> interesting) throws Exception {
        Map<String, OcelEvent> events = new HashMap<>();
        long start = System.currentTimeMillis();
        if (interesting != null && !interesting.isEmpty()) {
            extractEverything(events, interesting);
        } else {
            extractEventsAndObjects(events);
            extractEventsAndAttributes(events);
            extractEventsAndTimestamp(events);
            extractEventsAndActivity(events);
        }

//
//        Set<String> eventIDs = null;
//        if (interesting != null) {
//            // Extract IDs of events
//            eventIDs = new HashSet<>(events.keySet());
//        }

        logger.info("Finished extracting {} events in {}ms", events.size(), System.currentTimeMillis() - start);
        return events;
    }

    private void extractEverything(Map<String, OcelEvent> events, Set<String> interesting) throws Exception {
        try (
                OntopOWLStatement st = getStatement();
                TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qEventsWithEverything(interesting))) {
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                OcelEvent event = processEventResult(events, result);
                String timestamp = result.getOWLLiteral(OCELConstants.qEvtAtt_SimpleAnsVarTimestamp).getLiteral();
                event.setTimestamp(timestamp);
                timestamps.add(event.getTimestamp());
                String activity = result.getOWLLiteral(OCELConstants.qEvtAtt_SimpleAnsVarActivity).getLiteral();
                event.setActivity(activity);
            }
        }
    }

    public List<ZonedDateTime> getAllTimestamps() {
        return timestamps;
    }

    public Map<String, String> getGlobalInfo() {
        Map<String, String> content = new HashMap<>();
        //init global-log
        content.put("ocel:version", "1.0");
        return content;
    }


    private void extractEventsAndObjects(Map<String, OcelEvent> events) throws Exception {
        try (
                OntopOWLStatement st = getStatement();
                TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qEventsWithObjects)) {
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                processEventResult(events, result);
            }
        }
    }

    private OcelEvent processEventResult(Map<String, OcelEvent> events, OWLBindingSet result) throws OWLException {
        String evt = asUnquotedString(result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarEvent));
        OcelEvent event = events.computeIfAbsent(evt, OcelEvent::new);
        String object = asUnquotedString(result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarObject));
        event.getOmap().add(object);
        return event;
    }

    private String asUnquotedString(OWLObject object) {
        return ((OWLNamedIndividual) object).getIRI().toString();
    }

    private void extractEventsAndAttributes(Map<String, OcelEvent> events) throws Exception {
        try (OntopOWLStatement st = getStatement();
             TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qEvents)) {
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                String evt = asUnquotedString(result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarEvent));
                OcelEvent event = events.computeIfAbsent(evt, OcelEvent::new);
                if (result.getOWLObject(OCELConstants.qAtt) != null) {
                    String type = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttType).getLiteral();
                    String key = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttKey).getLiteral();
                    String value = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttVal).getLiteral();
                    event.getVmap().put(key, factory.createAttribute(type, key, value));
                }
            }
        }
    }


    private void extractEventsAndTimestamp(Map<String, OcelEvent> events) throws Exception {
        try (OntopOWLStatement st = getStatement();
             TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qEventsWithTimestamps)) {
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                String evt = asUnquotedString(result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarEvent));
                OcelEvent event = events.computeIfAbsent(evt, OcelEvent::new);
                String timestamp = result.getOWLLiteral(OCELConstants.qEvtAtt_SimpleAnsVarTimestamp).getLiteral();
                event.setTimestamp(timestamp);
                timestamps.add(event.getTimestamp());
            }
        }
    }

    private void extractEventsAndActivity(Map<String, OcelEvent> events) throws Exception {
        try (OntopOWLStatement st = getStatement();
             TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qEventsWithActivities)) {
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                String evt = asUnquotedString(result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarEvent));
                OcelEvent event = events.computeIfAbsent(evt, OcelEvent::new);
                String activity = result.getOWLLiteral(OCELConstants.qEvtAtt_SimpleAnsVarActivity).getLiteral();
                event.setActivity(activity);
            }
        }
    }

    private void extractObjectsAndAttributes(Map<String, OcelObject> objects, Set<String> interesting, Set<String> staticTypes) throws Exception {
        try (OntopOWLStatement st = getStatement();
             TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qObjects(interesting, staticTypes))) {
            while (resultSet.hasNext()) {
                OWLBindingSet result = resultSet.next();
                String obj = asUnquotedString(result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarObject));
                OcelObject object = objects.computeIfAbsent(obj, OcelObject::new);

                if (result.getOWLObject(OCELConstants.qAtt) != null) {
                    String type = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttType).getLiteral();
                    String key = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttKey).getLiteral();
                    String value = result.getOWLLiteral(OCELConstants.qAttTypeKeyVal_SimpleAnsVarAttVal).getLiteral();
                    object.getOvmap().put(key, factory.createAttribute(type, key, value));
                }
            }
        }
    }

    private void extractObjectAndType(Map<String, OcelObject> objects, Set<String> interesting, Set<String> staticTypes) throws Exception {
        try (OntopOWLStatement st = getStatement();
             TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qObjectWithType(interesting, staticTypes))) {
            processObjectResults(resultSet, objects);
        }
    }

    private void processObjectResults(TupleOWLResultSet resultSet, Map<String, OcelObject> objects) throws Exception {
        while(resultSet.hasNext()) {
            OWLBindingSet result = resultSet.next();
            String obj = asUnquotedString(result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarObject));
            OcelObject object = objects.computeIfAbsent(obj, OcelObject::new);
            String type = result.getOWLLiteral(OCELConstants.qType_SimpleAnsVarObject).getLiteral();
            objectTypes.add(type);
            object.setType(type);
        }
    }

    public enum QueryType {
        Select,
        Ask,
        Construct,
        Describe,
        Graph
    }

    public OWLResultSet getResults(String query, QueryType type) {
        try {
            OntopOWLStatement statement = getStatement();
            switch (type) {
                case Select:
                    return statement.executeSelectQuery(query);
                case Ask:
                    return statement.executeAskQuery(query);
                case Construct:
                    return statement.executeConstructQuery(query);
                case Describe:
                    return statement.executeDescribeQuery(query);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
//
//
//    private Set<String> extractObjectFromTypes(Set<String> staticTypes) throws Exception {
//        Map<String, List<String>> typesToObject;
//        File file = new File("cachedTypes.json");
//        boolean exists = file.exists();
//        if (file.exists()) {
//            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
//            if (!UIUtility.confirm("Use cached type to object map created at " + attr.creationTime() + "?")) {
//                exists = false;
//            }
//        }
//        if (!exists) {
//            typesToObject = new HashMap<>();
//            try (OntopOWLStatement st = getStatement();
//                 TupleOWLResultSet resultSet = st.executeSelectQuery(OCELConstants.qObjectWithType(null))) {
//                while(resultSet.hasNext()) {
//                    OWLBindingSet result = resultSet.next();
//                    String obj = asUnquotedString(result.getOWLObject(OCELConstants.qEvtAtt_SimpleAnsVarObject));
//                    String type = result.getOWLLiteral(OCELConstants.qType_SimpleAnsVarObject).getLiteral();
//                    if (!typesToObject.containsKey(type)) typesToObject.put(type, new ArrayList<>());
//                    typesToObject.get(type).add(obj);
//                }
//            }
//            String json = new GsonBuilder().setPrettyPrinting().create().toJson(typesToObject);
//            try(FileWriter cacheWriter = new FileWriter(file)) {
//                cacheWriter.write(json);
//            }
//        } else {
//            Type type = TypeToken.getParameterized(Map.class, String.class, TypeToken.getParameterized(List.class, String.class).getType()).getType();
//            try(FileReader cacheReader = new FileReader(file)) {
//                typesToObject = new Gson().fromJson(cacheReader, type);
//            }
//        }
//        return staticTypes.stream().map(typesToObject::get).flatMap(List::stream).collect(Collectors.toSet());
//    }
}
