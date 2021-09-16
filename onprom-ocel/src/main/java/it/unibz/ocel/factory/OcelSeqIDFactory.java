package it.unibz.ocel.factory;

import java.util.concurrent.atomic.AtomicLong;

public final class OcelSeqIDFactory {
    private static OcelSeqIDFactory singleton = new OcelSeqIDFactory();
    private AtomicLong counter = new AtomicLong();

    public static OcelSeqIDFactory instance() {
        return singleton;
    }

    private OcelSeqIDFactory() {
    }

    public long nextId() {
        return this.counter.incrementAndGet();
    }
}
