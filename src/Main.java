import javafx.application.Application;
import javafx.stage.Stage;
import views.Menu;
import views.Viewer;

/**
 * Main class of the CT viewer. Responsible for starting the application.
 */
public class Main extends Application{

    /**
     * JavaFX start method.
     * @param stage The stage to display on.
     */
    @Override
    public void start(Stage stage) {
        Menu menu = new Menu(stage);
        new Viewer(stage, menu.getRoot());
    }

    /**
     * Runs the application.
     * @param args Command line arguments Not used in this application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
