package postman.util;

import postman.exception.URLFormatException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class HttpClient {

    static byte[] blankLine = "\r\n\r\n".getBytes();

    public HttpResponse send(HttpRequest request) throws URLFormatException, IOException {
        return send(request, 3);
    }

    public HttpResponse send(HttpRequest request, int limitRetry) throws IOException, URLFormatException {
        try (
                Socket socket = createSocket(request);
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream()
        ) {
            out.write(request.getStringHeaders().getBytes());
            out.write("\r\n".getBytes());

            if (request.getBody() != null) {
                out.write(request.getBody());
            }
            out.flush();

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int b;
            while ((b = in.read()) != -1) {
                buffer.write(b);
            }

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

            getResponseStatus(reader, response);
            getResponseHeaders(reader, response);

            if (300 <= response.getStatusCode() && response.getStatusCode() < 400 && limitRetry > 0) {
                List<String> redirects = response.getHeader("Location");
                if (redirects != null) {
                    String redirectUrl = redirects.get(0);
                    request.setUrl(HttpUtils.getRedirectUrl(request.getUrl(), redirectUrl));
                    return send(request, limitRetry - 1);
                }
            }

            response.setBody(bodyBytes);
            getResponseCookies(response);

            return response;
        }
    }

    private static Socket createSocket(HttpRequest request) throws IOException, URLFormatException {
        if (request.getUrl().startsWith("https://")) {
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(
                    HttpUtils.extractHost(request.getUrl()),
                    HttpUtils.extractPort(request.getUrl())
            );
            sslSocket.startHandshake();
            return sslSocket;
        }
        return new Socket(
                HttpUtils.extractHost(request.getUrl()),
                HttpUtils.extractPort(request.getUrl())
        );
    }

    private static void getResponseCookies(HttpResponse response) {
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
                    case "expires" -> cookie.setExpires(value);
                    case "path" -> cookie.setPath(value);
                    case "domain" -> cookie.setDomain(value.startsWith(".") ? value.substring(1) : value);
                    case "httponly" -> cookie.setHttp(true);
                    case "secure" -> cookie.setSecure(true);
                    case "samesite" -> cookie.setSameSite(true);
                    case "max-age" -> {
                    }
                    default -> {
                        cookie.setKey(key);
                        cookie.setValue(value);
                    }
                }
            }
            cookies.add(cookie);
        }
        response.setCookies(cookies);
    }

    private static void getResponseHeaders(BufferedReader reader, HttpResponse response) throws IOException {
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            if (line.contains(": ") && !line.contains("\"")) {
                String[] headerParts = line.split(": ", 2);
                response.addHeader(headerParts[0], headerParts[1]);
            }
        }
    }

    private static void getResponseStatus(BufferedReader reader, HttpResponse response) throws IOException {
        String line;
        line = reader.readLine();
        String[] parts = line.split(" ");
        response.setStatusCode(Integer.parseInt(parts[1]));
        String statusMessage = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));
        response.setStatusMessage(statusMessage);
    }
}
