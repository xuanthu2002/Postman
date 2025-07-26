package postman.gui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class CellPaddingRenderer extends DefaultTableCellRenderer {
    public CellPaddingRenderer() {
        setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (!(c instanceof JComponent)) {
            return c;
        }
        ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        return c;
    }
}
