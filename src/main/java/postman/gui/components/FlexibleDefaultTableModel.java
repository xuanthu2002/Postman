package postman.gui.components;

import javax.swing.table.DefaultTableModel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FlexibleDefaultTableModel extends DefaultTableModel {
    private final List<Class<?>> types;

    public FlexibleDefaultTableModel(Object[][] data, Object[] columnNames, Class<?>[] types) {
        super(data, columnNames);
        this.types = new LinkedList<>(Arrays.asList(types));
    }

    public void addColumn(Object columnName, Class<?> type) {
        super.addColumn(columnName);
        types.add(type);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex >= 0 && columnIndex < types.size()) {
            return types.get(columnIndex);
        }
        return Object.class;
    }

    public void setRow(Object[] data, int row) {
        for (int i = 0; i < data.length && i < getColumnCount(); i++) {
            setValueAt(data[i], row, i);
        }
    }
}
