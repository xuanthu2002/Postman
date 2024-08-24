package postman.util;

import postman.exception.URLFormatException;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpUtils implements Serializable {

    public static Map<String, String> extractParams(String url) {
        Map<String, String> params = new LinkedHashMap<>();
        if (url != null && url.contains("?")) {
            String[] tmp = url.substring(url.indexOf("?") + 1).split("&");
            for (String p : tmp) {
                String[] kv = p.split("=", 2);
                params.put(kv[0], kv.length > 1 ? kv[1] : "");
            }
        }
        return params;
    }

    public static String extractHost(String url) {
        String regex = "^(?:https?://)?([^:/\\s]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return url;
    }

    public static int extractPort(String url) throws URLFormatException {
        String authority = extractAuthority(url);

        String regex = ":(?!.*:)(.*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(authority);

        if (matcher.find()) {
            String portStr = matcher.group(1);
            if (portStr != null && !portStr.isEmpty()) {
                try {
                    return Integer.parseInt(portStr);
                } catch (NumberFormatException e) {
                    throw new URLFormatException("Invalid port number format", e);
                }
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
        }
        return "/";
    }

    public static boolean isRelativePath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return !path.contains("://");
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
        String regex = "^([a-zA-Z][a-zA-Z\\d+\\-.]*)://([^/?#]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return url;
    }

    public static String getRedirectUrl(String oldUrl, String newUrl) {
        if (!isRelativePath(newUrl)) {
            return newUrl;
        }
        return extractOrigin(oldUrl) + newUrl;
    }
}
