package postman.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class HttpRequest {

    private Map<String, String> headers = new LinkedHashMap<>();
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

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void putHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getStringHeaders() {
        StringBuilder headerStr = new StringBuilder();
        URIUtils uriUtils = new URIUtils(url);
        headerStr.append(method).append(" ").append(uriUtils.extractPath()).append(" HTTP/1.0").append("\r\n");
        headerStr.append("Host: ").append(uriUtils.extractHost()).append("\r\n");
        headers.forEach((key, value) -> headerStr.append(key).append(": ").append(headers.get(key)).append("\r\n"));
        return headerStr.toString();
    }

    @Override
    public String toString() {
        StringBuilder request = new StringBuilder(getStringHeaders());
        request.append("\r\n");
        if (body != null) {
            request.append(new String(body)).append("\r\n");
        }
        return request.toString();
    }
}
