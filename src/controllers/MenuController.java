package controllers;
import models.*;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import views.Viewer;
import java.io.IOException;

public class MenuController {
    private String filename;
    private int xAxis;
    private int yAxis;
    private int zAxis;
    private Stage stage;

    public void hello() throws IOException {
        Volume v = new Volume(256,256, 113);
        v.ReadData("CThead", false);
        new Viewer(stage, new CTHeadViewer(v));
    }

    public void handleFileChange(KeyEvent actionEvent) {
        TextField text = (TextField) actionEvent.getSource();
        filename = text.getText();
    }

    public void setStage (Stage stage) {
        this.stage = stage;
    }
}
