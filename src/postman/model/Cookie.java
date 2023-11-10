package postman.model;

public class Cookie {
    
    private String key;
    private String value;
    private String domain;
    private String path;
    private String expires;
    private int size;
    private boolean http;
    private boolean secure;
    private boolean sameSite;

    public Cookie() {
        this.http = false;
        this.secure = false;
        this.sameSite = false;
    }

    public Cookie(String key, String value, String domain, String path, String expires, int size, boolean http, boolean secure, boolean sameSite) {
        this.key = key;
        this.value = value;
        this.domain = domain;
        this.path = path;
        this.expires = expires;
        this.size = size;
        this.http = http;
        this.secure = secure;
        this.sameSite = sameSite;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isHttp() {
        return http;
    }

    public void setHttp(boolean http) {
        this.http = http;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isSameSite() {
        return sameSite;
    }

    public void setSameSite(boolean sameSite) {
        this.sameSite = sameSite;
    }
    
    
}
