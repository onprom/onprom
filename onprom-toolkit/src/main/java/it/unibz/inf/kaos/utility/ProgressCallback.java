package it.unibz.inf.kaos.utility;

public interface ProgressCallback {
    void callback(CallbackByteChannel rbc, double progress);
}
