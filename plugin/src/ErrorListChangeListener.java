package de.ur.mi.roberts;

import java.util.HashSet;

/**
 * Created by Jonas Roberts on 09.10.2017.
 */
public interface ErrorListChangeListener {
    void errorSetChanged(String currentFileName, boolean activeFileChanged, HashSet<CodeError> completeSet, HashSet<CodeError> oldErrors, HashSet<CodeError> newErrors, HashSet<CodeError> updatedErrors);

    void fileNameChanged(String fileName);
}
