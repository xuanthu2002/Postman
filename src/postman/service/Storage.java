package postman.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import postman.util.HttpRequestStorage;

public class Storage {

    public void export(HttpRequestStorage httpRequestStorage, String direct) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(direct));
            out.writeObject(httpRequestStorage);
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public HttpRequestStorage importPostman(String direct) {
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(direct));
            HttpRequestStorage httpRequestStorage = (HttpRequestStorage) inp.readObject();
            return httpRequestStorage;
        } catch (Exception ex) {
            System.err.println(ex);
        }
        return null;
    }

}
