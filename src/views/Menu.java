package views;
import controllers.MenuController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.util.Objects;

/**
 * Displays and loads a menu interface to screen.
 * @author Josh Codd.
 */
public class Menu {
    Parent root;
    MenuController controller;

    /**
     * Creates and displays a menu interface.
     * @param stage The stage to display this scene on.
     */
    public Menu(Stage stage){
        try {
            FXMLLoader loader = new FXMLLoader();
            root = loader.load(Objects.requireNonNull(getClass().getClassLoader()
                    .getResource("views/menu.fxml"))
                    .openStream());
            controller = loader.getController();
            controller.setStage(stage);
        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "An error was encountered.",
                    ButtonType.OK);
            error.showAndWait();
            e.printStackTrace();
        }
    }

    /**
     * Gets the pane that the menu is contained in.
     * @return The parent pane of the menu.
     */
    public Parent getRoot() {
        return root;
    }

    /**
     * Gets the controller for this menu.
     * @return The controller of this menu.
     */
    public MenuController getController() {
        return controller;
    }
}
