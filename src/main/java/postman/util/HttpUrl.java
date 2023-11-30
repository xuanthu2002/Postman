package postman.util;

import java.io.Serializable;
import static java.lang.Integer.parseInt;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import postman.exception.URLFormatException;

public class HttpUrl implements Serializable {

    public HttpUrl() {
    }

    public static Map<String, String> extractParams(String url) {
        Map<String, String> params = new LinkedHashMap<>();
        Optional.ofNullable(url).ifPresent((__url) -> {
            if (url.contains("?")) {
                String tmp[] = url.substring(url.indexOf("?") + 1).split("&");
                for (String p : tmp) {
                    String key = p, value = "";
                    if (p.contains("=")) {
                        key = p.substring(0, p.indexOf("="));
                        value = p.substring(p.indexOf("=") + 1);
                    }
                    params.put(key, value);
                }
            }
        });
        return params;
    }

    public static String extractHost(String url) {
        if (url.startsWith("http://")) {
            url = url.substring(7);
        } else if (url.startsWith("https://")) {
            url = url.substring(8);
        }

        String host = url.split("/")[0];

        if (host.contains(":")) {
            return host.split(":")[0];
        }
        return host;
    }

    public static int extractPort(String url) throws URLFormatException {
        // Mặc định là cổng 80 cho HTTP và 443 cho HTTPS
        int port = url.startsWith("https") ? 443 : 80;

        if (url.startsWith("http://")) {
            url = url.substring(7);
        } else if (url.startsWith("https://")) {
            url = url.substring(8);
        }
        String host = url.split("/")[0];

        if (host.contains(":")) {
            String portStr = host.split(":")[1];
            for (char c : portStr.toCharArray()) {
                if (!Character.isDigit(c)) {
                    throw new URLFormatException("Port must be a number");
                }
            }
            port = Integer.parseInt(portStr);
        }

        return port;
    }

    public static String extractPath(String url) {
        try {
            int startIndex = url.indexOf("/", url.indexOf("//") + 2);

            if (startIndex >= 0) {
                return url.substring(startIndex);
            }
        } catch (StringIndexOutOfBoundsException e) {
            throw new StringIndexOutOfBoundsException("Url not have path"); // Ném ngoại lệ cho phần khác của ứng dụng xử lý
        }
        return "/";
    }

}
