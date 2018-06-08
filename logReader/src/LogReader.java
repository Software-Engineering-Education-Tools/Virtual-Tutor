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
import java.util.Calendar;
import java.util.HashMap;

import static java.util.Calendar.getInstance;

public class LogReader {

    private HashMap<String, Error> allErrorsHashmap;
    private HashMap<String, Error> errorHashMap;
    private long logTimestamp;
    private String userId;
    private String currentFileName;
    private boolean activeFileChanged;

    private LogWriter writer;
    private int experimentDay;


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
        allErrorsHashmap = new HashMap<>();
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
        allErrorsHashmap.values().iterator().forEachRemaining(this::writeErrorToCsv);
    }


    private void writeErrorToCsv(Error error) {
        writer.writeLine(error.toCsvLine());
    }

    private void readFile(Path path) {
        System.out.println("\n\nCURRENT PATH = " + path);
        errorHashMap = new HashMap<>();

        try {
            JsonStreamParser streamParser = new JsonStreamParser(new FileReader(path.toString()));
            while (streamParser.hasNext()) {
                JsonElement jsonElement = streamParser.next();
                processJsonElement(jsonElement);
            }
            errorHashMap.values().iterator().forEachRemaining(this::closeError);
            addToAllErrors();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void closeError(Error error) {
        error.close(this.logTimestamp);
    }


    private void addToAllErrors() {
        allErrorsHashmap.putAll(errorHashMap);
    }


    private void processJsonElement(JsonElement jsonElement) {

        this.logTimestamp = jsonElement.getAsJsonObject().getAsJsonPrimitive("logTime").getAsLong();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(this.logTimestamp);
        this.experimentDay = calendar.get(Calendar.DAY_OF_MONTH);
        System.out.println(calendar.get(Calendar.YEAR));
        //logTimestamp /= 1000;

        JsonObject logEntry = jsonElement.getAsJsonObject().getAsJsonObject("logEntry");
        processLogEntry(logEntry);


    }

    private void processLogEntry(JsonObject logEntry) {
        boolean hasRequestType = logEntry.getAsJsonObject().has("requestType");
        boolean hasActiveFileChanged = logEntry.getAsJsonObject().has("activeFileChanged");
        boolean isPushFromServer = !hasRequestType && hasActiveFileChanged;

        if (isPushFromServer) {
            String userName = logEntry.getAsJsonPrimitive("userName").getAsString();
            this.userId = createUserId(userName);
            System.out.println(userId);

            //System.out.println(logEntry);
            this.activeFileChanged = logEntry.getAsJsonPrimitive("activeFileChanged").getAsBoolean();

            if (this.activeFileChanged) {
                closeErrors();
            }
            //this entry is different file from last entry, therefore set currentFilename after setting classActive false
            this.currentFileName = logEntry.getAsJsonPrimitive("fileName").getAsString();

            //entry is list update
            if (logEntry.getAsJsonObject().has("added")) {
                JsonObject addedErrorsEntry = logEntry.getAsJsonObject("added");
                JsonObject updatedErrorsEntry = logEntry.getAsJsonObject("updated");
                JsonObject removedErrorsEntry = logEntry.getAsJsonObject("removed");
                updateErrorHashmap(this.logTimestamp, addedErrorsEntry, updatedErrorsEntry, removedErrorsEntry);
            }
        } else if (hasRequestType) {
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

    private String createUserId(String userName) {

        return userName + "_" + this.experimentDay;

    }

    private void closeErrors() {
        for (Error error : errorHashMap.values()) {
            if (!error.isClosed()) {
                error.close(this.logTimestamp);
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
        if (updatedErrors != null) {
            updatedErrors.iterator().forEachRemaining(this::updateExistingErrorInformation);
        }
        if (removedErrors != null) {
            removedErrors.iterator().forEachRemaining(this::updateRemovedErrorInformation);
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

        error.open(this.logTimestamp);


    }

    private void updateExistingErrorInformation(JsonElement jsonElement) {
        JsonObject errorJsonObject = jsonElement.getAsJsonObject();
        String errorId = errorJsonObject.getAsJsonPrimitive("errorId").getAsString();
        String key = getKey(errorId);
        int row = errorJsonObject.getAsJsonPrimitive("row").getAsInt();
        if (!errorHashMap.get(key).isSolved()) {
            Error error = errorHashMap.get(key);
            error.setRow(row);

        }


    }

    private void updateRemovedErrorInformation(JsonElement jsonElement) {
        JsonObject errorJsonObject = jsonElement.getAsJsonObject();
        String errorId = errorJsonObject.getAsJsonPrimitive("errorId").getAsString();
        String key = getKey(errorId);
        if (errorHashMap.containsKey(key)) {
            Error solvedError = errorHashMap.get(key);
            if (!this.activeFileChanged) {
                solvedError.setSolved(this.logTimestamp);
                errorHashMap.remove(key);
                errorHashMap.put(key + "_solved_" + solvedError.getErrorNum(), solvedError);
                System.out.println("ERROR SOLVED: " + key + " " + this.logTimestamp);
            }
            solvedError.close(this.logTimestamp); //todo check
        }


    }

    private String getKey(String errorId) {
        System.out.println("KEY: " + this.currentFileName + this.userId + errorId);
        return this.currentFileName + this.userId + "_" + this.experimentDay + "_" + errorId;
    }


}
