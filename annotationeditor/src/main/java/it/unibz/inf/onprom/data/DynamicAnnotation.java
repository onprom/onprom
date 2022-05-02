
/*
 * annotationeditor
 *
 * DynamicAnnotation.java
 *
 * Copyright (C) 2016-2022 Free University of Bozen-Bolzano
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

package it.unibz.inf.onprom.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unibz.inf.onprom.AnnotationEditor;
import it.unibz.inf.onprom.data.query.AnnotationQuery;
import it.unibz.inf.onprom.data.query.BinaryAnnotationQuery;
import it.unibz.inf.onprom.data.query.UnaryAnnotationQuery;
import it.unibz.inf.onprom.interfaces.AnnotationDiagram;
import it.unibz.inf.onprom.interfaces.AnnotationProperties;
import it.unibz.inf.onprom.io.SimpleQueryExporter;
import it.unibz.inf.onprom.ui.form.DynamicAnnotationForm;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by T. E. Kalayci on 13-Oct-2017.
 */
public class DynamicAnnotation extends Annotation {
    private static final Logger logger = LoggerFactory.getLogger(DynamicAnnotation.class.getName());
    private final Map<String, DynamicNavigationalAttribute> attributeValues = Maps.newLinkedHashMap();
    private final Map<String, Set<DynamicAnnotationAttribute>> relationValues = Maps.newLinkedHashMap();

    private Set<DynamicAttribute> externalURIComponents = Sets.newLinkedHashSet();

    @JsonIgnore
    private final Set<String> uri = Sets.newLinkedHashSet();
    @JsonIgnore
    private final List<AnnotationQuery> queries = Lists.newLinkedList();
    @JsonIgnore
    private final Map<String, ImmutablePair<String, Object>> uriFields = Maps.newLinkedHashMap();
    private UMLClass annotationClass;
    private boolean isLabelPartOfIndex;

    DynamicAnnotation() {
    }

    public DynamicAnnotation(UMLClass _annotationClass, UMLClass _umlClass, AnnotationProperties _properties) {
        super(_umlClass);
        annotationClass = _annotationClass;
        properties = _properties;
    }

    @Override
    public AnnotationProperties getAnnotationProperties() {
        if (properties == null) {
            properties = AnnotationEditor.getAnnotationProperties(annotationClass);
        }
        return super.getAnnotationProperties();
    }

    public DynamicNavigationalAttribute getAttributeValue(String name) {
        return attributeValues.get(name);
    }

    public Set<DynamicAnnotationAttribute> getRelationValue(String name) {
        return relationValues.get(name);
    }

    public Set<DynamicAttribute> getExternalURIComponents() {
        return externalURIComponents;
    }

    public void setExternalURIComponents(Set<DynamicAttribute> _components) {
        this.externalURIComponents = _components;
    }

    public void setAttributeValue(String name, DynamicNavigationalAttribute value) {
        setValue(attributeValues, name, value);
    }

    public void setRelationValue(String name, Set<DynamicAnnotationAttribute> value) {
        setValue(relationValues, name, value);
    }

    private <K, V> void setValue(Map<K, V> map, K key, V value) {
        if (key != null) {
            if (value != null) {
                map.put(key, value);
            } else {
                map.remove(key);
            }
        }
    }

    public UMLClass getAnnotationClass() {
        return annotationClass;
    }

    @Override
    public List<AnnotationQuery> getQuery() {
        //TODO check cyclic access
        //TODO when to check if visited or not?
        logger.info("Generating queries for " + this);
        queries.clear();
        uriFields.clear();
        uri.clear();

        logger.info("\tpreparing URI fields");

        // FIXME: check if this is needed!!!!
        uri.add(getVarName());

        if (isLabelPartOfIndex) {
            String id = "_L_" + hashCode();
            uriFields.put("__label__", ImmutablePair.of(id, getLabel()));
            uri.add(id);
        }

        externalURIComponents.forEach(component -> {
            String id = "_E" + uriFields.size() + "_" + hashCode();
            uriFields.put(id, ImmutablePair.of(id, component));
            //TODO shall we keep adding external component to the URI or just leave it to be a part of WHERE clause?
            uri.add(id);
        });

        attributeValues.forEach((key, value) -> {
            if (value.isPartOfURI()) {
                String id = "_I" + uriFields.size() + "_" + hashCode();
                uriFields.put(key, ImmutablePair.of(id, value));
                uri.add(id);
            }
        });

        // FIXME: check if this is needed!!!!   
        // isPartOfURI cannot be saved correctly. HACKY!
        relationValues.forEach((key, value) -> {
            value.forEach(dynamicAnnotationAttribute -> {
                if (dynamicAnnotationAttribute.isPartOfURI()) {
                    String id = dynamicAnnotationAttribute.getVarName();
                    uriFields.put(key, ImmutablePair.of(id, value));
                    uri.add(id);
                }
            });
        });

        queries.add(getAnnotationInstanceQuery());
        attributeValues.forEach((key, value) -> queries.add(getAttributeQuery(key, value)));
        relationValues.forEach((key, value) -> {
            value.forEach(dynamicAnnotationAttribute -> {
                queries.add(getRelationQuery(key, dynamicAnnotationAttribute));
            });
        });

        return queries;
    }

    private BinaryAnnotationQuery getAttributeQuery(String key, DynamicNavigationalAttribute value) {
        logger.info("\tgenerating attribute query for " + key);
        String field = uriFields.containsKey(key) ? uriFields.get(key).left : XESConstants.attValue;
        SelectBuilder builder = SimpleQueryExporter.getStringAttributeQueryBuilder(
                value.getAttribute(), this, null, Var.alloc(field)
        );
        addURIFields(builder, key);
        return new BinaryAnnotationQuery(
                builder.toString(), key,
                uri.toArray(new String[]{}), new String[]{field}
        );
    }

    private BinaryAnnotationQuery getRelationQuery(String key, DynamicAnnotationAttribute value) {
        logger.info("\tgenerating relation query for " + key);
        SelectBuilder builder = new SelectBuilder();
        final Var classVar = Var.alloc(getVarName());
        Var relationVar = Var.alloc(value.getVarName());
        boolean inheritanceExist = value.getRelatedClass().equalsOrInherits(relatedClass);
        if (inheritanceExist) {
            relationVar = classVar;
        }
        builder.addVar(classVar);

        if (!inheritanceExist && (value.getPath() != null)) {
            SimpleQueryExporter.addJoin(builder, value.getPath()
                    //, ImmutableMap.of(getCleanName(), getVarName(), value.getRelatedClass().getCleanName(), value.getVarName())
            );
        } else {
            builder.addWhere(classVar, "a", "<" + relatedClass.getLongName() + ">");
            try {
                builder.addVar(value.getVarName());
                if (!value.getVarName().equalsIgnoreCase(getVarName())) {
                    builder.addBind("?" + getVarName(), "?" + value.getVarName());
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        builder.addVar(relationVar);
        addURIFields(builder, key);
        String[] firstComponent = value.getAnnotation().getURI(builder);
        if (firstComponent.length < 1) {
            firstComponent = new String[]{relationVar.getVarName()};
        }
        return new BinaryAnnotationQuery(builder.toString(), key, firstComponent, uri.toArray(new String[]{}));
    }

    private UnaryAnnotationQuery getAnnotationInstanceQuery() {
        // add unary query for this annotation instance
        SelectBuilder builder = new SelectBuilder();
        final Var classVar = Var.alloc(getVarName());
        builder.addVar(classVar);

        builder.addWhere(classVar, "a", "<" + relatedClass.getLongName() + ">");
        addURIFields(builder, "");
        return new UnaryAnnotationQuery(builder.toString(), annotationClass.getLongName(), uri.toArray(new String[]{}));
    }

    private void addURIFields(final SelectBuilder builder, final String currentKey) {
        uriFields.forEach((key, entry) -> {
            if (!key.equals(currentKey)) {
                logger.info("\t\t adding " + key + " to the URI");
                final Var id = Var.alloc(entry.left);
                Object value = entry.right;
                try {
                    if (value instanceof String) {
                        builder.addVar("\"" + value + "\"", id);
                    } else if (value instanceof DynamicNavigationalAttribute) {
                        NavigationalAttribute attribute = ((DynamicNavigationalAttribute) value).getAttribute();
                        if (attribute instanceof ClassAttribute) {
                            if (attribute.getUmlClass().equalsOrInherits(getRelatedClass())) {
                                builder.addBind("?" + getVarName(), id);
                            } else {
                                builder.addBind("?" + attribute.getUmlClass().getCleanName(), id);
                            }
                        } else if (attribute instanceof StringAttribute && attribute.getPath() == null) {
                            builder.addVar("\"" + ((StringAttribute) attribute).getValue() + "\"", id);
                        } else {
                            builder.addVar(id);
                            if (!attribute.getUmlClass().equalsOrInherits(relatedClass)) {
                                SimpleQueryExporter.addJoin(builder, attribute.getPath()
                                        //, ImmutableMap.of(getCleanName(), getVarName())
                                );
                                builder.addWhere("?" + attribute.getUmlClass().getCleanName(), "<" + attribute.getAttribute().getLongName() + ">", id);
                            } else {
                                builder.addWhere("?" + getVarName(), "<" + attribute.getAttribute().getLongName() + ">", id);
                            }
                        }
                    } else if (value instanceof DynamicAnnotationAttribute) {
                        DynamicAnnotationAttribute attribute = (DynamicAnnotationAttribute) value;
                        if (!relatedClass.equalsOrInherits(attribute.getRelatedClass()) && attribute.getPath() != null) {
                            SimpleQueryExporter.addJoin(builder, attribute.getPath()
                                    //,ImmutableMap.of(getCleanName(), getVarName(), attribute.getCleanName(), attribute.getVarName())
                            );
                            builder.addVar(id);
                        } else {
                            if (!builder.getVars().contains(Var.alloc(attribute.getVarName()))) {
                                builder.addVar("?" + attribute.getVarName());
                                builder.addBind("?" + getVarName(), "?" + attribute.getVarName());
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        });
    }

    public List<Attribute> getAnnotationClassAttributes() {
        return annotationClass.getAttributes();
    }

    public Set<Relationship> getAnnotationClassRelations() {
        return annotationClass.getRelations();
    }

    public boolean isLabelPartOfIndex() {
        return isLabelPartOfIndex;
    }

    public void setLabelPartOfIndex(final boolean labelPartOfIndex) {
        isLabelPartOfIndex = labelPartOfIndex;
    }

    private String[] getURI(SelectBuilder builder) {
        getQuery();
        addURIFields(builder, null);
        return uri.toArray(new String[]{});
    }

    @Override
    public String getVarName() {
        return relatedClass.getCleanName() /*+ hashCode()*/;
    }

    @Override
    public java.util.Optional<JPanel> getForm(AnnotationDiagram panel) {
        return java.util.Optional.of(new DynamicAnnotationForm(panel, this));
    }
}

