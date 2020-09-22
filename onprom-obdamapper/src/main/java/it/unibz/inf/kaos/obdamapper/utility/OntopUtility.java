/*
 * onprom-obdamapper
 *
 * OntopUtility.java
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

package it.unibz.inf.kaos.obdamapper.utility;

import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.TargetQueryParserFactory;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.core.impl.RDBMSourceParameterConstants;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.spec.mapping.converter.OldSyntaxMappingConverter;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import org.apache.commons.rdf.api.RDF;
import org.semanticweb.owlapi.formats.PrefixDocumentFormatImpl;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class OntopUtility {
    public static synchronized Properties getDataSourceProperties(File obdaFile) {
        try {
            OldSyntaxMappingConverter converter = new OldSyntaxMappingConverter(new FileReader(obdaFile), obdaFile.getName());
            return converter.getOBDADataSourceProperties().orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static OntopSQLOWLAPIConfiguration getConfiguration(OWLOntology ontology, OBDAModel obdaModel, Properties dataSourceProperties) {
        return OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontology(ontology)
                .ppMapping(obdaModel.generatePPMapping())
                .properties(dataSourceProperties)
                .build();
    }

    public static OBDAModel emptyOBDAModel(OntopMappingSQLAllConfiguration configuration) {
        return new OBDAModel(
                configuration.getInjector().getInstance(SQLPPMappingFactory.class),
                new PrefixDocumentFormatImpl(),
                configuration.getInjector().getInstance(AtomFactory.class),
                configuration.getInjector().getInstance(TermFactory.class),
                configuration.getInjector().getInstance(TypeFactory.class),
                configuration.getInjector().getInstance(TargetAtomFactory.class),
                configuration.getInjector().getInstance(SubstitutionFactory.class),
                configuration.getInjector().getInstance(RDF.class),
                configuration.getInjector().getInstance(TargetQueryParserFactory.class),
                configuration.getInjector().getInstance(SQLPPSourceQueryFactory.class)
        );
    }

    public static OBDAModel getOBDAModel(File obdaFile) {
        Properties properties = new Properties();
        properties.put(RDBMSourceParameterConstants.DATABASE_URL, "");
        properties.put(RDBMSourceParameterConstants.DATABASE_USERNAME, "");
        properties.put(RDBMSourceParameterConstants.DATABASE_PASSWORD, "");
        properties.put(RDBMSourceParameterConstants.DATABASE_DRIVER, "");
        return getOBDAModel(obdaFile, properties);
    }

    public static OBDAModel getOBDAModel(File obdaFile, Properties dataSource) {

        OBDAModel model = emptyOBDAModel(
                OntopSQLOWLAPIConfiguration.defaultBuilder()
                        .nativeOntopMappingFile(obdaFile)
                        .properties(dataSource)
                        .build()
        );

        try {
            OldSyntaxMappingConverter converter = new OldSyntaxMappingConverter(new FileReader(obdaFile), obdaFile.getName());
            model.parseMapping(converter.getOutputReader(), dataSource);
            return model;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveModel(OBDAModel model, File file) {
        try {
            OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer(model.generatePPMapping());
            writer.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
