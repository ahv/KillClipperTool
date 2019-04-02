package killclipper;

import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage mainStage;

    @Override
    public void start(Stage stage) throws Exception {
        Main.mainStage = stage;
        stage.setTitle("Kill Clipper Tool v0.4");
        changeView("MainView");
        stage.show();
    }

    public static void popupView(String view) throws IOException {
        System.out.println("Spawning popup: " + view);
        Stage stage = new Stage();
        stage.setResizable(false);
        URL resource = Main.class.getResource("view/" + view + ".fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    //TODO: could perhaps do some .class shenanigans here: MainView.class, etc.
    public static void changeView(String view) throws IOException {
        System.out.println("Switching to view: " + view);
        URL resource = Main.class.getResource("view/" + view + ".fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        mainStage.setScene(scene);
    }

    public static void main(String[] args) {
        ApiCaller.getCharacterIdForName("FriendlyHenry");
        launch(args);
    }

}