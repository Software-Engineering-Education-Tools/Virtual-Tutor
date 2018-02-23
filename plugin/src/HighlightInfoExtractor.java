package de.ur.mi.roberts;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.HashMap;

/**
 * Created by Jonas Roberts on 04.10.2017.
 */
public class HighlightInfoExtractor {

    private final PsiFile psiFile;
    private Project project;
    private Document document;
    private Editor editor;
    private HighlightInfo info;


    HighlightInfoExtractor(Project project, Document document, HashMap<String, Integer> configuration) {
        this.project = project;
        this.document = document;
        this.psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);

    }

    int getRow() {
        return document.getLineNumber(info.getStartOffset()) + 1;
    }

    String getErrorMessage() {
        return info.getDescription();
    }

    public String getProblemSeverity() {
        return info.getSeverity().getName();
    }

    int getCursorPosInLine() {
        int startOffset = info.getStartOffset();
        editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        return editor.offsetToLogicalPosition(startOffset).column + 1;
    }

    int getLineCount() {
        return document.getLineCount();
    }

    String getLineContent() {
        int lineStart = document.getLineStartOffset(getRow() - 1);
        int lineEnd = document.getLineEndOffset(getRow() - 1);
        String line = document.getText(new TextRange(lineStart, lineEnd));
        line = line.replaceAll("[\n\t]", " ");
        return line.trim();
    }

    String getHighlightedText() {
        return info.getText().replaceAll("[\n\t]", " ");
    }

    long getId() {
        return (getErrorMessage() + getHighlightedText() + getFilename()).hashCode();
    }

    void setInfo(HighlightInfo info) {
        this.info = info;
    }

    String getParentElement() {
        int lineStartOffset = document.getLineStartOffset(getRow() - 1);
        System.out.println("lineStartOffset = " + lineStartOffset);
        PsiElement psiElement = psiFile.findElementAt(lineStartOffset);
        String parent = PsiTreeUtil.getStubOrPsiParent(psiElement).getParent().toString();
        String[] parentSplit = parent.split(":");
        if (parentSplit.length > 1) {
            if(parentSplit[0].contains("Java")) {
                int lineEndOffset = document.getLineEndOffset(getRow() - 1);
                PsiElement otherPsiElement = psiFile.findElementAt(lineEndOffset);
                String[] otherParents = PsiTreeUtil.getStubOrPsiParent(otherPsiElement).getParent().toString().split(":");
                if (otherParents.length > 1) {
                    parent = otherParents[1];
                }
            }else{
                parent = parentSplit[1];
            }
        }
        return parent;
    }


    String getFilename() {
        return psiFile.getName();
    }

    int getFileLength() {
        return psiFile.getTextLength();
    }
}
