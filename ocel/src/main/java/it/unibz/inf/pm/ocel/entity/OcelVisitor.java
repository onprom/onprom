package it.unibz.inf.pm.ocel.entity;


import it.unibz.inf.pm.ocel.classification.OcelEventAttributeClassifier;

public abstract class OcelVisitor {
    public OcelVisitor() {
    }

    public boolean precondition() {
        return true;
    }

    public void init(OcelLog log) {
    }

    public void visitLogPre(OcelLog log) {
    }

    public void visitLogPost(OcelLog log) {
    }

    public void visitExtensionPre(OcelExtension ext, OcelLog log) {
    }

    public void visitExtensionPost(OcelExtension ext, OcelLog log) {
    }

    public void visitClassifierPre(OcelEventAttributeClassifier classifier, OcelLog log) {
    }

    public void visitClassifierPost(OcelEventAttributeClassifier classifier, OcelLog log) {
    }



    public void visitObjectPre(OcelObject object,OcelEvent event) {
    }

    public void visitObjectPost(OcelObject object, OcelEvent event) {
    }




    public void visitAttributePre(OcelAttribute attr, OcelAttributable parent) {
    }

    public void visitAttributePost(OcelAttribute attr, OcelAttributable parent) {
    }

    private class OcelEventClassifier {
    }
}