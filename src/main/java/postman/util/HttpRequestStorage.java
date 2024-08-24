package postman.util;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HttpRequestStorage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1113799434508676095L;
    private HttpMethod method;
    private String url;
    private String bodyType;
    private List<Object[]> headers = new ArrayList<>();
    private List<Object[]> params = new ArrayList<>();
    private String body;
    private String fileName;

    public HttpRequestStorage() {
    }

    public HttpRequestStorage(HttpMethod method, String url, String bodyType, String body, String fileName) {
        this.method = method;
        this.url = url;
        this.bodyType = bodyType;
        this.body = body;
        this.fileName = fileName;
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

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public List<Object[]> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Object[]> headers) {
        this.headers = headers;
    }

    public void addHeader(Object[] header) {
        this.headers.add(header);
    }

    public List<Object[]> getParams() {
        return params;
    }

    public void setParams(List<Object[]> params) {
        this.params = params;
    }

    public void addParam(Object[] param) {
        this.params.add(param);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
