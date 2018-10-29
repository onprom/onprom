package it.unibz.inf.kaos.ui.utility;

import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.*;

/**
 * Created by T. E. Kalayci on 20-Nov-2017.
 */
public class DiagramUndoManager {
    private static final UndoManager UNDO_MANAGER = new UndoManager();

    public static void undo() {
        if (UNDO_MANAGER.canUndo()) {
            UNDO_MANAGER.undo();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    public static void redo() {
        if (UNDO_MANAGER.canRedo()) {
            UNDO_MANAGER.redo();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    public static void addEdit(final UndoableEdit edit) {
        UNDO_MANAGER.addEdit(edit);
    }

    public static void discardAllEdits() {
        UNDO_MANAGER.discardAllEdits();
    }
}
