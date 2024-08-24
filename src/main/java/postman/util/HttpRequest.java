package postman.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class HttpRequest {

    private final Map<String, String> headers = new LinkedHashMap<>();
    private HttpMethod method;
    private String url;
    private byte[] body;

    public HttpRequest() {
    }

    public HttpRequest(HttpMethod method, String url) {
        this.method = method;
        this.url = url;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getStringHeaders() {
        StringBuilder headerStr = new StringBuilder();
        headerStr.append(method).append(" ").append(HttpUtils.extractPath(url)).append(" HTTP/1.0").append("\r\n");
        headerStr.append("Host: ").append(HttpUtils.extractHost(url)).append("\r\n");
        headers.forEach((key, value) -> headerStr.append(key).append(": ").append(headers.get(key)).append("\r\n"));
        return headerStr.toString();
    }

    @Override
    public String toString() {
        StringBuilder request = new StringBuilder();
        request.append(method).append(" ").append(HttpUtils.extractPath(url)).append(" HTTP/1.0").append("\r\n");
        request.append("Host: ").append(HttpUtils.extractHost(url)).append("\r\n");
        headers.forEach((key, value) ->
                request.append(key).append(": ").append(headers.get(key)).append("\r\n")
        );
        request.append("\r\n");
        if (body != null) {
            request.append(new String(body)).append("\r\n");
        }
        return request.toString();
    }
}
