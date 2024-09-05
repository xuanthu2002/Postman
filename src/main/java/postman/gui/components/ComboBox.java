package postman.gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ComboBox<T> extends JComboBox<T> {

    public ComboBox() {
        super();
        setRenderer(new DefaultListCellRenderer() {
            private final EmptyBorder padding = new EmptyBorder(5, 5, 5, 5);

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(padding);
                return label;
            }
        });
    }

    public ComboBox(T[] items) {
        this();
        for (T item : items) {
            addItem(item);
        }
    }
}
