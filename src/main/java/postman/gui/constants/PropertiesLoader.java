package postman.gui.constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
    private static final Logger log = LoggerFactory.getLogger(PropertiesLoader.class);
    private final Properties props;

    public PropertiesLoader(String path) {
        props = new Properties();
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                props.load(is);
            } else {
                log.error("⚠️ Cannot find property file: {}", path);
            }
        } catch (Exception e) {
            log.error("⚠️ Failed to load property file: {}", path, e);
        }
    }

    public String getString(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(props.getProperty(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Color getColor(String key, Color defaultColor) {
        try {
            String[] rgb = props.getProperty(key).split(",");
            return new Color(
                    Integer.parseInt(rgb[0].trim()),
                    Integer.parseInt(rgb[1].trim()),
                    Integer.parseInt(rgb[2].trim())
            );
        } catch (Exception e) {
            return defaultColor;
        }
    }

    public Font getFont(String nameKey, String styleKey, String sizeKey, Font defaultFont) {
        try {
            String name = props.getProperty(nameKey);
            int style = Integer.parseInt(props.getProperty(styleKey, "0"));
            int size = Integer.parseInt(props.getProperty(sizeKey, "12"));
            return new Font(name, style, size);
        } catch (Exception e) {
            return defaultFont;
        }
    }
}
