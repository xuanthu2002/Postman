package postman.gui.components;

import postman.gui.constants.Fonts;
import postman.gui.constants.Values;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EditableTable extends JTable {
    private boolean showDeleteOption;
    private boolean addedDeleteButton;

    public EditableTable() {
        super();
        setFont(Fonts.GENERAL_PLAIN_12);
        setRowHeight(Values.DEFAULT_TABLE_ROW_HEIGHT);
        setCellSelectionEnabled(true);
        setSelectionBackground(getBackground());
        getTableHeader().setReorderingAllowed(false);
        putClientProperty("terminateEditOnFocusLost", true);
    }

    public boolean isShowDeleteOption() {
        return showDeleteOption;
    }

    public void setShowDeleteOption(boolean showDeleteOption) {
        this.showDeleteOption = showDeleteOption;
    }

    @Override
    public void setModel(TableModel model) {
        super.setModel(model);
        if (showDeleteOption) {
            addDeleteButton();
        }

        for (int i = 0; i < getColumnCount(); i++) {
            if (Boolean.class.equals(getModel().getColumnClass(i))) {
                columnModel.getColumn(i).setMinWidth(40);
                columnModel.getColumn(i).setPreferredWidth(50);
                columnModel.getColumn(i).setMaxWidth(120);
            }
        }

        if (showDeleteOption && addedDeleteButton) {
            int deleteCol = getColumnCount() - 1;
            TableCellRenderer deleteButton = getDeleteButtonRenderer();
            setDefaultEditor(getColumnClass(deleteCol), new TableCellButtonEditor(
                    (JButton) deleteButton,
                    row -> {
                        if (row >= 0) {
                            ((DefaultTableModel) getModel()).removeRow(row);
                        }
                    }
            ));
            columnModel.getColumn(deleteCol).setMinWidth(40);
            columnModel.getColumn(deleteCol).setPreferredWidth(50);
            columnModel.getColumn(deleteCol).setMaxWidth(120);
        }
    }

    private void addDeleteButton() {
        if (!addedDeleteButton && getModel() instanceof FlexibleDefaultTableModel) {
            FlexibleDefaultTableModel flexibleDefaultTableModel = (FlexibleDefaultTableModel) getModel();
            TableCellRenderer deleteButton = getDeleteButtonRenderer();
            flexibleDefaultTableModel.addColumn("", JButton.class);
            int deleteCol = flexibleDefaultTableModel.getColumnCount() - 1;
            setDefaultRenderer(flexibleDefaultTableModel.getColumnClass(deleteCol), deleteButton);
            addedDeleteButton = true;
            setModel(flexibleDefaultTableModel);
        }
    }

    public List<Object[]> getTableData() {
        List<Object[]> data = new ArrayList<>();
        for (int i = 0; i < getRowCount(); i++) {
            Object[] row = new Object[getColumnCount()];
            for (int j = 0; j < getColumnCount(); j++) {
                row[j] = getValueAt(i, j);
            }
            data.add(row);
        }
        return data;
    }

    public void setTableData(List<Object[]> data) {
        for (int i = 0; i < data.size(); i++) {
            Object[] rowData = data.get(i);
            for (int j = 0; j < getColumnCount() && j < rowData.length; j++) {
                setValueAt(rowData[j], i, j);
            }
        }
    }

    private TableCellButtonRenderer getDeleteButtonRenderer() {
        TableCellButtonRenderer deleteButton = new TableCellButtonRenderer();
        deleteButton.setText("\uE74D");
        Font iconFont;
        try {
            InputStream is = getClass().getResourceAsStream("/fonts/segmdl2.ttf");
            assert is != null;
            iconFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }
        deleteButton.setFont(iconFont);
        deleteButton.setForeground(Color.RED);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setFocusPainted(false);
        deleteButton.setBorder(null);
        deleteButton.setSize(10, 20);
        return deleteButton;
    }

    @Override
    public TableCellEditor getDefaultEditor(Class<?> columnClass) {
        if (columnClass == String.class) {
            JTextField cell = new JTextField();
            cell.setFont(Fonts.GENERAL_PLAIN_12);
            cell.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            cell.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    int row = getEditingRow();
                    int col = getEditingColumn();
                    if (row < 0 || col < 0) return;
                    String value = cell.getText();
                    setValueAt(value, row, col);
                }
            });
            DefaultCellEditor singleClick = new DefaultCellEditor(cell);
            singleClick.setClickCountToStart(1);
            return singleClick;
        }
        return super.getDefaultEditor(columnClass);
    }

    @Override
    public TableCellRenderer getDefaultRenderer(Class<?> columnClass) {
        if (columnClass == String.class) {
            return new CellPaddingRenderer();
        }
        if (columnClass == Boolean.class) {
            TableCellRenderer renderer = super.getDefaultRenderer(columnClass);
            if (renderer instanceof JCheckBox) {
                ((JCheckBox) renderer).setBorderPainted(false);
            }
            return renderer;
        }
        return super.getDefaultRenderer(columnClass);
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        if (row < getRowCount()) super.setValueAt(aValue, row, column);
    }

    public void clear() {
        DefaultTableModel model = (DefaultTableModel) getModel();
        model.setRowCount(0);
    }
}
