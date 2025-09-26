package postman.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Storage {

    private static final Logger log = LoggerFactory.getLogger(Storage.class);

    public static void exportRequest(HttpRequestStorage httpRequestStorage, String direct) {
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(Paths.get(direct)))) {
            out.writeObject(httpRequestStorage);
        } catch (Exception ex) {
            log.error("Failed to export request {}", httpRequestStorage, ex);
        }
    }

    public static HttpRequestStorage importRequest(String direct) {
        try (ObjectInputStream inp = new ObjectInputStream(Files.newInputStream(Paths.get(direct)))) {
            return (HttpRequestStorage) inp.readObject();
        } catch (Exception ex) {
            log.error("Failed to import request at : {}", direct, ex);
        }
        return null;
    }

    private Storage() {
    }
}
