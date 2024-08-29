package postman.util;

import org.junit.jupiter.api.Test;
import postman.exception.URLFormatException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpUrlTest {

    @Test
    void testConstructorValidUrl() {
        assertDoesNotThrow(() -> new HttpUrl("https://example.com"));
        assertDoesNotThrow(() -> new HttpUrl("http://www.example.com"));
        assertDoesNotThrow(() -> new HttpUrl("example.com"));
        assertDoesNotThrow(() -> new HttpUrl("www.example.com"));
        assertDoesNotThrow(() -> new HttpUrl("https://example12322.com"));
        assertDoesNotThrow(() -> new HttpUrl("https://localhost:8080"));
        assertDoesNotThrow(() -> new HttpUrl("https://example12322.com:8080?a=b&c=d"));
    }

    @Test
    void testConstructorInvalidUrl() {
        assertThrows(URLFormatException.class, () -> new HttpUrl("htp://invalid-url"));
        assertThrows(URLFormatException.class, () -> new HttpUrl("://invalid-url"));
        assertThrows(URLFormatException.class, () -> new HttpUrl("https://invalid-url.1232"));
        assertThrows(URLFormatException.class, () -> new HttpUrl("https://example.com:abc"));
        assertThrows(URLFormatException.class, () -> new HttpUrl("https://example:abc"));
    }

    @Test
    void testExtractParams() {
        String url = "https://example.com/page?param1=value1&param2=value2";
        Map<String, String> params = HttpUrl.extractParams(url);
        assertEquals(2, params.size());
        assertEquals("value1", params.get("param1"));
        assertEquals("value2", params.get("param2"));
    }

    @Test
    void testIsRelativePath() {
        assertTrue(HttpUrl.isRelativePath("/path/to/resource"));
        assertFalse(HttpUrl.isRelativePath("https://example.com/path"));
    }

    @Test
    void testExtractHost() throws URLFormatException {
        HttpUrl httpUrl = new HttpUrl("https://example.com:8080/path");
        assertEquals("example.com", httpUrl.extractHost());
    }

    @Test
    void testExtractPortWithExplicitPort() throws URLFormatException {
        HttpUrl httpUrl = new HttpUrl("https://example.com:8080/path");
        assertEquals(8080, httpUrl.extractPort());
    }

    @Test
    void testExtractPortWithoutExplicitPort() throws URLFormatException {
        HttpUrl httpUrl = new HttpUrl("https://example.com/path");
        assertEquals(443, httpUrl.extractPort());

        HttpUrl httpUrl2 = new HttpUrl("http://example.com/path");
        assertEquals(80, httpUrl2.extractPort());
    }

    @Test
    void testExtractPortInvalidPort() {
        assertThrows(URLFormatException.class, () -> {
            HttpUrl httpUrl = new HttpUrl("https://example.com:invalid/path");
            httpUrl.extractPort();
        });
    }

    @Test
    void testExtractPath() throws URLFormatException {
        HttpUrl httpUrl = new HttpUrl("https://example.com:8080/path/to/resource");
        assertEquals("/path/to/resource", httpUrl.extractPath());
    }

    @Test
    void testExtractOrigin() throws URLFormatException {
        HttpUrl httpUrl = new HttpUrl("https://example.com:8080/path/to/resource");
        assertEquals("https://example.com:8080", httpUrl.extractOrigin());
    }

    @Test
    void testExtractAuthority() throws URLFormatException {
        HttpUrl httpUrl = new HttpUrl("https://example.com:8080/path/to/resource");
        assertEquals("example.com:8080", httpUrl.extractAuthority());
    }

    @Test
    void testRedirectAbsoluteUrl() throws URLFormatException {
        HttpUrl httpUrl = new HttpUrl("https://example.com:8080/path/to/resource");
        httpUrl.redirect("https://another.com/path");
        assertEquals("https://another.com/path", httpUrl.getUrl());
    }

    @Test
    void testRedirectRelativePath() throws URLFormatException {
        HttpUrl httpUrl = new HttpUrl("https://example.com:8080/path/to/resource");
        httpUrl.redirect("/new/resource");
        assertEquals("https://example.com:8080/new/resource", httpUrl.getUrl());
    }

    @Test
    void testToString() throws URLFormatException {
        HttpUrl httpUrl = new HttpUrl("https://example.com/path/to/resource");
        assertEquals("https://example.com/path/to/resource", httpUrl.toString());
    }

    @Test
    void testSetUrlValid() throws URLFormatException {
        HttpUrl httpUrl = new HttpUrl("https://example.com/path/to/resource");
        httpUrl.setUrl("https://another.com/new/resource");
        assertEquals("https://another.com/new/resource", httpUrl.getUrl());
    }

    @Test
    void testSetUrlInvalid() {
        assertThrows(URLFormatException.class, () -> {
            HttpUrl httpUrl = new HttpUrl("https://example.com/path/to/resource");
            httpUrl.setUrl("invalid-url");
        });
    }
}
