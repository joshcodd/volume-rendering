package views;
import controllers.ViewerController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import models.CTHeadViewer;
import models.Volume;

import java.util.Objects;

public class Viewer {
    private String filename;

    public Viewer(Stage stage, CTHeadViewer ctHead){
        try {
            FXMLLoader loader = new FXMLLoader();
            Parent root = loader.load(Objects.requireNonNull(getClass().getClassLoader()
                    .getResource("views/viewer.fxml"))
                    .openStream());

            Scene scene = new Scene(root, ctHead.getTop_width() + ctHead.getSide_width(), 680);
            ViewerController controller = loader.getController();
            controller.setCTHeadViewer(ctHead);
            controller.draw();
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
}
