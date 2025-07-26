package postman.gui.constants;

import java.awt.*;

public class AppFonts {
    private static final PropertiesLoader loader = new PropertiesLoader("/config/fonts.properties");

    public static final Font GENERAL_PLAIN_12 = loader.getFont("font.general.name", "font.general.style.plain", "font.general.size", new Font("SansSerif", Font.PLAIN, 12));
    public static final Font GENERAL_BOLD_12 = loader.getFont("font.general.name", "font.general.style.bold", "font.general.size", new Font("SansSerif", Font.BOLD, 12));
    public static final Font GENERAL_ITALIC_12 = loader.getFont("font.general.name", "font.general.style.italic", "font.general.size", new Font("SansSerif", Font.ITALIC, 12));
}
