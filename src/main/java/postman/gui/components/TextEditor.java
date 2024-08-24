package postman.gui.components;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import static postman.gui.constants.Fonts.GENERAL_PLAIN_12;
import static postman.gui.constants.Values.TAB_SIZE;

public class TextEditor extends RSyntaxTextArea {
    public TextEditor() {
        super();
        this.setFont(GENERAL_PLAIN_12);
        this.setTabSize(TAB_SIZE);
        this.setTabsEmulated(true);
        this.setAntiAliasingEnabled(true);
    }
}
