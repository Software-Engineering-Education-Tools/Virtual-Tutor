public class Error {

    private static int errorNumber; //ok

    private final int errorNum; //ok
    private String errorId; // ok
    private String userId; //ok
    private String filename; //ok
    private String errorType; //ok
    private int delay; //ok
    private int row; //ok

    private long occurredTimestamp; //ok
    private long displayedTimestamp; //ok
    private long solvedTimestamp; //ok
    private long detailFirstOpenedTimestamp; //ok
    private long detailLastOpenedTimestamp; //ok
    private long classActivateTimestamp; //ok

    private boolean solved; //ok
    private boolean displayed; //ok
    private boolean detailOpened; //ok
    private boolean closed; //ok

    private long errorDuration; //ok
    private long displayedDuration; //todo
    private long classActiveDuration; //ok
    private long firstDetailToSolvedDuration; //ok
    private long lastDetailToSolvedDuration; //ok
    private long detailOpenedDelay; //ok


    private int updatedCount; //ok
    private int setCursorCount; //ok
    private int detailOpenedCount; //ok
    private int classActivatedCount; //ok


    public Error(String errorId, String userId, long occurredTimestamp) {
        this.errorId = errorId;
        this.userId = userId;
        this.occurredTimestamp = occurredTimestamp;
        errorNumber++;
        this.errorNum = errorNumber;
        this.closed = true;


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
        } else {
            detailLastOpenedTimestamp = timestamp;
        }

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
        calculateDetailToSolvedDurations(timestamp);
        //close(timestamp);


    }

    public void open(long timestamp) {
        this.classActivateTimestamp = timestamp;
        this.classActivatedCount++;
        this.closed = false;
    }

    public void close(long timestamp) {
        this.closed = true;
        this.classActiveDuration += (timestamp - this.classActivateTimestamp);

        System.out.println("classActiveDuration = " + classActiveDuration);

        calculateDisplayed(timestamp);
    }



    private void calculateDetailToSolvedDurations(long timestamp) {
        if (detailOpened) {
            firstDetailToSolvedDuration = timestamp - detailFirstOpenedTimestamp;
            lastDetailToSolvedDuration = timestamp - detailLastOpenedTimestamp;
        }


    }

    public void calculateDisplayed(long timestamp) {
        System.out.println("Error.calculateDisplayed");
        System.out.println("displayed = " + displayed);
        if (classActiveDuration > delay && !displayed) {
            displayedTimestamp = timestamp - (classActiveDuration - delay);
            displayedDuration = timestamp - displayedTimestamp;
            displayed = true;
            System.out.println("first if");
        }
        if (displayed) {
            displayedDuration = classActiveDuration - delay;
            System.out.println("displayedDuration = " + displayedDuration);
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
