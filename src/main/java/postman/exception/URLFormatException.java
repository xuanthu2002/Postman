package postman.exception;

public class URLFormatException extends Exception {

    public URLFormatException(String string) {
        super(string);
    }

    public URLFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
