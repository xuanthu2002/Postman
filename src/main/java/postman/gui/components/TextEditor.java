package postman.gui.components;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;

import static postman.gui.constants.Values.TAB_SIZE;

public class TextEditor extends RSyntaxTextArea {

    private static final Logger log = LoggerFactory.getLogger(TextEditor.class);

    public TextEditor() {
        super();
        try {
            Theme theme = Theme.load(getClass().getResourceAsStream(
                    "/org/fife/ui/rsyntaxtextarea/themes/idea.xml"));
            theme.apply(this);
        } catch (IOException ioe) {
            log.error("Unable to set TextEditor theme", ioe);
        }
        Font font = this.getFont();
        font = new Font(font.getFontName(), font.getStyle(), 12);
        this.setFont(font);
        this.setTabSize(TAB_SIZE);
        this.setTabsEmulated(true);
        this.setAntiAliasingEnabled(true);
    }
}
