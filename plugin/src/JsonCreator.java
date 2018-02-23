package de.ur.mi.roberts;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jonas Roberts on 16.10.2017.
 */
public class JsonCreator {

    public static JsonObject getSimpleJson(HashMap<Long, CodeError> errorHashMap) {
        JsonObject json = new JsonObject();
        if (!errorHashMap.isEmpty()) {
            ArrayList<CodeError> list = new ArrayList<>(errorHashMap.values());
            json.addProperty("timestamp", list.get(0).getErrorTimestamp());
            json.addProperty("errorCount", list.size());

            JsonArray errorArray = new JsonArray();
            for (CodeError error : list) {
                errorArray.add(error.toSimpleJson());
            }
            json.add("errors", errorArray);
        }
        return json;
    }

    public static JsonObject getJsonForError(String errorId, HashMap<Long, CodeError> errorHashMap) {
        JsonObject errorJson;
        try {
            CodeError error = errorHashMap.get(Long.parseLong(errorId));
            if (error != null) {
                return error.toJson();
            } else {
                errorJson = new JsonObject();
                errorJson.addProperty("error", "There is no Code Error with the id " + errorId);
                return errorJson;
            }
        } catch (NumberFormatException e) {
            errorJson = new JsonObject();
            errorJson.addProperty("error", "Illegal id format. Id must be of type float");
            return errorJson;
        }


    }

    public static JsonObject getJsonForUpdate(String userName, String currentFileName, boolean activeFileChanged, HashMap<Long, CodeError> addedErrorsHashMap, HashMap<Long, CodeError> removedErrorsHashMap, HashMap<Long, CodeError> updatedErrorsHashMap) {
        JsonObject errorJson = new JsonObject();
        errorJson.addProperty("fileName", currentFileName);
        errorJson.addProperty("activeFileChanged", activeFileChanged);
        errorJson.addProperty("userName", userName);
        errorJson.add("added", getSimpleJson(addedErrorsHashMap));
        errorJson.add("updated", getSimpleJson(updatedErrorsHashMap));
        errorJson.add("removed", getSimpleJson(removedErrorsHashMap));
        return errorJson;

    }
}
