package postman.gui.components;

import javax.swing.*;
import java.awt.*;
import java.util.EventObject;

public class TableCellButtonEditor extends DefaultCellEditor {
    private final JButton button;
    private final TableCellButtonActionListener actionListener;
    private boolean isPushed;
    private int row;

    public TableCellButtonEditor(JButton button, TableCellButtonActionListener actionListener) {
        super(new JTextField());
        this.button = button;
        this.actionListener = actionListener;
        button.addActionListener(e -> fireEditingStopped());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.row = row;
        this.isPushed = true;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        if (isPushed) {
            actionListener.actionPerformed(row);
            isPushed = false;
        }
        return button;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }
}
