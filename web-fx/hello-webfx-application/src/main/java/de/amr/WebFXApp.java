package de.amr;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class WebFXApp extends Application {

	@Override
	public void init() throws Exception {
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setScene(new Scene(new StackPane(new Text("Hello world!")), 800, 600));
		primaryStage.show();
	}

}