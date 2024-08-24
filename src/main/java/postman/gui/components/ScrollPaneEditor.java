package postman.gui.components;

import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

public class ScrollPaneEditor extends RTextScrollPane {
    public ScrollPaneEditor() {
        super();
        this.setLineNumbersEnabled(true);
    }

    public ScrollPaneEditor(RTextArea textArea) {
        super(textArea);
    }
}
