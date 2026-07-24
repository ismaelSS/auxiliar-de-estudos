package org.IsmaelSS.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
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

import java.util.ArrayList;
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
        dashboard.setOnCustomStudyStart(() -> handleCustomStudy());
        view.setDashboard(dashboard);
    }

    private void handleReviewTheme(Theme theme) {
        int count = askQuestionCount();
        if (count == 0) return;

        RoundState roundState = RoundState.createDueReviewRound(theme, statsService, count);
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

    private int askQuestionCount() {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Número de questões");
        dialog.setHeaderText("Quantas questões por tema?");

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("5", "10", "15", "20", "Todas");
        comboBox.setValue("Todas");
        comboBox.getStyleClass().add("combo-box");
        dialog.getDialogPane().setContent(comboBox);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                return "Todas".equals(comboBox.getValue()) ? Integer.MAX_VALUE : Integer.parseInt(comboBox.getValue());
            }
            return null;
        });

        return dialog.showAndWait().orElse(0);
    }

    private void handleCustomStudy() {
        ReviewDashboardView dashboard = view.getDashboard();
        if (dashboard == null || !dashboard.isCustomStudyMode()) return;

        List<String> selectedNames = dashboard.getSelectedThemeNames();
        if (selectedNames.isEmpty()) return;

        int count = askQuestionCount();
        if (count == 0) return;

        List<Theme> selectedThemes = new ArrayList<>();
        for (Theme theme : themes) {
            if (selectedNames.contains(theme.getName())) {
                selectedThemes.add(theme);
            }
        }

        if (selectedThemes.isEmpty()) return;

        RoundState roundState = RoundState.createCustomStudyRound(selectedThemes, count, statsService);
        if (roundState.getSelectedQuestionsCount() == 0) return;

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
