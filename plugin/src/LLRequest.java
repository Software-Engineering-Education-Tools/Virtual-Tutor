package de.ur.mi.roberts;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Created by Jonas Roberts on 05.02.2018.
 */
public class LLRequest {

    public static final String REQUEST_ERROR_INFO = "errorInfo";
    public static final String REQUEST_SET_CURSOR = "setCursor";
    public static final String REQUEST_UPDATE = "update";

    private String errorId = "";
    private String requestType = "";
    private JsonObject requestJson = new JsonObject();


    public LLRequest(String request) {
        parseJson(request);
    }

    private void parseJson(String request) {
        try {
            requestJson = new JsonParser().parse(request).getAsJsonObject();
            requestType = requestJson.get("requestType").getAsString();
            System.out.println("parsed Json " + requestType);

        } catch (ClassCastException e) {
            System.out.println("parse Json not successful");
            System.out.println(e);
        }


        if (requestType.equals(REQUEST_ERROR_INFO)) {
            errorId = requestJson.get("errorId").getAsString();

        } else if (requestType.equals(REQUEST_SET_CURSOR)) {
            errorId = requestJson.get("errorId").getAsString();


        }



    }

    public String getErrorId() {
        return errorId;
    }

    public String getRequestType() {
        return requestType;
    }

    public JsonObject getRequestJson() {
        return requestJson;
    }

}
