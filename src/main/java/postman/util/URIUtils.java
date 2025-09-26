package postman.util;

import postman.exception.URLFormatException;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URIUtils implements Serializable {
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

    public static String extractHost(String url) {
        String regex = "^(?:[a-zA-Z][a-zA-Z\\d+-.]*://)?([^:/\\s?#]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return url;
    }

    public static int extractPort(String url) throws URLFormatException {
        String authority = extractAuthority(url);
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


    public static String extractPath(String url) {
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

    public static String extractOrigin(String url) {
        String regex = "(?<!/)(/)(?!/)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return url.substring(0, matcher.start(1));
        }
        return url;
    }

    public static String extractAuthority(String url) {
        int index = url.indexOf("://");
        if (index > -1) {
            return extractAuthority(url.substring(index + 3));
        }
        index = url.indexOf("/");
        if (index > -1) {
            return url.substring(0, index);
        }
        index = url.indexOf("?");
        if (index > -1) {
            return url.substring(0, index);
        }
        return url;
    }

    public static String getRedirect(String currentUri, String direction) {
        if (!isRelativePath(direction)) {
            currentUri = direction;
        } else {
            currentUri = extractOrigin(currentUri) + direction;
        }
        return currentUri;
    }

    public static boolean isRelativePath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return !path.contains("://");
    }
}
