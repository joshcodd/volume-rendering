package views;
import controllers.ViewerController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import models.CTHeadViewer;
import java.util.Objects;

public class Viewer {

    public Viewer(Stage stage, CTHeadViewer ctHead){
        ViewerController controller = new ViewerController();
        try {
            FXMLLoader loader = new FXMLLoader();
            Parent root = loader.load(Objects.requireNonNull(getClass().getClassLoader()
                    .getResource("views/viewer.fxml"))
                    .openStream());

            Scene scene = new Scene(root, ctHead.getTop_width() + ctHead.getSide_width() + 330, 550);
            controller = loader.getController();
            controller.setCTHeadViewer(ctHead);
            scene.getStylesheets().add("styles.css");
            controller.setStage(stage);
            controller.draw();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "An error was encountered.",
                    ButtonType.OK);
            error.showAndWait();
            e.printStackTrace();
            stage.close();
        }

        controller.sliderValueStyle(controller.getOpacitySlider());
        controller.getMidSlideButton().fire();
    }

    public Viewer(Stage stage, Parent menu){
        ViewerController controller = new ViewerController();
        try {
            FXMLLoader loader = new FXMLLoader();
            Parent root = loader.load(Objects.requireNonNull(getClass().getClassLoader()
                    .getResource("views/viewer.fxml"))
                    .openStream());

            Scene scene = new Scene(root, 890, 550);
            controller = loader.getController();
            scene.getStylesheets().add("styles.css");

            controller.getMenuPane().getChildren().add(menu);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "An error was encountered.",
                    ButtonType.OK);
            error.showAndWait();
            e.printStackTrace();
            stage.close();
        }

        controller.sliderValueStyle(controller.getOpacitySlider());
        controller.getMidSlideButton().fire();
    }
}
