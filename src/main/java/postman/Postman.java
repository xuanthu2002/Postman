package postman;

import postman.gui.PostmanView;

import java.io.IOException;

public class Postman {

    public static void main(String[] args) throws IOException {
//        System.setProperty("sun.java2d.uiScale", "1.0");
        new PostmanView().setVisible(true);
    }
}
