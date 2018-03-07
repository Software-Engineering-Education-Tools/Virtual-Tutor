public class Error {

    private static int errorNumber;

    private final int errorNum;
    private String errorId;
    private String userId;
    private String filename;
    private String errorType;
    private int delay;
    private int row;

    private long occurredTimestamp;
    private long displayedTimestamp;
    private long solvedTimestamp;
    private long detailFirstOpenedTimestamp;
    private long detailLastOpenedTimestamp;
    private long classActivateTimestamp;

    private boolean solved;
    private boolean displayed;
    private boolean detailOpened;
    private boolean closed;

    private long errorDuration;
    private long displayedDuration;
    private long classActiveDuration;
    private long firstDetailToSolvedDuration;
    private long lastDetailToSolvedDuration;
    private long detailOpenedDelay;


    private int updatedCount;
    private int setCursorCount;
    private int detailOpenedCount;
    private int classActivatedCount;


    public Error(String errorId, String userId, long occurredTimestamp) {
        this.errorId = errorId;
        this.userId = userId;
        this.occurredTimestamp = occurredTimestamp;
        errorNumber++;
        this.errorNum = errorNumber;

    }

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public long getOccurredTimestamp() {
        return occurredTimestamp;
    }

    public void setOccurredTimestamp(long occurredTimestamp) {
        this.occurredTimestamp = occurredTimestamp;
    }

    public long getDisplayedTimestamp() {
        return displayedTimestamp;
    }


    public long getSolvedTimestamp() {
        return solvedTimestamp;
    }

    public void setDetailOpened(long timestamp) {
        if (detailFirstOpenedTimestamp == 0) {
            detailFirstOpenedTimestamp = timestamp;
            detailOpenedDelay = detailFirstOpenedTimestamp - displayedTimestamp;
        }
        detailLastOpenedTimestamp = timestamp;

        this.detailOpened = true;
        detailOpenedCount++;
    }

    public boolean wasDetailOpened() {
        return detailOpened;
    }


    public boolean wasDisplayed() {
        return displayed;
    }


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public void setSolved(long timestamp) {
        this.solved = true;
        this.solvedTimestamp = timestamp;
        this.errorDuration = timestamp - this.occurredTimestamp;
        calculateDisplayed(timestamp);
        calculateDetailToSolvedDurations(timestamp);
        this.closed = true;


    }

    public void close() {
        this.closed = true;
    }

    private void calculateDetailToSolvedDurations(long timestamp) {
        if (detailOpened) {
            firstDetailToSolvedDuration = timestamp - detailFirstOpenedTimestamp;
            lastDetailToSolvedDuration = timestamp - detailLastOpenedTimestamp;
        }


    }

    public void calculateDisplayed(long timestamp) {
        if (classActiveDuration > delay && !displayed) {
            displayedTimestamp = timestamp - (classActiveDuration - delay);
            displayedDuration = timestamp - displayedTimestamp;
            displayed = true;
        }
    }

    public boolean isSolved() {

        return solved;
    }

    public void setDelay(int delay) {
        this.delay = delay * 1000;
    }

    public int getErrorNumber() {
        return errorNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setClassActive(String filename, boolean isActive, long timestamp) {
        if (isActive) {
            this.classActivateTimestamp = timestamp;
            this.classActivatedCount++;
        } else {
            this.classActiveDuration += (timestamp - this.classActivateTimestamp);
            calculateDisplayed(timestamp);
        }
    }

    public int getErrorNum() {
        return errorNum;
    }

    public void setRow(int row) {
        this.row = row;
        updatedCount++;
    }


    public void incrementSetCursorCount(long timestamp) {
        this.setCursorCount++;
    }

    public String toCsvLine() {
        return
                errorNum + "\t" +
                        errorId + "\t" +
                        userId + "\t" +
                        filename + "\t" +
                        errorType + "\t" +
                        delay + "\t" +
                        row + "\t" +
                        occurredTimestamp + "\t" +
                        displayedTimestamp + "\t" +
                        solvedTimestamp + "\t" +
                        detailFirstOpenedTimestamp + "\t" +
                        detailLastOpenedTimestamp + "\t" +
                        solved + "\t" +
                        displayed + "\t" +
                        detailOpened + "\t" +
                        errorDuration + "\t" +
                        displayedDuration + "\t" +
                        classActiveDuration + "\t" +
                        firstDetailToSolvedDuration + "\t" +
                        lastDetailToSolvedDuration + "\t" +
                        detailOpenedDelay + "\t" +
                        updatedCount + "\t" +
                        setCursorCount + "\t" +
                        detailOpenedCount + "\t" +
                        classActivatedCount + "\n"
                ;
    }

    public static String getCsvHeader() {
        return
                "errorNum" + "\t" +
                        "errorId" + "\t" +
                        "userId" + "\t" +
                        "filename" + "\t" +
                        "errorType" + "\t" +
                        "delay" + "\t" +
                        "row" + "\t" +
                        "occurredTimestamp" + "\t" +
                        "displayedTimestamp" + "\t" +
                        "solvedTimestamp" + "\t" +
                        "detailFirstOpenedTimestamp" + "\t" +
                        "detailLastOpenedTimestamp" + "\t" +
                        "solved" + "\t" +
                        "displayed" + "\t" +
                        "detailOpened" + "\t" +
                        "errorDuration" + "\t" +
                        "displayedDuration" + "\t" +
                        "classActiveDuration" + "\t" +
                        "firstDetailToSolvedDuration" + "\t" +
                        "lastDetailToSolvedDuration" + "\t" +
                        "detailOpenedDelay" + "\t" +
                        "updatedCount" + "\t" +
                        "setCursorCount" + "\t" +
                        "detailOpenedCount" + "\t" +
                        "classActivatedCount" + "\n"
                ;
    }

    public boolean isClosed() {
        return closed;
    }


}
