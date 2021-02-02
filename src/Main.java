import javafx.application.Application;
import javafx.stage.Stage;
import views.Menu;
import views.Viewer;

public class Main extends Application{

    @Override
    public void start(Stage stage) {
        Menu menu = new Menu(stage);
        new Viewer(stage, menu.getRoot());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
