package postman.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postman.exception.URLFormatException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HttpClient {

    private static final Logger log = LoggerFactory.getLogger(HttpClient.class);
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static Future<?> future = null;

    static final byte[] BLANK_LINE = "\r\n\r\n".getBytes();
    static final int DEFAULT_RETRY_TIMES = 3;

    public static void cancel() {
        if (future != null) {
            log.info("Cancelled request");
            future.cancel(true);
        }
    }

    public static void send(HttpRequest request, OnResultListener listener) {
        send(request, DEFAULT_RETRY_TIMES, listener);
    }

    public static void send(HttpRequest request, int limitRetry, OnResultListener listener) {
        log.info("Sending request \r\n{}", request);
        future = executorService.submit(() -> process(request, limitRetry, listener));
    }

    private static void process(HttpRequest request, int limitRetry, OnResultListener listener) {
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

            if (Thread.interrupted()) {
                return;
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int b;
            while ((b = in.read()) != -1) {
                buffer.write(b);
            }

            byte[] responseBytes = buffer.toByteArray();
            int headerLength = responseBytes.length - 4;
            for (int i = 0; i < responseBytes.length - 4; i++) {
                if (Arrays.equals(Arrays.copyOfRange(responseBytes, i, i + 4), BLANK_LINE)) {
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
                List<String> directions = response.getHeader("Location");
                if (directions != null) {
                    String direction = directions.get(0);
                    direction = URIUtils.getRedirect(request.getUrl(), direction);
                    request.setUrl(direction);
                    send(request, limitRetry - 1, listener);
                    return;
                }
            }

            response.setBody(bodyBytes);
            getResponseCookies(response);

            if (Thread.interrupted()) {
                return;
            }

            listener.onSuccess(response);
        } catch (URLFormatException | IOException e) {
            listener.onFailure(e);
        }
    }

    private static Socket createSocket(HttpRequest request) throws IOException, URLFormatException {
        String url = request.getUrl();
        String host = URIUtils.extractHost(url);
        int port = URIUtils.extractPort(url);

        if (url.startsWith("https://")) {
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(
                    host, port
            );
            sslSocket.startHandshake();
            return sslSocket;
        }
        return new Socket(host, port);
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
                    case "expires": {
                        cookie.setExpires(value);
                        break;
                    }
                    case "path": {
                        cookie.setPath(value);
                        break;
                    }
                    case "domain": {
                        cookie.setDomain(value.startsWith(".") ? value.substring(1) : value);
                        break;
                    }
                    case "httponly": {
                        cookie.setHttpOnly(true);
                        break;
                    }
                    case "secure": {
                        cookie.setSecure(true);
                        break;
                    }
                    case "samesite": {
                        cookie.setSameSite(true);
                        break;
                    }
                    case "max-age": {
                        break;
                    }
                    default: {
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


    public interface OnResultListener {
        void onSuccess(HttpResponse response);

        void onFailure(Exception e);
    }

    private HttpClient() {
    }
}
