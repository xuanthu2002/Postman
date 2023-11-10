package postman.model;

import static java.lang.Integer.parseInt;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpUrl {

    private String url;
    private Map<String, String> param = new LinkedHashMap<>();

    public HttpUrl() {
    }

    public HttpUrl(String url) {
        this.url = url;
        if (url.contains("?")) {
            String params[] = url.substring(url.indexOf("?") + 1).split("&");
            for (String p : params) {
                String key = p, value = "";
                if (p.contains("=")) {
                    key = p.substring(0, p.indexOf("="));
                    value = p.substring(p.indexOf("=") + 1);
                }
                this.param.put(key, value);
            }
        }
    }

    public Map<String, String> getParam() {
        return param;
    }

    public void setParam(Map<String, String> param) {
        this.param = param;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String extractHost() {
        String workingUrl = url;
        if (workingUrl.startsWith("http://")) {
            workingUrl = workingUrl.substring(7);
        } else if (workingUrl.startsWith("https://")) {
            workingUrl = workingUrl.substring(8);
        }

        String host = workingUrl.split("/")[0];

        if (host.contains(":")) {
            return host.split(":")[0];
        } else {
            return host;
        }
    }

    public int extractPort() {
        String workingUrl = url;
        // Mặc định là cổng 80 cho HTTP và 443 cho HTTPS
        int defaultPort = url.startsWith("https") ? 443 : 80;

        if (workingUrl.startsWith("http://")) {
            workingUrl = workingUrl.substring(7);
        } else if (workingUrl.startsWith("https://")) {
            workingUrl = workingUrl.substring(8);
        }
        String host = workingUrl.split("/")[0];

        if (host.contains(":")) {
            String portStr = host.split(":")[1];
            try {
                return parseInt(portStr);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Port must be a number");
            }
        } else {
            return defaultPort;
        }
    }

    public String extractPath() {
        try {
            String workingUrl = url;
            int startIndex = workingUrl.indexOf("/", workingUrl.indexOf("//") + 2);

            if (startIndex >= 0) {
                return workingUrl.substring(startIndex);
            }
        } catch (StringIndexOutOfBoundsException e) {
            throw e; // Ném ngoại lệ cho phần khác của ứng dụng xử lý
        }
        return "/";
    }

    @Override
    public String toString() {
        return url;
    }
}
