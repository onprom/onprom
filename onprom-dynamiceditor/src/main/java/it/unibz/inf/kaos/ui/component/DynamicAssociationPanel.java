package it.unibz.inf.kaos.ui.component;

import it.unibz.inf.kaos.data.*;
import it.unibz.inf.kaos.interfaces.DiagramShape;
import it.unibz.inf.kaos.ui.form.AbstractAnnotationForm;
import it.unibz.inf.kaos.ui.form.DynamicAnnotationForm;
import it.unibz.inf.kaos.ui.utility.AnnotationEditorButtons;
import it.unibz.inf.kaos.ui.utility.UIUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;

/**
 * Created by T. E. Kalayci on 25-Oct-2017.
 */
public class DynamicAssociationPanel extends JPanel {
    private final DynamicAnnotationForm form;
    private final JComboBox<DynamicAnnotationAttribute> cmbAnnotations;
    private final JComboBox<Set<DiagramShape>> cmbPath;
    private final JCheckBox chkIndex;

    public DynamicAssociationPanel(DynamicAnnotationForm _form, Association association) {
        form = _form;
        setLayout(new FlowLayout(FlowLayout.LEADING, 1, 1));

        chkIndex = UIUtility.createCheckBox("part of the URI");
        add(chkIndex);

        add(UIUtility.createLabel(association.getName(), AbstractAnnotationForm.BTN_SIZE, new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                chkIndex.setSelected(!chkIndex.isSelected());
            }
        }));

        cmbAnnotations = UIUtility.createWideComboBox(form.getAnnotations(), AbstractAnnotationForm.TXT_SIZE, e -> populatePath(), true, true);
        add(cmbAnnotations);

        cmbPath = UIUtility.createWideComboBox(AbstractAnnotationForm.TXT_SIZE, null, true, true);
        add(cmbPath);

        JButton btnTraceAdd = UIUtility.createSmallButton(AnnotationEditorButtons.DIAGRAM, e -> form.startNavigation(new UpdateListener() {
            @Override
            public void updateAttribute(Set<DiagramShape> path, UMLClass selectedClass, Attribute selectedAttribute) {
                cmbPath.setSelectedItem(new NavigationalAttribute(path, selectedClass, selectedAttribute));
            }
        }, false));
        add(btnTraceAdd);
    }

    private void populatePath() {
        if ((cmbAnnotations != null) && (cmbAnnotations.getItemCount() > 0) && (cmbAnnotations.getSelectedItem() != null)) {
            DynamicAnnotationAttribute selectedItem = (DynamicAnnotationAttribute) cmbAnnotations.getSelectedItem();
            if (selectedItem != null) {
                UIUtility.loadItems(cmbPath, form.getPaths(selectedItem.getRelatedClass()));
            }
        }
    }

    public DynamicAnnotationAttribute getValue() {
        if (cmbAnnotations.getSelectedItem() != null) {
            DynamicAnnotationAttribute annotation = (DynamicAnnotationAttribute) cmbAnnotations.getSelectedItem();
            if (annotation != null) {
                if (cmbPath.getSelectedIndex() > -1) {
                    annotation.setPath(cmbPath.getItemAt(cmbPath.getSelectedIndex()));
                }
                annotation.setPartOfIndex(chkIndex.isSelected());
            }
            return annotation;
        }
        return null;
    }

    public void setValue(DynamicAnnotationAttribute dynamicAnnotationAttribute) {
        if (dynamicAnnotationAttribute != null) {
            chkIndex.setSelected(dynamicAnnotationAttribute.isPartOfIndex());
            cmbAnnotations.setSelectedItem(dynamicAnnotationAttribute);
            cmbPath.setSelectedItem(dynamicAnnotationAttribute.getPath());
        }
    }

}
