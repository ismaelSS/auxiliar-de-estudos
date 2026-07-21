package org.IsmaelSS.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import org.IsmaelSS.model.RoundState;
import org.IsmaelSS.model.Theme;
import org.IsmaelSS.service.StatsService;
import org.IsmaelSS.service.ThemeLoader;
import org.IsmaelSS.view.QuestionFileManagerView;
import org.IsmaelSS.view.ReportsView;
import org.IsmaelSS.view.ReviewDashboardView;
import org.IsmaelSS.view.StudyRoundView;
import org.IsmaelSS.view.ThemeSelectionView;

import java.util.List;

public class ThemeSelectionController {
    private final ThemeLoader themeLoader;
    private final ThemeSelectionView view;
    private final ScreenController screenController;
    private final StatsService statsService;
    private List<Theme> themes;
    private ReportsController reportsController;
    private QuestionFileManagerController questionFileManagerController;

    public ThemeSelectionController(ThemeLoader themeLoader, ThemeSelectionView view,
                                    ScreenController screenController, StatsService statsService) {
        this.themeLoader = themeLoader;
        this.view = view;
        this.screenController = screenController;
        this.statsService = statsService;
    }

    public void initialize() {
        refreshDashboard();

        screenController.registerScreen("themeSelection", view.getScene());

        view.getTabPane().getSelectionModel().selectedItemProperty().addListener(
                (obs, oldTab, newTab) -> handleTabSelection(newTab));

        screenController.switchTo("themeSelection");
    }

    public void refreshDashboard() {
        themes = themeLoader.loadAllThemes();
        ReviewDashboardView dashboard = new ReviewDashboardView(
                statsService, themes,
                theme -> () -> handleReviewTheme(theme),
                this::handleMarkAsDone
        );
        view.setDashboard(dashboard);
    }

    private void handleReviewTheme(Theme theme) {
        RoundState roundState = RoundState.createDueReviewRound(theme, statsService);
        if (roundState.getSelectedQuestionsCount() == 0) {
            return;
        }
        StudyRoundView studyRoundView = new StudyRoundView();
        StudyRoundController studyRoundController = new StudyRoundController(
                roundState, studyRoundView, screenController, statsService);
        studyRoundController.setOnRoundEndCallback(() -> {
            refreshDashboard();
            screenController.switchTo("themeSelection");
        });
        studyRoundController.initialize();
    }

    private void handleMarkAsDone(String themeName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Marcar como feita");
        alert.setHeaderText(null);
        alert.setContentText("Marcar todas as questões atrasadas como dominadas para '" + themeName + "'?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                statsService.markThemeAsDone(themeName);
                refreshDashboard();
            }
        });
    }

    private void handleTabSelection(Tab tab) {
        if (tab == view.getRelatoriosTab()) {
            if (reportsController == null) {
                ReportsView reportsView = new ReportsView();
                reportsController = new ReportsController(statsService, reportsView, screenController, themeLoader);
            }
            reportsController.refreshAndShow(tab);
        } else if (tab == view.getGerenciarTab()) {
            if (tab.getContent() == null) {
                QuestionFileManagerView qfmView = new QuestionFileManagerView();
                questionFileManagerController = new QuestionFileManagerController(themeLoader, this, qfmView);
                questionFileManagerController.initialize();
                tab.setContent(qfmView.getRoot());
            }
        }
    }
}
