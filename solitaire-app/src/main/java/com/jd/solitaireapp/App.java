package com.jd.solitaireapp;

import com.jd.solitaireapp.ui.AppNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

/** Desktop entry point, independent of the console demonstration. */
public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        AppNavigator navigator = new AppNavigator();
        primaryStage.setTitle("Solitaire Collection");
        primaryStage.setScene(navigator.getScene());
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(740);
        primaryStage.show();
    }

    /** Launches the JavaFX application. @param args JavaFX launch arguments */
    public static void main(String[] args) {
        launch(args);
    }
}
