package it.unibz.ocel.logging;

public class OcelLogging {
    private static OcelLoggingListener listener = new OcelStdoutLoggingListener();

    public OcelLogging() {
    }

    public static void setListener(OcelLoggingListener listener) {
        OcelLogging.listener = listener;
    }

    public static void log(String message) {
        log(message, OcelLogging.Importance.DEBUG);
    }

    public static void log(String message, OcelLogging.Importance importance) {
        if (listener != null) {
            listener.log(message, importance);
        }

    }

    public static enum Importance {
        DEBUG,
        INFO,
        WARNING,
        ERROR;

        private Importance() {
        }
    }
}
