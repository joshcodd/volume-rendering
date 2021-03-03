package controllers;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import models.*;
import javafx.stage.Stage;
import views.Viewer;
import java.io.File;
import java.util.Objects;
import static javafx.collections.FXCollections.observableArrayList;

/**
 * Controller class for the menu interface.
 * Handles all user input.
 * @author Josh Codd.
 */
public class MenuController {
    private static final String PATH_TO_SCANS = "src/data";
    private String filename;
    private int xAxis = 0;
    private int yAxis = 0;
    private int zAxis = 0;
    private boolean isCorrectEndian = false;
    private boolean isVH = false;
    private Stage stage;

    @FXML
    public Button submitButton;
    public TextField xText;
    public TextField yText;
    public TextField zText;
    public CheckBox correctEndianBox;
    public ChoiceBox<String> filenameChoiceBox;
    public Button defaultButton;
    public VBox menuPane;
    public CheckBox vhResampleBox;

    /**
     * Initializes the user interface elements.
     */
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

        vhResampleBox.selectedProperty().addListener((observable, oldValue, newValue) ->
                isVH = newValue);

        filenameChoiceBox.setItems(getScans());

        filenameChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filename = "src/data/" + newValue;
            isFilledIn();
        });

        submitButton.setDisable(true);
    }


    /**
     * Opens a new viewer using data entered into page.
     */
    public void handleSubmitClick() {
        Volume v = new Volume(xAxis, yAxis, zAxis);
        try {
            v.ReadData(filename, isCorrectEndian, isVH);
            new Viewer(stage, new CTHeadViewer(v));
        } catch (Exception e){
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "An error was encountered.",
                    ButtonType.OK);
            error.showAndWait();
            e.printStackTrace();
        }
    }

    /**
     * Opens a new viewer with the standard CAT scan being displayed.
     */
    public void handleDefaultClick() {
        Volume v = new Volume(256,256, 113);
        try {
            v.ReadData("src/data/CThead", false, false);

            new Viewer(stage, new CTHeadViewer(v));
        } catch (Exception e){
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "An error was encountered.",
                    ButtonType.OK);
            error.showAndWait();
        }
    }

    /**
     * Sets the stage of the application.
     * @param stage The stage to set.
     */
    public void setStage (Stage stage) {
        this.stage = stage;
    }


    /**
     * Checks if a string is possible to convert to a integer.
     * @param value The value to convert.
     * @return The converted value.
     */
    private int toInt(String value){
        try {
            Integer.parseInt(value);
        } catch (Exception e){
            return 0;
        }
        return Integer.parseInt(value);
    }

    /**
     * Gets the names of all files (scans) available.
     * @return The list of scans available to view.
     */
    private ObservableList<String> getScans() {
        File folder = new File(PATH_TO_SCANS);
        ObservableList<String> scans = observableArrayList();
        for (File scan : Objects.requireNonNull(folder.listFiles())) {
            scans.add(scan.getName());
        }
        return scans;
    }

    /**
     * Handles submit button validation. Can only be pressed if all sensible values have been
     * entered.
     */
    private void isFilledIn(){
        submitButton.setDisable(filenameChoiceBox.getValue() == null ||
                xAxis == 0 || yAxis == 0 || zAxis == 0);
    }
}
