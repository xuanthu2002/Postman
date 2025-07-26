package postman.util;

import postman.exception.URLFormatException;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URIUtils implements Serializable {
    private String url;

    public URIUtils(String url) {
        this.url = url;
    }

    public static URIUtils of(String url) {
        return new URIUtils(url);
    }

    public static boolean validate(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        String regex = "^(https?://)?" +                    // Protocol
                "(([Ww]){3}\\.)?" +                         // Optional www.
                "(([a-zA-Z0-9-.]+\\.[a-zA-Z]{2,})" +        // example.com
                "|((\\d{1,3}\\.){3}\\d{1,3})" +             // 192.168.1.56
                "|(?i:localhost))" +                        // localhost
                "(:\\d+)?" +                                // Optional port
                "(/.*)?" +                                  // Optional path
                "(\\?.+=.*)?$";                             // Optional query string
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

    public static Map<String, String> extractParams(String url) {
        Map<String, String> params = new LinkedHashMap<>();
        if (url != null && url.contains("?")) {
            String[] query = url.substring(url.indexOf("?") + 1).split("&", -1);
            for (String p : query) {
                String[] kv = p.split("=", 2);
                params.put(kv[0], kv.length > 1 ? kv[1] : "");
            }
        }
        return params;
    }

    public static boolean isRelativePath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return !path.contains("://");
    }

    public String extractHost() {
        String regex = "^(?:[a-zA-Z][a-zA-Z\\d+-.]*://)?([^:/\\s?#]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return url;
    }

    public int extractPort() throws URLFormatException {
        String authority = extractAuthority();
        String hostPort = authority.contains("@") ? authority.split("@")[1] : authority;

        int colonIndex = hostPort.lastIndexOf(':');
        if (colonIndex != -1) {
            String portStr = hostPort.substring(colonIndex + 1);
            try {
                int port = Integer.parseInt(portStr);
                if (port < 0 || port > 65535) {
                    throw new URLFormatException(String.format("Port should be >= 0 and < 65536. Received type string ('%s').", portStr));
                }
                return port;
            } catch (NumberFormatException e) {
                throw new URLFormatException(String.format("Port should be >= 0 and < 65536. Received type string ('%s').", portStr), e);
            }
        }

        return url.startsWith("https://") ? 443 : 80;
    }


    public String extractPath() {
        String regex = "(?<!/)(/)(?!/)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return url.substring(matcher.start(1));
        } else {
            int startParams = url.indexOf('?');
            String query = "";
            if (startParams != -1) {
                query = url.substring(startParams);
            }
            return "/" + query;
        }
    }

    public String extractOrigin() {
        String regex = "(?<!/)(/)(?!/)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return url.substring(0, matcher.start(1));
        }
        return url;
    }

    public String extractAuthority() {
        String regex = "^([a-zA-Z][a-zA-Z\\d+\\-.]*)://([^/?#]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return url;
    }

    public String getRedirect(String direction) {
        if (!isRelativePath(direction)) {
            url = direction;
        } else {
            url = extractOrigin() + direction;
        }
        return url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return this.url;
    }
}
