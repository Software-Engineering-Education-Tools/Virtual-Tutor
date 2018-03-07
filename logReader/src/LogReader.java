import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class LogReader {

    private HashMap<String, Error> errorHashMap;
    private long logTimestamp;
    private String userId;
    private String currentFileName;
    private boolean activeFileChanged;

    private LogWriter writer;


    public static void main(String[] args) {
        LogReader reader = new LogReader();
        if (args.length != 0) {
            reader.read(Paths.get(args[0]));
        } else {

            String subfolderName = "logs";
            reader.read(Paths.get(System.getProperty("user.dir"), subfolderName));

        }

    }

    private LogReader() {
        errorHashMap = new HashMap<>();
        writer = new LogWriter(System.getProperty("user.dir") + "/results", Error.getCsvHeader());

    }

    private void read(Path path) {
        System.out.println("reading from " + path);

        try {
            Files.walk(path)
                    .filter(Files::isRegularFile)
                    .forEach(this::readFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        errorHashMap.values().iterator().forEachRemaining(this::writeErrorToCsv);
    }

    private void writeErrorToCsv(Error error) {
        writer.writeLine(error.toCsvLine());
    }

    private void readFile(Path path) {
        System.out.println("\n\nCURRENT PATH = " + path);
        try {
            JsonStreamParser streamParser = new JsonStreamParser(new FileReader(path.toString()));
            while (streamParser.hasNext()) {
                JsonElement jsonElement = streamParser.next();
                processJsonElement(jsonElement);
            }
            closeErrors();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void closeErrors() {
        for (Error error : errorHashMap.values()) {
            error.close();
        }
    }


    private void processJsonElement(JsonElement jsonElement) {

        this.logTimestamp = jsonElement.getAsJsonObject().getAsJsonPrimitive("logTime").getAsLong();
        JsonObject logEntry = jsonElement.getAsJsonObject().getAsJsonObject("logEntry");
        processLogEntry(logEntry);


    }

    private void processLogEntry(JsonObject logEntry) {
        if (logEntry.getAsJsonObject().has("activeFileChanged")) {
            this.userId = logEntry.getAsJsonPrimitive("userName").getAsString();
            System.out.println("logEntry = " + logEntry);
            this.activeFileChanged = logEntry.getAsJsonPrimitive("activeFileChanged").getAsBoolean();
            if (this.activeFileChanged) {
                setClassActiveFalse();
            }
            this.currentFileName = logEntry.getAsJsonPrimitive("fileName").getAsString();

            //entry is list update
            if (logEntry.getAsJsonObject().has("added")) {
                JsonObject addedErrorsEntry = logEntry.getAsJsonObject("added");
                JsonObject updatedErrorsEntry = logEntry.getAsJsonObject("updated");
                JsonObject removedErrorsEntry = logEntry.getAsJsonObject("removed");
                updateErrorHashmap(this.logTimestamp, addedErrorsEntry, updatedErrorsEntry, removedErrorsEntry);
            }
        } else if (logEntry.getAsJsonObject().has("requestType")) {
            if (logEntry.getAsJsonObject().getAsJsonPrimitive("requestType").getAsString().equals("update")) return;

            String errorId = logEntry.getAsJsonPrimitive("errorId").getAsString();
            if (errorHashMap.containsKey(getKey(errorId))) {
                Error error = errorHashMap.get(getKey(errorId));
                switch (logEntry.getAsJsonPrimitive("requestType").getAsString()) {
                    case "setCursor":
                        error.incrementSetCursorCount(this.logTimestamp);
                        break;
                    case "errorInfo":
                        error.setDetailOpened(this.logTimestamp);
                        break;

                }
            }
        }
    }

    private void setClassActiveFalse() {
        for (Error error : errorHashMap.values()) {
            if (error.getUserId().equals(this.userId) && error.getFilename().equals(this.currentFileName) && !error.isClosed()) {
                error.setClassActive(this.currentFileName, false, this.logTimestamp);
                //error.calculateDisplayed(this.logTimestamp);

            }
        }
    }

    private void updateErrorHashmap(long logTimestamp, JsonObject addedErrorsEntry, JsonObject updatedErrorsEntry, JsonObject removedErrorsEntry) {
        JsonArray addedErrors = addedErrorsEntry.getAsJsonArray("errors");
        JsonArray updatedErrors = updatedErrorsEntry.getAsJsonArray("errors");
        JsonArray removedErrors = removedErrorsEntry.getAsJsonArray("errors");


        if (addedErrors != null) {
            addedErrors.iterator().forEachRemaining(this::addErrorToHashMap);
        }
        if (removedErrors != null) {
            removedErrors.iterator().forEachRemaining(this::updateRemovedErrorInformation);
        }
        if (updatedErrors != null) {
            updatedErrors.iterator().forEachRemaining(this::updateExistingErrorInformation);
        }
    }

    private void updateExistingErrorInformation(JsonElement jsonElement) {
        JsonObject errorJsonObject = jsonElement.getAsJsonObject();
        String errorId = errorJsonObject.getAsJsonPrimitive("errorId").getAsString();
        String key = getKey(errorId);
        int row = errorJsonObject.getAsJsonPrimitive("row").getAsInt();
        if (errorHashMap.containsKey(key) && !errorHashMap.get(key).isSolved()) {
            Error error = errorHashMap.get(key);
            error.setRow(row);

        }


    }

    private void addErrorToHashMap(JsonElement jsonElement) {
        JsonObject errorJsonObject = jsonElement.getAsJsonObject();
        String errorId = errorJsonObject.getAsJsonPrimitive("errorId").getAsString();
        String key = getKey(errorId);

        Error error;
        if (errorHashMap.containsKey(key)) {
            error = errorHashMap.get(key);


        } else {
            error = new Error(errorId, this.userId, this.logTimestamp);
            error.setDelay(errorJsonObject.getAsJsonPrimitive("timeout").getAsInt());
            error.setFilename(this.currentFileName);
            error.setErrorType(errorJsonObject.getAsJsonPrimitive("errorType").getAsString());
            error.setRow(errorJsonObject.getAsJsonPrimitive("row").getAsInt());

            errorHashMap.put(key, error);
        }
        error.setClassActive(this.currentFileName, true, this.logTimestamp);


    }

    private void updateRemovedErrorInformation(JsonElement jsonElement) {
        JsonObject errorJsonObject = jsonElement.getAsJsonObject();
        String errorId = errorJsonObject.getAsJsonPrimitive("errorId").getAsString();
        String key = getKey(errorId);
        if (errorHashMap.containsKey(key)) {
            Error solvedError = errorHashMap.get(key);
            solvedError.setSolved(this.logTimestamp);
            solvedError.setClassActive(this.currentFileName, false, this.logTimestamp);
            errorHashMap.remove(key);
            errorHashMap.put(key + "_solved_" + solvedError.getErrorNum(), solvedError);
            System.out.println("ERROR SOLVED: " + key + " " + this.logTimestamp);
        }


    }

    private String getKey(String errorId) {
        return this.currentFileName + this.userId + errorId;
    }


}
