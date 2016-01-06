package uk.co.jlensmeister;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class Client extends Application {
	@Override
	public void start(Stage stage) {
		try {
			FXMLLoader rootLoader = new FXMLLoader(getClass().getResource("resources/Frame.fxml"));
			FrameController fc = (FrameController)rootLoader.getController();
			Parent root = (Parent) rootLoader.load();
			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.setMinWidth(600);
			stage.setMinHeight(550);
			stage.setHeight(550);
			stage.setWidth(600);
			stage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
