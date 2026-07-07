package org.IsmaelSS.controller;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class ScreenController {
    private final Stage stage;
    private final Map<String, Scene> screens = new HashMap<>();

    public ScreenController(Stage stage) {
        this.stage = stage;
    }

    public void registerScreen(String name, Scene scene) {
        screens.put(name, scene);
    }

    public void switchTo(String name) {
        Scene scene = screens.get(name);
        if (scene != null) {
            stage.setScene(scene);
            stage.show();
        }
    }
}
