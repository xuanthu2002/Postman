package postman.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class HttpRequest {

    private HttpMethod method;
    private String url;
    private Map<String, String> headers = new LinkedHashMap<>();
    private String body;
    private boolean sendingFile = false;

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

    public String getBody() {
        return Optional.ofNullable(body).orElse("");
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

    public String getStringHeaders() {
        StringBuilder headerStr = new StringBuilder();
        headerStr.append(method).append(" ").append(HttpUrl.extractPath(url)).append(" HTTP/1.0").append("\r\n");
        headerStr.append("Host: ").append(HttpUrl.extractHost(url)).append("\r\n");
        for (String key : headers.keySet()) {
            headerStr.append(key).append(": ").append(headers.get(key)).append("\r\n");
        }
        return headerStr.toString();
    }

    public boolean isSendingFile() {
        return sendingFile;
    }

    public void setSendingFile(boolean sendingFile) {
        this.sendingFile = sendingFile;
    }

    @Override
    public String toString() {
        StringBuilder request = new StringBuilder();
        request.append(method).append(" ").append(HttpUrl.extractPath(url)).append(" HTTP/1.0").append("\r\n");
        request.append("Host: ").append(HttpUrl.extractHost(url)).append("\r\n");
        for (String key : headers.keySet()) {
            request.append(key).append(": ").append(headers.get(key)).append("\r\n");
        }
        request.append("\r\n");
        request.append(Optional.ofNullable(body).orElse("")).append("\r\n");
        return request.toString();
    }
}
