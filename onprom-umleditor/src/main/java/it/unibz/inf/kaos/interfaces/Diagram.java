package it.unibz.inf.kaos.interfaces;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by T. E. Kalayci on 20-Nov-2017.
 */
public interface Diagram {
    boolean isUpdateAllowed();

    Set<DiagramShape> getShapes(boolean forJSON);

    <T extends DiagramShape> T findFirst(Class<T> type);

    <T extends DiagramShape> long count(Class<T> type);

    <T extends DiagramShape> Stream<T> getAll(Class<T> type);

    void setCurrentAction(ActionType actionType);
}
