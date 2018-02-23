package de.ur.mi.roberts;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Arrays;

/**
 * Created by Jonas Roberts on 04.10.2017.
 */
public class CodeError {
    private final long errorId;
    private final long timestampStart;
    private final long timestampError;
    private final int row;
    private final int column;
    private final String errorSeverity;
    private final String errorMessage;
    private final String parentElement;
    private final String lineContent;
    private final String fileName;
    private final int fileLength;
    private final String userName;
    private int timeout;
    private String errorType;
    private String highlightedText;
    private int lineCount;

    public CodeError(long errorId, long timestampStart, long timestampError, int timeout, int row, int column, String highlightedText, String errorType, String errorMessage, String errorSeverity, String parentElement, String lineContent, String fileName, int fileLength, int lineCount, String userName) {

        this.errorId = errorId;
        this.timestampStart = timestampStart;
        this.timestampError = timestampError;
        this.timeout = timeout;
        this.row = row;
        this.column = column;
        this.highlightedText = highlightedText == null || highlightedText.equals("") ? "**not defined**" : highlightedText;
        this.errorType = errorType;
        this.errorMessage = errorMessage == null || errorMessage.equals("") ? "**not defined**" : errorMessage;
        this.errorSeverity = errorSeverity == null || errorSeverity.equals("") ? "**not defined**" : errorSeverity;
        this.parentElement = parentElement == null || parentElement.equals("") ? "**not defined**" : parentElement;
        this.lineContent = lineContent == null || lineContent.equals("") ? "**not defined**" : lineContent;
        this.fileName = fileName;
        this.fileLength = fileLength;
        this.lineCount = lineCount;
        this.userName = userName;
    }

    public static String getHeaderForCSV() {
        return "errorId\ttimestampStart\ttimestampError\trow\tcolumn\thighlightedText\terrorMessage\terrorSeverity\tparentElement\tlineContent\tfileName\tfileLength\tlineCount\tuserName\n";
    }


    public String toSingleLineForCSV() {
        return errorId + "\t" +
                timestampStart + "\t" +
                timestampError + "\t" +
                row + "\t" +
                column + "\t" +
                highlightedText + "\t" +
                errorMessage + "\t" +
                errorSeverity + "\t" +
                parentElement + "\t" +
                lineContent + "\t" +
                fileName + "\t" +
                fileLength + "\t" +
                lineCount + "\t" +
                userName +
                "\n";
    }

    public JsonObject toSimpleJson() {
        JsonObject json = new JsonObject();
//        JsonArray posArray = new JsonArray();
//        posArray.add(row);
//        posArray.add(column);

        json.addProperty("errorId", this.errorId);
        json.addProperty("timeout", this.timeout);
        json.addProperty("row", this.row);
        json.addProperty("column", this.column);
        json.addProperty("errorMessage", this.errorMessage);
        json.addProperty("errorType", this.errorType);
        json.addProperty("lineContent", this.lineContent);
        return json;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("errorId", errorId);
        json.addProperty("timeout", timeout);
        json.addProperty("timestampStart", timestampStart);
        json.addProperty("timestampError", timestampError);
        json.addProperty("row", row);
        json.addProperty("column", column);
        json.addProperty("errorType", errorType);
        json.addProperty("highlightedText", highlightedText);
        json.addProperty("errorMessage", errorMessage);
        json.addProperty("errorSeverity", errorSeverity);
        json.addProperty("parentElement", parentElement);
        json.addProperty("lineContent", lineContent);
        json.addProperty("fileName", fileName);
        json.addProperty("fileLength", fileLength);
        json.addProperty("lineCount", lineCount);
        json.addProperty("userName", userName);
        return json;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeError error = (CodeError) o;
        return errorId == error.errorId;
    }


    @Override
    public int hashCode() {
        return (int) (errorId ^ (errorId >>> 32));
    }

    public long getErrorTimestamp() {
        return timestampError;
    }

    public long getId() {
        return errorId;
    }

    public String getParentElement() {
        return parentElement;
    }

    public int[] getCursorPos() {
        return new int[]{row, column};
    }

    @Override
    public String toString() {
        return errorMessage;
    }

    public String getLineContent() {
        return this.lineContent;
    }
}
