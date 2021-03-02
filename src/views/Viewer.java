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

/**
 * Class to display and load a viewer to screen.
 * @author Josh Codd.
 */
public class Viewer {

    /**
     * Creates and displays a viewer using the specified ct viewer.
     * @param stage The stage to display this scene on.
     * @param ctHead The viewer to display.
     */
    public Viewer(Stage stage, CTHeadViewer ctHead){
        ViewerController controller = new ViewerController();
        try {
            FXMLLoader loader = new FXMLLoader();
            Parent root = loader.load(Objects.requireNonNull(getClass().getClassLoader()
                    .getResource("views/viewer.fxml"))
                    .openStream());

            Scene scene = new Scene(root, ctHead.getTop_width() + ctHead.getSide_width() + 350,
                    Math.max(620, ctHead.getSide_height() + ctHead.getFront_height() + 50));
            controller = loader.getController();
            controller.setCTHeadViewer(ctHead);
            scene.getStylesheets().add("styles.css");
            controller.setStage(stage);
            controller.init();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "An error was encountered.",
                    ButtonType.OK);
            error.showAndWait();
            stage.close();
        }
        controller.sliderValueStyle(controller.getOpacitySlider());
        controller.getMidSlideButton().fire();
    }

    /**
     * Creates and displays a viewer showing the menu specified.
     * @param stage The stage to display this scene on.
     * @param menu The menu to display.
     */
    public Viewer(Stage stage, Parent menu){
        ViewerController controller = new ViewerController();
        try {
            FXMLLoader loader = new FXMLLoader();
            Parent root = loader.load(Objects.requireNonNull(getClass().getClassLoader()
                    .getResource("views/viewer.fxml"))
                    .openStream());
            Scene scene = new Scene(root, 890, 550);
            controller = loader.getController();
            controller.getVolRendMenu().setManaged(false);
            scene.getStylesheets().add("styles.css");
            controller.getMenuPane().getChildren().add(menu);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "An error was encountered.",
                    ButtonType.OK);
            error.showAndWait();
            stage.close();
        }

        controller.sliderValueStyle(controller.getOpacitySlider());
        controller.getMidSlideButton().fire();
    }
}
