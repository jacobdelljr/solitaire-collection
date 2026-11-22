package com.jd.solitaireapp;

import com.jd.solitaireapp.ui.AppNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX entry point that installs the menu, window limits, and close confirmation.
 * The implicit public no-argument constructor is used by the JavaFX launcher.
 */
public class App extends Application {
    /**
     * Creates the navigator and shows the main window with guarded game closure.
     * @param primaryStage window supplied by the JavaFX runtime
     */
    @Override
    public void start(Stage primaryStage) {
        AppNavigator navigator = new AppNavigator();
        primaryStage.setTitle("Solitaire Collection");
        primaryStage.setScene(navigator.getScene());
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(740);
        primaryStage.setOnCloseRequest(event -> {
            if (!navigator.requestClose()) { event.consume(); }
        });
        primaryStage.show();
    }

    /**
     * Launches the JavaFX application.
     * @param args JavaFX launch arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
