package postman;

import java.io.IOException;
import postman.gui.PostmanView;

public class Postman {

    public static void main(String[] args) throws IOException {
        System.setProperty("sun.java2d.uiScale", "1.0");
        new PostmanView().setVisible(true);
    }

}
