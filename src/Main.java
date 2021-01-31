import javafx.application.Application;
import javafx.stage.Stage;
import views.Menu;

public class Main extends Application{

    @Override
    public void start(Stage stage) {
        new Menu(stage);

    }

    public static void main(String[] args) {
        launch(args);
    }
}
