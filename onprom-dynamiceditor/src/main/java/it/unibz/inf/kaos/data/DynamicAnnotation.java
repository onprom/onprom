package it.unibz.inf.kaos.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unibz.inf.kaos.data.query.AnnotationQuery;
import it.unibz.inf.kaos.data.query.BinaryAnnotationQuery;
import it.unibz.inf.kaos.dynamic.DynamicAnnotationEditor;
import it.unibz.inf.kaos.interfaces.AnnotationDiagram;
import it.unibz.inf.kaos.interfaces.AnnotationProperties;
import it.unibz.inf.kaos.io.SimpleQueryExporter;
import it.unibz.inf.kaos.ui.form.DynamicAnnotationForm;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.sparql.core.Var;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by T. E. Kalayci on 13-Oct-2017.
 */
public class DynamicAnnotation extends Annotation {
    private static final Logger logger = LoggerFactory.getLogger(DynamicAnnotation.class.getName());
    private final Map<String, DynamicNavigationalAttribute> attributeValues = new LinkedHashMap<>();
    private final Map<String, DynamicAnnotationAttribute> relationValues = new LinkedHashMap<>();
    @JsonIgnore
    private final Set<String> uri = Sets.newLinkedHashSet();
    @JsonIgnore
    private final List<AnnotationQuery> queries = Lists.newLinkedList();
    @JsonIgnore
    private final Map<String, Entry> uriFields = Maps.newLinkedHashMap();
    private UMLClass annotationClass;
    private boolean isLabelPartOfIndex;

    DynamicAnnotation() {
    }

    public DynamicAnnotation(UMLClass _umlClass, UMLClass _annotationClass, AnnotationProperties _properties) {
        super(_umlClass);
        annotationClass = _annotationClass;
        properties = _properties;
    }

    @Override
    public AnnotationProperties getAnnotationProperties() {
        if (properties == null) {
            properties = DynamicAnnotationEditor.getAnnotationProperties(annotationClass);
        }
        return super.getAnnotationProperties();
    }

    public DynamicNavigationalAttribute getAttributeValue(String name) {
        return attributeValues.get(name);
    }

    public DynamicAnnotationAttribute getRelationValue(String name) {
        return relationValues.get(name);
    }

    public void setAttributeValue(String name, DynamicNavigationalAttribute value) {
        setValue(attributeValues, name, value);
    }

    public void setRelationValue(String name, DynamicAnnotationAttribute value) {
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
        /*
        //TODO should we or when to check if visited or not?
        if (visited) {
            logger.warn("Already visited " + toString());
            return queries;
        }
        visited = true;
        */
        logger.info("Generating queries for " + toString());
        queries.clear();
        uriFields.clear();
        uri.clear();

        logger.info("\tprepare URI fields");

        if (isLabelPartOfIndex) {
            String id = "_L_" + hashCode();
            uriFields.put("__label__", new Entry(id, getLabel()));
            uri.add(id);
        }

        attributeValues.forEach((k, v) -> {
            if (v.isPartOfIndex()) {
                String id = "_I" + uriFields.size() + "_" + hashCode();
                uriFields.put(k, new Entry(id, v));
                uri.add(id);
            }
        });

        relationValues.forEach((k, v) -> {
            if (v.isPartOfIndex()) {
                String id = v.getVarName();
                uriFields.put(k, new Entry(id, v));
                uri.add(id);
            }
        });
        uri.add(getVarName());

        attributeValues.forEach((key, value) -> {
            logger.info("\tgenerating attribute query for " + key);
            SelectBuilder builder;
            String field;
            if (uriFields.containsKey(key)) {
                field = uriFields.get(key).id;
                builder = SimpleQueryExporter.getStringAttributeQueryBuilder(value, this, null, Var.alloc(field));
            } else {
                field = XESConstants.attValue;
                builder = SimpleQueryExporter.getStringAttributeQueryBuilder(value, this, null);
            }
            addURIFields(builder);
            queries.add(new BinaryAnnotationQuery(
                    builder.toString(), IRI.create(key),
                    uri.toArray(new String[]{}),
                    new String[]{field}));
        });

        relationValues.forEach((key, value) -> {
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
                SimpleQueryExporter.addJoin(builder, value.getPath(), ImmutableMap.of(getCleanName(), getVarName(), value.getRelatedClass().getCleanName(), value.getVarName()));
            } else {
                builder.addWhere(classVar, "a", "<" + relatedClass.getLongName() + ">");
                try {
                    builder.addVar(value.getVarName());
                    builder.addBind("?" + getVarName(), "?" + value.getVarName());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            builder.addVar(relationVar);
            addURIFields(builder);
            String[] firstComponent = value.getAnnotation().getURI(builder);
            if (firstComponent.length < 1) {
                firstComponent = new String[]{relationVar.getVarName()};
            }
            queries.add(new BinaryAnnotationQuery(builder.toString(), IRI.create(key), firstComponent, uri.toArray(new String[]{})));
        });
        return queries;
    }

    private void addURIFields(final SelectBuilder builder) {
        uriFields.forEach((key, entry) -> {
            final Var id = Var.alloc(entry.id);
            Object value = entry.value;
            try {
                if (value instanceof String) {
                    builder.addVar("\"" + value + "\"", id);
                } else if (value instanceof DynamicNavigationalAttribute) {
                    DynamicNavigationalAttribute attribute = (DynamicNavigationalAttribute) value;
                    if (attribute.getPath() == null) {
                        builder.addVar("\"" + attribute.getValue() + "\"", id);
                    } else {
                        builder.addVar(id);
                        if (!attribute.getUmlClass().equalsOrInherits(relatedClass)) {
                            SimpleQueryExporter.addJoin(builder, attribute.getPath(), ImmutableMap.of(getCleanName(), getVarName()));
                            builder.addWhere("?" + attribute.getUmlClass().getCleanName(), "<" + attribute.getAttribute().getLongName() + ">", id);
                        } else {
                            builder.addWhere("?" + getVarName(), "<" + attribute.getAttribute().getLongName() + ">", id);
                        }
                    }
                } else if (value instanceof DynamicAnnotationAttribute) {
                    DynamicAnnotationAttribute attribute = (DynamicAnnotationAttribute) value;
                    if (!relatedClass.equalsOrInherits(attribute.getRelatedClass()) && attribute.getPath() != null) {
                        SimpleQueryExporter.addJoin(builder, attribute.getPath(),
                                ImmutableMap.of(getCleanName(), getVarName(),
                                        attribute.getCleanName(), attribute.getVarName()));
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
        addURIFields(builder);
        return uri.toArray(new String[]{});
    }

    @Override
    public String getVarName() {
        return relatedClass.getCleanName() + hashCode();
    }

    @Override
    public DynamicAnnotationForm getForm(AnnotationDiagram panel) {
        return new DynamicAnnotationForm(panel, this);
    }

    class Entry {
        final String id;
        final Object value;

        Entry(String id, Object value) {
            this.id = id;
            this.value = value;
        }
    }
}
