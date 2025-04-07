package app.calculadora;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/app/calculadora/interfaz.fxml"));
        try {
	        Image icon = new Image(getClass().getResourceAsStream("/app/calculadora/images/calculadora-icono.png"));
	        primaryStage.getIcons().add(icon);
        }catch (Exception e) {
        	System.err.println("No se ha podido cargar el icono: " + e.getMessage());
        }
        primaryStage.setTitle("Calculadora de Interés Compuesto");
        primaryStage.setScene(new Scene(root, 500, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
