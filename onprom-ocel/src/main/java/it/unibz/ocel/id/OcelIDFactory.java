package it.unibz.ocel.id;

public class OcelIDFactory {
    private static OcelIDFactory singleton = new OcelIDFactory();

    public static OcelIDFactory instance() {
        return singleton;
    }

    private OcelIDFactory() {
    }

    public synchronized OcelID createId() {
        return new OcelID();
    }
}
