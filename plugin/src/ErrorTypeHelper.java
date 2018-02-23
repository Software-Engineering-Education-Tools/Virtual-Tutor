package de.ur.mi.roberts;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by Jonas Roberts on 10.01.2018.
 */
public class ErrorTypeHelper {
    public static String getErrorType(String errorMessage, Set<String> includedErrorTypes) {
        if (errorMessage != null) {
            errorMessage = errorMessage.replaceAll("[.,!'\"]", "").toLowerCase();
            List<String> errorMessageWordList = Arrays.asList(errorMessage.split(" "));
            for (String errorType : includedErrorTypes) {
                String errorTypeLower = errorType.toLowerCase();
                List<String> errorTypeWordList = Arrays.asList(errorTypeLower.split(" "));
                if (errorMessageWordList.containsAll(errorTypeWordList)) {
                    return errorTypeLower;

                }
            }
        }
        return "";
    }
}
