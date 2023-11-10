package postman.model;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.parseInt;

public class HttpRequest {
    
    private HttpMethod method;
    private HttpUrl httpUrl;
    private String path;
    private Map<String, String> headers = new HashMap<>();
    private String body;

    public HttpRequest() {
    }

    public HttpRequest(HttpMethod method, HttpUrl httpUrl, String body) {
        this.method = method;
        this.httpUrl = httpUrl;
        this.path = httpUrl.extractPath();
        setPort(httpUrl.extractPort());
        this.body = body;
    }

    public HttpRequest(HttpMethod method, HttpUrl httpUrl) {
        this.method = method;
        this.httpUrl = httpUrl;
        this.path = httpUrl.extractPath();
        this.headers.put("Host", httpUrl.extractHost());
        setPort(httpUrl.extractPort());
    }

    public HttpRequest(HttpMethod method) {
        this.method = method;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHost() {
        return headers.get("Host");
    }

    public int getPort() {
        return parseInt(headers.get("Port"));
    }

    public void setPort(int port) {
        this.headers.put("Port", String.valueOf(port));
    }

    public HttpUrl getHttpUrl() {
        return httpUrl;
    }

    public void setUrl(HttpUrl httpUrl) {
        this.httpUrl = httpUrl;
        this.path = httpUrl.extractPath();
        this.headers.put("Host", httpUrl.extractHost());
        setPort(httpUrl.extractPort());
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getHeaderValue(String field) {
        return headers.get(field);
    }

    @Override
    public String toString() {
        String request = method + " " + path + " HTTP/1.0\r\n";
        for(String key : headers.keySet()) {
            request += key + ": " + headers.get(key) + "\r\n";
        }
        request += "\r\n";
        request += body == null ? "" : body + "\r\n";
        return request;
    }
}
