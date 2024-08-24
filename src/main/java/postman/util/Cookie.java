package postman.util;

public class Cookie {

    private String key;
    private String value;
    private String domain;
    private String path;
    private String expires;
    private int size;
    private boolean httpOnly;
    private boolean secure;
    private boolean sameSite;

    public Cookie() {
        this.httpOnly = false;
        this.secure = false;
        this.sameSite = false;
    }

    public Cookie(String key, String value, String domain, String path, String expires, int size, boolean httpOnly, boolean secure, boolean sameSite) {
        this.key = key;
        this.value = value;
        this.domain = domain;
        this.path = path;
        this.expires = expires;
        this.size = size;
        this.httpOnly = httpOnly;
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

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
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
