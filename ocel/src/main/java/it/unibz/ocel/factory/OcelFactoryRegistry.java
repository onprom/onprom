package it.unibz.ocel.factory;


import it.unibz.ocel.util.OcelRegistry;

public class OcelFactoryRegistry extends OcelRegistry<OcelFactory> {
    private static OcelFactoryRegistry singleton = new OcelFactoryRegistry();

    private OcelFactoryRegistry() {
        this.setCurrentDefault(new OcelFactoryNaiveImpl());
    }

    public static OcelFactoryRegistry instance() {
        return singleton;
    }

    protected boolean areEqual(OcelFactory a, OcelFactory b) {
        return a.getClass().equals(b.getClass());
    }
}
