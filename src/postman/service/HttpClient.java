package postman.service;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import postman.exception.URLFormatException;
import postman.util.Cookie;
import postman.util.HttpRequest;
import postman.util.HttpResponse;
import postman.util.HttpUrl;

public class HttpClient {

    static byte[] blankLine = "\r\n\r\n".getBytes();

    public HttpResponse send(HttpRequest request) throws IOException, URLFormatException {
        Socket socket = null;

        if (request.getUrl().startsWith("https://")) {
            // Khi sử dụng HTTPS, sử dụng SSLSocketFactory để tạo kết nối
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(
                    HttpUrl.extractHost(request.getUrl()),
                    HttpUrl.extractPort(request.getUrl())
            );
            sslSocket.startHandshake();
            socket = sslSocket;
        } else {
            // Khi sử dụng HTTP, sử dụng kết nối socket thông thường
            socket = new Socket(
                    HttpUrl.extractHost(request.getUrl()),
                    HttpUrl.extractPort(request.getUrl())
            );
        }

        OutputStream out = socket.getOutputStream();

        out.write(request.getStringHeaders().getBytes(StandardCharsets.UTF_8));
        out.write("\r\n".getBytes());

        // Xử lý gửi file
        if (request.isSendingFile()) {
            File file = new File(request.getBody());
            byte[] data = new byte[(int) file.length()];
            FileInputStream fileInput = new FileInputStream(file);
            fileInput.read(data);
            out.write(data);
        } else {
            out.write(request.getBody().getBytes());
        }
        out.flush();

        // get response
        InputStream in = socket.getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            buffer.write(b);
        }
        out.close();
        in.close();
        socket.close();

        byte[] responseBytes = buffer.toByteArray();
        int headerLength = responseBytes.length - 4;
        for (int i = 0; i < responseBytes.length - 4; i++) {
            if (Arrays.equals(Arrays.copyOfRange(responseBytes, i, i + 4), blankLine)) {
                headerLength = i;
                break;
            }
        }

        byte[] headersBytes = Arrays.copyOfRange(responseBytes, 0, headerLength);
        byte[] bodyBytes = Arrays.copyOfRange(responseBytes, headerLength + 4, responseBytes.length);

        HttpResponse response = new HttpResponse();

        ByteArrayInputStream bais = new ByteArrayInputStream(headersBytes);
        BufferedReader reader = new BufferedReader(new InputStreamReader(bais));

        // Đọc dòng trạng thái
        String line;
        line = reader.readLine();
        String[] parts = line.split(" ");
        response.setStatusCode(Integer.parseInt(parts[1]));
        String statusMessage = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));
        response.setStatusMessage(statusMessage);

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
        response.setHeaders(headers);

        // Gửi lại yêu cầu khi có chuyển hướng
        if (300 <= response.getStatusCode() && response.getStatusCode() < 400) {
            request.setUrl(response.getHeader("Location").get(0));
            return send(request);
        }

        response.setBody(bodyBytes);

        // Xử lý cookies
        List<String> strCookies = Optional
                .ofNullable(response.getHeader("Set-Cookie"))
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
        response.setCookies(cookies);

        return response;
    }

}
