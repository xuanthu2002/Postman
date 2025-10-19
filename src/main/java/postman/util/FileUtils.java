package postman.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {

    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    public static void exportRequest(HttpRequestStorage httpRequestStorage, String direct) {
        try (OutputStream out = Files.newOutputStream(Paths.get(direct))) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(out, httpRequestStorage);
        } catch (Exception ex) {
            log.error("Failed to export request {}", httpRequestStorage, ex);
        }
    }

    public static HttpRequestStorage importRequest(String direct) {
        try (InputStream inp = Files.newInputStream(Paths.get(direct))) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(inp, HttpRequestStorage.class);
        } catch (Exception ex) {
            log.error("Failed to import request at : {}", direct, ex);
        }
        return null;
    }

    public static String getUserSelectedFilePath(String title, String direct) {
        return "";
    }

    private FileUtils() {
    }
}
