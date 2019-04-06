package killclipper;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage mainStage;
    public static String workingDirectory = Paths.get("").toAbsolutePath().toString();

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("Working directory: " + workingDirectory);
        Clipper.initialize();
        Main.mainStage = stage;
        // TODO: Automate versioning
        stage.setTitle("Kill Clipper Tool v0.7.01");
        changeView("MainView");
        stage.show();
    }

    public static void popupView(String view) throws IOException {
        System.out.println("Spawning popup: " + view);
        Stage stage = new Stage();
        stage.setResizable(false);
        URL resource = Main.class.getResource("view/" + view + ".fxml");
        Parent root = FXMLLoader.load(resource);
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void changeView(String view) throws IOException {
        System.out.println("Switching to view: " + view);
        URL resource = Main.class.getResource("view/" + view + ".fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        mainStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
