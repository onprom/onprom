package it.unibz.ocel.factory;


import it.unibz.ocel.util.OcelRegistry;

public class OcelFactoryRegistry extends OcelRegistry<OcelFactory> {
    private static OcelFactoryRegistry singleton = new OcelFactoryRegistry();

    public static OcelFactoryRegistry instance() {
        return singleton;
    }

    private OcelFactoryRegistry() {
        this.setCurrentDefault(new OcelFactoryNaiveImpl());
    }

    protected boolean areEqual(OcelFactory a, OcelFactory b) {
        return a.getClass().equals(b.getClass());
    }
}
