package org.IsmaelSS.controller;

import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ScreenController {
    private final Stage stage;
    private final Map<String, Scene> screens = new HashMap<>();
    private int baseFontSize = 14;
    private static final int MIN_FONT_SIZE = 10;
    private static final int MAX_FONT_SIZE = 30;

    public ScreenController(Stage stage) {
        this.stage = stage;
    }

    public void registerScreen(String name, Scene scene) {
        screens.put(name, scene);
        loadTheme(scene);
        installZoomFilter(scene);
        applyCurrentFontSize(scene);
    }

    public void switchTo(String name) {
        Scene scene = screens.get(name);
        if (scene != null) {
            applyCurrentFontSize(scene);
            stage.setScene(scene);
            stage.show();
        }
    }

    private void loadTheme(Scene scene) {
        URL cssUrl = getClass().getResource("/styles/theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Warning: theme.css not found on classpath");
        }
    }

    public int getBaseFontSize() {
        return baseFontSize;
    }

    private void installZoomFilter(Scene scene) {
        scene.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (!event.isControlDown()) return;
            if (event.getDeltaY() > 0) {
                baseFontSize = Math.min(baseFontSize + 1, MAX_FONT_SIZE);
            } else if (event.getDeltaY() < 0) {
                baseFontSize = Math.max(baseFontSize - 1, MIN_FONT_SIZE);
            }
            applyCurrentFontSize(scene);
            event.consume();
        });
    }

    private void applyCurrentFontSize(Scene scene) {
        if (scene != null && scene.getRoot() != null) {
            scene.getRoot().setStyle("-fx-font-size: " + baseFontSize + "px");
        }
    }
}
