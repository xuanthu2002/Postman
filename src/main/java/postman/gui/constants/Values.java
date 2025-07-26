package postman.gui.constants;

import postman.gui.components.TableCellButtonRenderer;

import java.awt.*;

public class Values {
    public static final int DEFAULT_TABLE_ROW_HEIGHT = 30;
    public static final int BUTTON_SEND_HEIGHT = 32;
    public static final Dimension DEFAULT_LAUNCH_SIZE = new Dimension(470, 700);
    public static final int TAB_SIZE = 4;

//    public static final HttpRequest DEFAULT_REQUEST = getDefaultRequest();
//
//    private static HttpRequest getDefaultRequest() {
//        try {
//            HttpRequest request = new HttpRequest(HttpMethod.GET, new HttpUrl(""));
//            request.putHeader("User-Agent", "Postman");
//            request.putHeader("Accept", "*/*");
//            request.putHeader("Accept-Encoding", "gzip, deflate, br");
//            return request;
//        } catch (URLFormatException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    private Values() {
    }
}
