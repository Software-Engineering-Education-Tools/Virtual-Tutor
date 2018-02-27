public class Error extends LogEntry {

    private final RequestType requestType;

    public Error(long logTimestamp, RequestType requestType) {
        super(logTimestamp);
        this.requestType = requestType;
    }
}
