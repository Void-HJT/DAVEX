package DavexCenter.goSdk.client;

public class GoClientException extends RuntimeException {
    public GoClientException(String message) {
        super(message);
    }

    public GoClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
