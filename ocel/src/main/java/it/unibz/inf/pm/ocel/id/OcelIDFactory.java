package it.unibz.inf.pm.ocel.id;

public class OcelIDFactory {
    private static OcelIDFactory singleton = new OcelIDFactory();

    private OcelIDFactory() {
    }

    public static OcelIDFactory instance() {
        return singleton;
    }

    public synchronized OcelID createId() {
        return new OcelID();
    }
}
