package controllers;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.*;
import javafx.stage.Stage;
import views.Viewer;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static javafx.collections.FXCollections.observableArrayList;

public class MenuController {
    @FXML
    public Button submitButton;
    public TextField filenameText;
    public TextField xText;
    public TextField yText;
    public TextField zText;
    public CheckBox correctEndianBox;

    @FXML
    public ChoiceBox<String> filenameChoiceBox;

    private String filename;
    private int xAxis = 0;
    private int yAxis = 0;
    private int zAxis = 0;
    private boolean isCorrectEndian = false;
    private Stage stage;

    @FXML
    public void initialize(){
        xText.textProperty().addListener((observable, oldValue, newValue) -> {
            xAxis = toInt(newValue);
            isFilledIn();
        });

        yText.textProperty().addListener((observable, oldValue, newValue) -> {
            yAxis = toInt(newValue);
            isFilledIn();
        });

        zText.textProperty().addListener((observable, oldValue, newValue) -> {
            zAxis = toInt(newValue);
            isFilledIn();
        });

        correctEndianBox.selectedProperty().addListener((observable, oldValue, newValue) ->
                isCorrectEndian = newValue);

        filenameChoiceBox.setItems(getScans());

        filenameChoiceBox.valueProperty().addListener((observable, oldValue, newValue) ->
                filename = "src/data/" + newValue);
    }

    public void handleSubmitClick() {
        Volume v = new Volume(xAxis, yAxis, zAxis);
        try {
            v.ReadData(filename, isCorrectEndian);
            new Viewer(stage, new CTHeadViewer(v));
        } catch (Exception e){
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "An error was encountered.",
                    ButtonType.OK);
            error.showAndWait();
            e.printStackTrace();
        }
    }

    public void handleDefaultClick() throws IOException {
        Volume v = new Volume(256,256, 113);
        try {
        v.ReadData("src/data/CThead", false);
        new Viewer(stage, new CTHeadViewer(v));
        } catch (Exception e){
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "An error was encountered.",
                    ButtonType.OK);
            error.showAndWait();
        }
    }

    public void setStage (Stage stage) {
        this.stage = stage;
    }

    private int toInt(String value){
        try {
            Integer.parseInt(value);
        } catch (Exception e){
            return 0;
        }
        return Integer.parseInt(value);
    }

    private ObservableList<String> getScans() {
        File folder = new File("src/data");
        ObservableList<String> scans = observableArrayList();
        for (File scan : Objects.requireNonNull(folder.listFiles())) {
            scans.add(scan.getName().substring(0, scan
                    .getName().length()));
        }
        return scans;
    }

    private void isFilledIn(){
        submitButton.setDisable(xAxis == 0 || yAxis == 0 || zAxis == 0);
    }


}
