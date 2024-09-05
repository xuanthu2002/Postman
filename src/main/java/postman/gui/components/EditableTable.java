package postman.gui.components;

import postman.gui.constants.Fonts;
import postman.gui.constants.Values;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class EditableTable extends JTable {

    public EditableTable() {
        super();
        this.setFont(Fonts.GENERAL_PLAIN_12);
        this.setRowHeight(Values.DEFAULT_TABLE_ROW_HEIGHT);
        this.setCellSelectionEnabled(true);
        this.setSelectionBackground(this.getBackground());
        this.putClientProperty("terminateEditOnFocusLost", true);
    }

    @Override
    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);
        setSingleClickToEdit();
    }

    private void setSingleClickToEdit() {
        JTextField cell = new JTextField();
        cell.setFont(Fonts.GENERAL_PLAIN_12);
        DefaultCellEditor singleClick = new DefaultCellEditor(cell);
        singleClick.setClickCountToStart(1);
        for (int i = 1; i < this.getColumnCount(); i++) {
            this.setDefaultEditor(this.getColumnClass(i), singleClick);
        }
    }

    public void clear() {
        DefaultTableModel model = (DefaultTableModel) this.getModel();
        model.setRowCount(0);
    }
}
