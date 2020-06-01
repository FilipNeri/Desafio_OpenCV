package application;
	
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("Tela.fxml"));
			Scene scene = new Scene(root,800,600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		//teste
		      System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		      Mat mat = Mat.eye( 3, 3, CvType.CV_8UC1 );
		      System.out.println( "mat = " + mat.dump() );
		   
		launch(args);
	}
}
