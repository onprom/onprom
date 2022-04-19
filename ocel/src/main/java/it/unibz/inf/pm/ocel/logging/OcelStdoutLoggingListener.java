package it.unibz.inf.pm.ocel.logging;


public class OcelStdoutLoggingListener implements OcelLoggingListener {
    public OcelStdoutLoggingListener() {
    }

    public void log(String message, OcelLogging.Importance importance) {
        System.out.println(message);
    }
}
