package postman.util;

import org.junit.jupiter.api.Test;
import postman.exception.URLFormatException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class URIUtilsTest {
    @Test
    void testExtractParams() {
        String url = "https://example.com/page?param1=value1&param2=value2";
        Map<String, String> params = URIUtils.extractParams(url);
        assertEquals(2, params.size());
        assertEquals("value1", params.get("param1"));
        assertEquals("value2", params.get("param2"));
    }

    @Test
    void testIsRelativePath() {
        assertTrue(URIUtils.isRelativePath("/path/to/resource"));
        assertFalse(URIUtils.isRelativePath("https://example.com/path"));
    }

    @Test
    void testExtractHost() throws URLFormatException {
        URIUtils uriUtils = new URIUtils("https://example.com:8080/path");
        assertEquals("example.com", uriUtils.extractHost());
    }

    @Test
    void testExtractPortWithExplicitPort() throws URLFormatException {
        URIUtils uriUtils = new URIUtils("https://example.com:8080/path");
        assertEquals(8080, uriUtils.extractPort());
    }

    @Test
    void testExtractPortWithoutExplicitPort() throws URLFormatException {
        URIUtils uriUtils = new URIUtils("https://example.com/path");
        assertEquals(443, uriUtils.extractPort());

        URIUtils uriUtils2 = new URIUtils("http://example.com/path");
        assertEquals(80, uriUtils2.extractPort());
    }

    @Test
    void testExtractPortInvalidPort() {
        assertThrows(URLFormatException.class, () -> {
            URIUtils uriUtils = new URIUtils("https://example.com:invalid/path");
            uriUtils.extractPort();
        });
    }

    @Test
    void testExtractPath() throws URLFormatException {
        URIUtils uriUtils = new URIUtils("https://example.com:8080/path/to/resource");
        assertEquals("/path/to/resource", uriUtils.extractPath());
    }

    @Test
    void testExtractOrigin() throws URLFormatException {
        URIUtils uriUtils = new URIUtils("https://example.com:8080/path/to/resource");
        assertEquals("https://example.com:8080", uriUtils.extractOrigin());
    }

    @Test
    void testExtractAuthority() throws URLFormatException {
        URIUtils uriUtils = new URIUtils("https://example.com:8080/path/to/resource");
        assertEquals("example.com:8080", uriUtils.extractAuthority());
    }

    @Test
    void testRedirectAbsoluteUrl() throws URLFormatException {
        URIUtils uriUtils = new URIUtils("https://example.com:8080/path/to/resource");
        uriUtils.getRedirect("https://another.com/path");
        assertEquals("https://another.com/path", uriUtils.getUrl());
    }

    @Test
    void testRedirectRelativePath() throws URLFormatException {
        URIUtils uriUtils = new URIUtils("https://example.com:8080/path/to/resource");
        uriUtils.getRedirect("/new/resource");
        assertEquals("https://example.com:8080/new/resource", uriUtils.getUrl());
    }

    @Test
    void testValidateURL() throws URLFormatException {
        URIUtils uriUtils = new URIUtils("https://example.com/path/to/resource");
        uriUtils.setUrl("https://another.com/new/resource");
        assertEquals("https://another.com/new/resource", uriUtils.getUrl());
    }
}
