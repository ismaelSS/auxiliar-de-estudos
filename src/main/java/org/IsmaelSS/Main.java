package org.IsmaelSS;

import javafx.application.Application;
import javafx.stage.Stage;
import org.IsmaelSS.controller.ScreenController;
import org.IsmaelSS.controller.ThemeSelectionController;
import org.IsmaelSS.service.StatsService;
import org.IsmaelSS.service.ThemeLoader;
import org.IsmaelSS.view.ThemeSelectionView;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("FlashCard Java");
        stage.setMinWidth(600);
        stage.setMinHeight(500);

        ThemeLoader themeLoader = new ThemeLoader();
        StatsService statsService = new StatsService();
        ScreenController screenController = new ScreenController(stage);

        ThemeSelectionView themeSelectionView = new ThemeSelectionView();
        ThemeSelectionController themeSelectionController = new ThemeSelectionController(
                themeLoader, themeSelectionView, screenController, statsService);

        themeSelectionController.initialize();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
