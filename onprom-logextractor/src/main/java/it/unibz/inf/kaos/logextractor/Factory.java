package it.unibz.inf.kaos.logextractor;

import java.text.ParseException;

public interface Factory<T, E, L> {
    T createAttribute(String type, String key, String value, E extension) throws ParseException;

    E getPredefinedExtension(String key);

    void addDefaultExtensions(L xlog);
}
