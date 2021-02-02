package views;
import controllers.MenuController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.util.Objects;

public class Menu {
    Parent root;
    MenuController controller;

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

    public Parent getRoot() {
        return root;
    }

    public MenuController getController() {
        return controller;
    }

}
