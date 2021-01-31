import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {
    private String filename;
    private int xAxis;
    private int yAxis;
    private int zAxis;

    @Override
    public void start(Stage stage) {
        stage.setTitle("CThead Viewer");
        try {
            FXMLLoader loader = new FXMLLoader();
            Parent root = loader.load(Objects.requireNonNull(getClass().getClassLoader()
                    .getResource("menu.fxml"))
                    .openStream());

            Scene scene = new Scene(root, 546, 680);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "An error was encountered.",
                    ButtonType.OK);
            error.showAndWait();
            stage.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void hello(ActionEvent actionEvent) {
        ctHeadViewer.main(new String[]{});
    }

    public void handleFileChange(KeyEvent actionEvent) {
        TextField text = (TextField) actionEvent.getSource();
        filename = text.getText();
    }

}
