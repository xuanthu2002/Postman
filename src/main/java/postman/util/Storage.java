package postman.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Storage {

    public static void exportRequest(HttpRequestStorage httpRequestStorage, String direct) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(direct))) {
            out.writeObject(httpRequestStorage);
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public static HttpRequestStorage importRequest(String direct) {
        try (ObjectInputStream inp = new ObjectInputStream(new FileInputStream(direct))) {
            return (HttpRequestStorage) inp.readObject();
        } catch (Exception ex) {
            System.err.println(ex);
        }
        return null;
    }

}
