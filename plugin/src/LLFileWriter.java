package de.ur.mi.roberts;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

/**
 * Created by Jonas Roberts on 04.10.2017.
 */
public class LLFileWriter implements AssistantInteractionListener{

    private File file;
    private BufferedWriter writer;
    private JsonParser jsonParser;


    public LLFileWriter(String fileName) {
        jsonParser = new JsonParser();
        file = new File(System.getProperty("user.home"), "Desktop/Log_" + fileName + ".json");
        boolean isNewFile = true;
        if (file.exists()) {
            isNewFile = false;
        }
        try {
            writer = new BufferedWriter(new java.io.FileWriter(file.getAbsoluteFile(), true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isNewFile) {
           // writeHeader();
        }
    }

    private void writeHeader() {
        try {
            writer.write(CodeError.getHeaderForCSV());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void appendToFile(String message) {
        System.out.println("Appending to Logfile");
        JsonObject logEntry = new JsonObject();
        logEntry.addProperty("logTime", System.currentTimeMillis());
        logEntry.add("logEntry", jsonParser.parse(message));
        if (file.exists()) {
            try {
                writer.write(logEntry.toString());
                System.out.println("logEntry.toString() = " + logEntry.toString());
                writer.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void appendToFile(HashSet<CodeError> errorSet) {
        for (CodeError error : errorSet) {
            appendToFile(error.toSingleLineForCSV());
        }
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(String message) {
        appendToFile(message);
    }

    @Override
    public void onMessageSent(String message) {
        appendToFile(message);
    }
}
