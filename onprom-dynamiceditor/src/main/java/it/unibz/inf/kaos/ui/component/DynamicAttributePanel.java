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
public class DynamicAttributePanel extends JPanel {
    private final DynamicAnnotationForm form;
    private final JComboBox<DynamicAttribute> cmbAttributes;
    private final JComboBox<Set<DiagramShape>> cmbPath;
    private final JCheckBox chkIndex;

    public DynamicAttributePanel(DynamicAnnotationForm _form, Attribute attribute) {
        form = _form;
        setLayout(new FlowLayout(FlowLayout.LEADING, 1, 1));

        chkIndex = UIUtility.createCheckBox("part of the URI");
        add(chkIndex);

        add(UIUtility.createLabel(attribute.getName(), AbstractAnnotationForm.BTN_SIZE, new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                chkIndex.setSelected(!chkIndex.isSelected());
            }
        }));

        cmbAttributes = UIUtility.createWideComboBox(form.getAttributes(), AbstractAnnotationForm.TXT_SIZE, e -> populatePath(), true, true);
        add(cmbAttributes);

        cmbPath = UIUtility.createWideComboBox(AbstractAnnotationForm.TXT_SIZE, null, false, true);
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
        form.populatePath(cmbAttributes, cmbPath);
    }

    public DynamicNavigationalAttribute getValue() {
        if (cmbAttributes.getSelectedItem() != null) {
            DynamicNavigationalAttribute navigationalAttribute;
            Object attributeValue = cmbAttributes.getSelectedItem();
            if (attributeValue instanceof DynamicNavigationalAttribute) {
                navigationalAttribute = (DynamicNavigationalAttribute) attributeValue;
                if (cmbPath.getSelectedIndex() > -1) {
                    navigationalAttribute.setPath(cmbPath.getItemAt(cmbPath.getSelectedIndex()));
                }
            } else {
                navigationalAttribute = new DynamicNavigationalAttribute(attributeValue.toString());
            }
            navigationalAttribute.setPartOfURI(chkIndex.isSelected());
            return navigationalAttribute;
        }
        return null;
    }

    public void setValue(DynamicNavigationalAttribute navigationalAttribute) {
        if (navigationalAttribute != null) {
            cmbAttributes.setSelectedItem(navigationalAttribute);
            cmbPath.setSelectedItem(navigationalAttribute.getPath());
            chkIndex.setSelected(navigationalAttribute.isPartOfURI());
        }
    }
}
