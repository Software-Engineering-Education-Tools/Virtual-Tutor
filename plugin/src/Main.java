package de.ur.mi.roberts;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.openapi.wm.impl.WindowManagerImpl;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;


/**
 * Created by Jonas Roberts on 22.08.2017.
 */
public class Main implements ProjectComponent, AssistantInteractionListener, ErrorListChangeListener {

    private LLServer server;
    private Project project;
    private ProblemFetcher fetcher;
    private LLFileWriter llFileWriter;
    private HashMap<Long, CodeError> allErrorsHashMap;
    private HashMap<Long, CodeError> addedErrorsHashMap;
    private HashMap<Long, CodeError> removedErrorsHashMap;
    private HashMap<Long, CodeError> updatedErrorsHashMap;
    private HashMap<String, Integer> configuration = new HashMap<>();
    private Process assistantProcess;
    private String userName;



    public Main(Project project) {
        this.project = project;
    }

    @Override
    public void initComponent() {
        System.out.println("Main.initComponent");
    }

    private void getConfigurationData() {
        VirtualFile configFile = getConfigFile();
        if (configFile != null) {
            JsonObject configJson = getJsonFromConfigFile(configFile);
            this.configuration = fillConfigHashMap(configJson);
            System.out.println(configuration);

        } else {
            System.out.println("no config file found");
        }


    }

    private HashMap<String, Integer> fillConfigHashMap(JsonObject configJson) {
        HashMap<String, Integer> config = new HashMap<>();
        Set<Map.Entry<String, JsonElement>> entries = configJson.entrySet();

        try {
            for (Map.Entry<String, JsonElement> entry : entries) {
                if (entry.getValue().getAsJsonArray().get(0).getAsInt() != 0) {
                    config.put(entry.getKey().replaceAll("[.,!'\"]", "").toLowerCase(), entry.getValue().getAsJsonArray().get(1).getAsInt());
                }
            }
        } catch (IllegalStateException e) {
            System.out.println("Error in llconfig.json file!");
        }
        return config;


    }

    private JsonObject getJsonFromConfigFile(VirtualFile configFile) {

        String fileContent = LoadTextUtil.loadText(configFile).toString();
        return (JsonObject) new JsonParser().parse(fileContent);
    }

    private VirtualFile getConfigFile() {
        VirtualFile[] vFiles = ProjectRootManager.getInstance(this.project).getContentRoots();

        return vFiles[0].findChild("llconfig.json");
    }

    @Override
    public void disposeComponent() {
        server.shutdown();
        llFileWriter.close();
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "InputTracker";
    }

    @Override
    public void projectOpened() {
        System.out.println("Main.projectOpened");
        getConfigurationData();

        userName = getMacAddress();
        this.fetcher = new ProblemFetcher(this, project, userName, this.configuration);
        this.llFileWriter = new LLFileWriter(userName);
        this.allErrorsHashMap = new HashMap<>();
        this.addedErrorsHashMap = new HashMap<>();
        this.removedErrorsHashMap = new HashMap<>();
        this.updatedErrorsHashMap = new HashMap<>();
        startServer();
        initProblemListener();
        startAssistant();
    }

    private void startAssistant() {
        try {

            File path = PluginManager.getPlugin(PluginId.getId("de.ur.mi.")).getPath();


            ProcessBuilder processBuilder = new ProcessBuilder(path.getAbsolutePath() + "\\electron-quick-start-win32-x64\\electron-quick-start.exe");
            processBuilder.directory(new File(path.getAbsolutePath() + "\\electron-quick-start-win32-x64"));
            assistantProcess = processBuilder.start();
            if(assistantProcess.isAlive()){
                System.out.println("assistant assistantProcess running");
           }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initProblemListener() {
        MessageBus messageBus = project.getMessageBus();
        messageBus.connect().subscribe(DaemonCodeAnalyzer.DAEMON_EVENT_TOPIC, new DaemonCodeAnalyzer.DaemonListenerAdapter() {
            @Override
            public void daemonFinished() {
                super.daemonFinished();
                fetcher.checkForNewErrors(false);
            }

            @Override
            public void daemonCancelEventOccurred(@NotNull String reason) {
                super.daemonCancelEventOccurred(reason);
            }
        });
    }


    @Override
    public void projectClosed() {
        assistantProcess.destroy();
        server.shutdown();
        llFileWriter.close();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    private void startServer() {
        server = new LLServer(Constants.WEBSOCKET_PORT);
        server.addInteractionListener(this);
        server.addInteractionListener(llFileWriter);
        server.start();
    }


    public void onAllErrorsRequested() {
        server.sendMessage(JsonCreator.getSimpleJson(allErrorsHashMap).toString());
    }

    @Override
    public void onMessageReceived(String message) {
        System.out.println("about to create request object");
        LLRequest request = new LLRequest(message);

        if (request.getRequestType().equals(LLRequest.REQUEST_ERROR_INFO)) {
            System.out.println("errorRequest");
            server.sendMessage(JsonCreator.getJsonForError(request.getErrorId(), allErrorsHashMap).toString());
        } else if (request.getRequestType().equals(LLRequest.REQUEST_SET_CURSOR)) {
            setCursorToError(request.getErrorId());

        } else if (request.getRequestType().equals(LLRequest.REQUEST_UPDATE)) {
            System.out.println("updateRequest");
            new WriteCommandAction(project) {

                @Override
                protected void run(@NotNull Result result) throws Throwable {
                    fetcher.checkForNewErrors(true);
                }
            }.execute();
        }

    }

    @Override
    public void onMessageSent(String message) {

    }

    private void setCursorToError(String errorId) {
        int[] cursorPos = allErrorsHashMap.get(Long.parseLong(errorId)).getCursorPos();

        System.out.println("cursorPos = " + cursorPos[0] + " " + cursorPos[1]);

        new WriteCommandAction(project) {
            @Override
            protected void run(@NotNull Result result) throws Throwable {
                IdeFrameImpl ideFrame = (IdeFrameImpl) WindowManagerImpl.getInstance().getIdeFrame(project);
                ideFrame.setVisible(true);
                ideFrame.requestFocus();
                ideFrame.toFront();



                Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                LogicalPosition logicalPosition = new LogicalPosition(cursorPos[0] - 1, cursorPos[1] - 1);
                System.out.println("logicalPosition.line = " + logicalPosition.line);
                editor.getCaretModel().moveToLogicalPosition(logicalPosition);
                editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);

                editor.getContentComponent().grabFocus();

            }
        }.execute();
    }


    @Override
    public void errorSetChanged(String currentFileName, boolean activeFileChanged, HashSet<CodeError> completeSet, HashSet<CodeError> removedErrors, HashSet<CodeError> newErrors, HashSet<CodeError> updatedErrors) {
        updateErrorHashMaps(completeSet, removedErrors, newErrors, updatedErrors);


        if (Constants.LOGGING_ACTIVE) {
            llFileWriter.appendToFile(completeSet);
        }
        for(Long error : allErrorsHashMap.keySet()){
            CodeError codeError = allErrorsHashMap.get(error);
        }
        server.sendMessage(JsonCreator.getJsonForUpdate(userName, currentFileName, activeFileChanged, addedErrorsHashMap, removedErrorsHashMap, updatedErrorsHashMap).toString());
        //onAllErrorsRequested();
    }

    @Override
    public void fileNameChanged(String fileName) {
        System.out.println("fileName = " + fileName);
    }

    private void updateErrorHashMaps(HashSet<CodeError> completeSet, HashSet<CodeError> removedErrors, HashSet<CodeError> newErrors, HashSet<CodeError> updatedErrors) {
        allErrorsHashMap.clear();
        for (CodeError error : completeSet) {
            allErrorsHashMap.put(error.getId(), error);
        }

        removedErrorsHashMap.clear();
        for (CodeError error : removedErrors) {
            removedErrorsHashMap.put(error.getId(), error);
        }

        addedErrorsHashMap.clear();
        for (CodeError error : newErrors) {
            addedErrorsHashMap.put(error.getId(), error);
        }

        updatedErrorsHashMap.clear();
        for (CodeError error : updatedErrors) {
            updatedErrorsHashMap.put(error.getId(), error);
            //updat
        }


    }


    private String getMacAddress() {
        byte[] mac = new byte[0];
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            mac = network.getHardwareAddress();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        String macString = Arrays.toString(mac);
        macString = macString.replaceAll("[^0-9 ]", "");
        macString = macString.replaceAll(" ", "_");
        System.out.println("macString = " + macString);
        return macString;
    }

}
