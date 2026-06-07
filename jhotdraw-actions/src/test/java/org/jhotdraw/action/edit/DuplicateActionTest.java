/*
 * @(#)DuplicateActionTest.java
 *
 * Copyright (c) 2026 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.action.edit;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import org.jhotdraw.api.gui.EditableComponent;
import org.jhotdraw.datatransfer.ClipboardUtil;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DuplicateActionTest {

    @After
    public void tearDown() {
        ClipboardUtil.setClipboard(null);
    }

    @Test
    public void duplicateDelegatesToEnabledEditableTarget() {
        EditableTarget target = new EditableTarget();
        DuplicateAction action = new DuplicateAction(target);

        action.actionPerformed(event(action));

        assertEquals(1, target.duplicateCount);
        assertEquals(0, target.deleteCount);
        assertEquals(0, target.clearSelectionCount);
        assertEquals(0, target.selectAllCount);
    }

    @Test
    public void duplicateIgnoresDisabledEditableTarget() {
        EditableTarget target = new EditableTarget();
        target.setEnabled(false);
        DuplicateAction action = new DuplicateAction(target);

        action.actionPerformed(event(action));

        assertEquals(0, target.duplicateCount);
    }

    @Test
    public void clearSelectionDelegatesToEnabledEditableTarget() {
        EditableTarget target = new EditableTarget();
        ClearSelectionAction action = new ClearSelectionAction(target);

        action.actionPerformed(event(action));

        assertEquals(1, target.clearSelectionCount);
        assertEquals(0, target.duplicateCount);
    }

    @Test
    public void clearSelectionCollapsesTextSelection() {
        JTextField target = new JTextField("abcdef");
        target.select(2, 5);
        ClearSelectionAction action = new ClearSelectionAction(target);

        action.actionPerformed(event(action));

        assertEquals(target.getSelectionStart(), target.getSelectionEnd());
        assertEquals(2, target.getSelectionStart());
    }

    @Test
    public void copyStillExportsDisabledTargetToClipboard() {
        ClipboardUtil.setClipboard(new Clipboard("test clipboard"));
        CopyTarget target = new CopyTarget();
        target.setEnabled(false);
        CopyAction action = new CopyAction(target);

        action.actionPerformed(event(action));

        assertEquals(1, target.transferHandler.exportCount);
    }

    private ActionEvent event(Object source) {
        return new ActionEvent(source, ActionEvent.ACTION_PERFORMED, "test");
    }

    private static class EditableTarget extends JComponent implements EditableComponent {

        private static final long serialVersionUID = 1L;

        int deleteCount;
        int duplicateCount;
        int selectAllCount;
        int clearSelectionCount;
        boolean selectionEmpty;

        @Override
        public void delete() {
            deleteCount++;
        }

        @Override
        public void duplicate() {
            duplicateCount++;
        }

        @Override
        public void selectAll() {
            selectAllCount++;
        }

        @Override
        public void clearSelection() {
            clearSelectionCount++;
        }

        @Override
        public boolean isSelectionEmpty() {
            return selectionEmpty;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            super.addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            super.removePropertyChangeListener(l);
        }
    }

    private static class CopyTarget extends JComponent {

        private static final long serialVersionUID = 1L;

        final CountingTransferHandler transferHandler = new CountingTransferHandler();

        CopyTarget() {
            setTransferHandler(transferHandler);
        }
    }

    private static class CountingTransferHandler extends TransferHandler {

        private static final long serialVersionUID = 1L;

        int exportCount;

        @Override
        protected Transferable createTransferable(JComponent c) {
            exportCount++;
            return new StringSelection("copied");
        }

        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }
    }
}
