package com.gb.cloud.client;

import com.gb.cloud.common.MyMsg;
import com.gb.cloud.common.OneFile;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.ResourceBundle;

import static com.gb.cloud.client.Network.oeos;
import static java.nio.file.Files.*;

public class Controller implements Initializable {

    final String SERVER_IP = "localhost";
    final int SERVER_PORT = 8189;
    final int PART_SIZE = 8192;
    private byte [] file_array = new byte[]{};
    //final int MAX_PART_SIZE = 1024*512;
    private static String currentFolder;



    @FXML
    TextField userNameField;
    @FXML
    TextField passwordField;
    @FXML
    TextField folderField;
    @FXML
    TextField txtField;
    @FXML
    ListView<String> clientsFilesList;
    @FXML
    ListView<String> serversFilesList;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start(SERVER_IP, SERVER_PORT);
        currentFolder = "C:\\Cloud\\Client\\";
        Thread t1 = new Thread(() -> {
            try {
                OneFile oneFile;
                MyMsg myMsg;
                byte data [];
                while (true) {

                    Object object = Network.odis.readObject();
                    if (object.getClass() == OneFile.class){
                        oneFile = (OneFile) (object);
                        System.out.println("Incoming file: " + oneFile.name);
                        data = oneFile.data;
                        write(Paths.get(currentFolder + "\\" + oneFile.name), data, StandardOpenOption.CREATE);
                        System.out.println("Файл " + oneFile.name + " записан");
                        refreshContent();
                    } else if (object.getClass() == MyMsg.class){
                        myMsg = (MyMsg)(object);
                        executeIncomingMsg(myMsg);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t1.setDaemon(true);
        t1.start();
       clientsFilesList.setItems(FXCollections.observableArrayList());
       refreshLocalFilesList();
    }

    public void authorize(){
        MyMsg myMsg = new MyMsg(MyMsg.Type.AUTH_REQUEST);
        myMsg.login = userNameField.getText();
        myMsg.password = passwordField.getText();
        sendCommand(myMsg);
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent){
        sendFile();
    }

    private void executeIncomingMsg(MyMsg myMsg){
        if (myMsg.type == MyMsg.Type.REFRESH_REQUEST){
            System.out.println("Получено обновление списка файлов");

            if (Platform.isFxApplicationThread()) {
                serversFilesList.getItems().clear();
                myMsg.stringListView.stream().forEach(o -> serversFilesList.getItems().add(o));


            } else {
                Platform.runLater(() -> {
                    serversFilesList.getItems().clear();
                    myMsg.stringListView.stream().forEach(o -> serversFilesList.getItems().add(o));
                });
            }

        } else {
            System.out.println("Unknown MyMsg type");
        }
    }

    private void sendCommand(MyMsg myMsg){
        try {
            oeos.writeObject(myMsg);
            oeos.flush();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendFile(){
        try {
            OneFile oneFile = new OneFile();
            String selectedFile = new String();
            selectedFile = clientsFilesList.getSelectionModel().getSelectedItem();
                        oneFile.name = selectedFile;
            //oneFile.data = readAllBytes(Paths.get(currentFolder + "\\" + txtField.getText()));
            //file_array = readAllBytes(Paths.get(currentFolder + "\\" + txtField.getText()));
            System.out.println("Selected File:" + selectedFile);
            file_array = readAllBytes(Paths.get(currentFolder + "\\" + selectedFile));
            int partsCount = file_array.length / PART_SIZE;
            if (file_array.length % PART_SIZE != 0) partsCount++;
            for (int i = 0; i < partsCount; i++) {
                int startPosition = i * PART_SIZE;
                int endPosition = (i + 1) * PART_SIZE;
                if (endPosition > file_array.length) {
                    endPosition = file_array.length;
                }
                oneFile.data = Arrays.copyOfRange(file_array, startPosition, endPosition);
                oneFile.numberOfParts = partsCount;
                oneFile.partNumber = i;
                oneFile.printOneFile();
                oeos.writeObject(oneFile);
                oeos.flush();
            }
            refreshContent();
        } catch (Exception e){
            System.out.println("Поймано исключение при отправке файла: " + e.toString());
            e.printStackTrace();
        }
    }

    public void getFile(){
        MyMsg myMsg = new MyMsg(MyMsg.Type.FILE_REQUEST);
        myMsg.fileName = serversFilesList.getSelectionModel().getSelectedItem();
        sendCommand(myMsg);
        refreshContent();
    }

    public void refreshContent(){
        if (!folderField.getText().equals("")) currentFolder = folderField.getText();
        refreshCloud();
        refreshLocalFilesList();
    }

    public void refreshCloud(){
        sendCommand(new MyMsg(MyMsg.Type.REFRESH_REQUEST));
    }

    public void refreshLocalFilesList(){
        if (Platform.isFxApplicationThread()) {
            try {
                clientsFilesList.getItems().clear();
                list(Paths.get(Controller.currentFolder)).map(p -> p.getFileName().toString()).forEach(o -> clientsFilesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    clientsFilesList.getItems().clear();
                    list(Paths.get(currentFolder)).map(p -> p.getFileName().toString()).forEach(o -> clientsFilesList.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}


