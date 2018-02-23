package de.ur.mi.roberts;

import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;

import java.util.*;

/**
 * Created by Jonas Roberts on 20.09.2017.
 */
public class ProblemFetcher {


    private final long timestampStart;
    private final Set<String> includedErrorTypes;
    private Project project;
    private String userName;
    private HashMap<String, Integer> configuration;
    private HashSet<CodeError> oldErrorsSet;
    private HashSet<CodeError> currentErrorsSet;
    private ErrorListChangeListener listChangeListener;

    private String oldFileName = "";


    public ProblemFetcher(ErrorListChangeListener listChangeListener, Project project, String userName, HashMap<String, Integer> configuration) {
        this.listChangeListener = listChangeListener;
        this.project = project;
        this.userName = userName;
        this.configuration = configuration;
        this.includedErrorTypes = configuration.keySet();
        this.timestampStart = System.currentTimeMillis();
        this.oldErrorsSet = new HashSet<CodeError>();
        this.currentErrorsSet = new HashSet<CodeError>();

    }

    private HashSet<CodeError> getRemovedErrors(HashSet<CodeError> oldSet, HashSet<CodeError> currentSet) {
        HashSet<CodeError> temp = new HashSet<>();
        temp.addAll(oldSet);
        temp.removeAll(currentSet);
        return temp;
    }

    private HashSet<CodeError> getAddedErrors(HashSet<CodeError> oldSet, HashSet<CodeError> currentSet) {
        HashSet<CodeError> temp = new HashSet<>();
        temp.addAll(currentSet);
        temp.removeAll(oldSet);
        return temp;
    }


    public void checkForNewErrors(boolean forceResponse) {
        Document document = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();
        List<HighlightInfo> highlightInfoList = DaemonCodeAnalyzerImpl.getHighlights(document, HighlightSeverity.INFORMATION, project);
        HighlightInfoExtractor extractor = new HighlightInfoExtractor(project, document, configuration);
        String currentFileName = extractor.getFilename();
        long timestampError = System.currentTimeMillis();
        boolean setChanged = false;
        boolean setUpdated = false;
        boolean activeFileChanged = false;

        this.oldErrorsSet.clear();

        if (!oldFileName.equals(currentFileName) || forceResponse) {
            activeFileChanged = true;
            listChangeListener.fileNameChanged(currentFileName);
            oldFileName = currentFileName;
        } else {
            this.oldErrorsSet.addAll(this.currentErrorsSet);
        }
        this.currentErrorsSet.clear();


        for (HighlightInfo info : highlightInfoList) {
            extractor.setInfo(info);

            String errorMessage = extractor.getErrorMessage();
            String errorType = ErrorTypeHelper.getErrorType(errorMessage, this.includedErrorTypes);
            if (errorType.equals("")) {
                continue;
            }

            int timeout = configuration.get(errorType);
            int row = extractor.getRow();
            int column = extractor.getCursorPosInLine();
            String parentElement = extractor.getParentElement();
            String lineContent = extractor.getLineContent();
            String highlightedText = extractor.getHighlightedText();
            long errorId = extractor.getId();
            int fileLength = extractor.getFileLength();
            int lineCount = extractor.getLineCount();
            String errorSeverity = extractor.getProblemSeverity();

            CodeError error = new CodeError(errorId, timestampStart, timestampError, timeout, row, column, highlightedText, errorType, errorMessage, errorSeverity, parentElement, lineContent, currentFileName, fileLength, lineCount, userName);
            this.currentErrorsSet.add(error);
        }


        HashSet<CodeError> removedErrors = new HashSet<>();
        HashSet<CodeError> addedErrors = new HashSet<>();
        HashSet<CodeError> updatedErrors = new HashSet<>();


        if (!(this.currentErrorsSet.equals(this.oldErrorsSet))) {
            removedErrors = getRemovedErrors(this.oldErrorsSet, this.currentErrorsSet);
            addedErrors = getAddedErrors(this.oldErrorsSet, this.currentErrorsSet);
            setChanged = true;
        }

        updatedErrors = getUpdatedErrors(this.currentErrorsSet, this.oldErrorsSet);
        if (updatedErrors.size() > 0) {

            setUpdated = true;
        }


        if (setUpdated || setChanged || activeFileChanged) {
            System.out.println("addedErrors = " + addedErrors);
            System.out.println("removedErrors = " + removedErrors);
            System.out.println("updatedErrors = " + updatedErrors);
            listChangeListener.errorSetChanged(currentFileName, activeFileChanged, currentErrorsSet, removedErrors, addedErrors, updatedErrors);
        }

        this.oldErrorsSet.clear();
        this.oldErrorsSet.addAll(this.currentErrorsSet);

    }

    private HashSet<CodeError> getUpdatedErrors(HashSet<CodeError> allErrors, HashSet<CodeError> oldErrors) {
        HashSet<CodeError> updatedErrorsSet = new HashSet<>();

        for (CodeError error : allErrors) {
            for (CodeError oldError : oldErrors) {
                if (error.getId() == oldError.getId() && !Arrays.equals(error.getCursorPos(), oldError.getCursorPos()) ) {
                    updatedErrorsSet.add(error);
                }
            }
        }
        return updatedErrorsSet;
    }
}
