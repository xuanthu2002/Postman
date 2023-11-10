package postman.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpResponse {
    
    private int statusCode;
    private String statusMessage;
    private Map<String, List<String>> headers = new HashMap<>();
    private String body;
    private List<Cookie> cookies = new ArrayList<>();

    public HttpResponse() {
    }

    public HttpResponse(int statusCode, String statusMessage, String body, List<Cookie> cookies) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.body = body;
        this.cookies = cookies;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public List<String> getHeader(String key) {
        return headers.get(key);
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }
    
    public void addHeader(String key, String value) {
        if(!headers.containsKey(key)) {
            headers.put(key, new ArrayList<>());
        }
        headers.get(key).add(value);
    }
    
    public void addHeaders(String key, List<String> value) {
        if(!headers.containsKey(key)) {
            headers.put(key, new ArrayList<>());
        }
        headers.get(key).addAll(value);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }
    
}
