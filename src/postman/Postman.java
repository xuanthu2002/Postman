package postman;

import postman.model.HttpMethod;
import postman.model.HttpRequest;
import postman.model.HttpResponse;
import postman.model.HttpUrl;
import postman.service.HttpClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import postman.view.PostmanView;

public class Postman {
    public static void main(String[] args) throws IOException {
        new PostmanView().setVisible(true);
//        String body = "{\"name\": \"product insert postman\", " +
//                "\"imgUrl\": \"google.com\", " +
//                "\"price\": 120000, " +
//                "\"description\": \"\"}";
//        HttpRequest request = new HttpRequest(
//                HttpMethod.GET,
//                new HttpUrl("https://api.openweathermap.org/data/2.5/weather?lat=44.34&lon=10.99&appid=84c5391cd69a5298a2f05497898ff5de")
//        );
//        HttpRequest request = new HttpRequest(HttpMethod.GET, new HttpUrl("localhost:8080/product"));
//        request.setBody(body);
//        request.addHeader("Content-Type", "application/json");
//        request.addHeader("Content-Length", body.length() + "");
//        new HttpClient().send(request);
//        HttpRequest request = new HttpRequest(HttpMethod.GET, new HttpUrl("https://provinces.open-api.vn/api/?depth=3"));
//        HttpRequest request = new HttpRequest(HttpMethod.GET, new HttpUrl("google.com"));
//        HttpClient httpClient = new HttpClient();
//        HttpResponse response = httpClient.send(request);
//        System.out.println(request.toString());
//        System.out.println("Status: ");
//        System.out.println(response.getStatusCode()+" "+response.getStatusMessage());
//        System.out.println("Headers: ");
//        Map<String, List<String>> headers = response.getHeaders();
//        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
//            System.out.println(entry.getKey() + ": " + entry.getValue());
//        }
//        System.out.println("Body: ");
//        System.out.println(response.getBody());
    }
    
}
