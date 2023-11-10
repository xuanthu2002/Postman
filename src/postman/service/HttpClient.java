package postman.service;

import postman.model.HttpRequest;
import postman.model.HttpResponse;
import postman.model.HttpUrl;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;
import postman.model.Cookie;

public class HttpClient {

    public HttpResponse send(HttpRequest request) throws IOException {
        Socket socket = null;

        if (request.getHttpUrl().getUrl().toLowerCase().startsWith("https")) {
            // Khi sử dụng HTTPS, sử dụng SSLSocketFactory để tạo kết nối
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(request.getHost(), request.getPort());
            sslSocket.startHandshake();
            socket = sslSocket;
        } else {
            // Khi sử dụng HTTP, sử dụng kết nối socket thông thường
            socket = new Socket(request.getHost(), request.getPort());
        }

        OutputStream out = socket.getOutputStream();
        out.write(request.toString().getBytes(StandardCharsets.UTF_8));
        out.flush();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
        );

        HttpResponse httpResponse = new HttpResponse();

        // Đọc dòng trạng thái
        String line;
        line = reader.readLine();
        String[] parts = line.split(" ");
        httpResponse.setStatusCode(Integer.parseInt(parts[1]));
        String statusMessage = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));
        httpResponse.setStatusMessage(statusMessage);

        StringBuilder body = new StringBuilder();
        Map<String, List<String>> headers = new HashMap<>();

        // Đọc headers
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            if (line.contains(": ") && !line.contains("\"")) {
                String[] headerParts = line.split(": ", 2);
                if (!headers.containsKey(headerParts[0])) {
                    headers.put(headerParts[0], new ArrayList<>());
                }
                headers.get(headerParts[0]).add(headerParts[1]);
            }
        }
        httpResponse.setHeaders(headers);

        // Đọc body
        while ((line = reader.readLine()) != null) {
            body.append(line).append("\r\n");
        }
        httpResponse.setBody(body.toString());

        out.close();
        reader.close();
        socket.close();

        if (300 <= httpResponse.getStatusCode() && httpResponse.getStatusCode() < 400) {
            request.setUrl(new HttpUrl(httpResponse.getHeader("Location").get(0)));
            // Gửi lại yêu cầu khi có chuyển hướng
            return send(request);
        }

        // Xử lý cookies
        List<String> strCookies = Optional
                .ofNullable(httpResponse.getHeader("Set-Cookie"))
                .orElse(new ArrayList<>());
        List<Cookie> cookies = new ArrayList<>();
        for (String strCookie : strCookies) {
            Cookie cookie = new Cookie();
            for (String s : strCookie.split(";")) {
                s = s.trim();
                String key = s;
                String value = "";
                if (s.contains("=")) {
                    key = s.substring(0, s.indexOf("="));
                    value = s.substring(s.indexOf("=") + 1);
                }
                switch (key.toLowerCase()) {
                    case "expires":
                        cookie.setExpires(value);
                        break;
                    case "path":
                        cookie.setPath(value);
                        break;
                    case "domain":
                        cookie.setDomain(value.startsWith(".") ? value.substring(1) : value);
                        break;
                    case "httponly":
                        cookie.setHttp(true);
                        break;
                    case "secure":
                        cookie.setSecure(true);
                        break;
                    case "samesite":
                        cookie.setSameSite(true);
                        break;
                    case "max-age":
                        break;
                    default:
                        cookie.setKey(key);
                        cookie.setValue(value);
                        break;
                }
            }
            cookies.add(cookie);
        }
        httpResponse.setCookies(cookies);
        
        return httpResponse;
    }

}
