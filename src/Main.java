import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import models.CTHeadViewer;
import models.Volume;
import views.Menu;
import views.Viewer;

public class Main extends Application{

    @Override
    public void start(Stage stage) {

        Menu menu = new Menu(stage);
        Volume v = new Volume(256,256, 113);

        new Viewer(stage, menu.getRoot());

    }

    public static void main(String[] args) {
        launch(args);
    }
}
