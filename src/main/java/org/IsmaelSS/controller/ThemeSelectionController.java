package org.IsmaelSS.controller;

import org.IsmaelSS.model.RoundState;
import org.IsmaelSS.model.Theme;
import org.IsmaelSS.service.StatsService;
import org.IsmaelSS.service.ThemeLoader;
import org.IsmaelSS.view.ReportsView;
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

    public ThemeSelectionController(ThemeLoader themeLoader, ThemeSelectionView view,
                                    ScreenController screenController, StatsService statsService) {
        this.themeLoader = themeLoader;
        this.view = view;
        this.screenController = screenController;
        this.statsService = statsService;
    }

    public void initialize() {
        themes = themeLoader.loadAllThemes();
        view.setThemes(themes);
        refreshScores();

        int maxQuestions = themes.stream()
                .mapToInt(Theme::getQuestionCount)
                .max()
                .orElse(1);
        view.updateQuestionCountRange(maxQuestions);

        screenController.registerScreen("themeSelection", view.getScene());

        view.getStartButton().setOnAction(e -> handleStart());
        view.getRelatoriosButton().setOnAction(e -> handleRelatorios());

        screenController.switchTo("themeSelection");
    }

    public void refreshScores() {
        for (Theme theme : themes) {
            String score = statsService.getAproveitamento(theme.getName());
            view.updateAproveitamento(theme.getName(), score);
        }
    }

    private void handleStart() {
        List<Theme> selectedThemes = view.getSelectedThemes();

        if (selectedThemes.isEmpty()) {
            view.setFeedback("Selecione pelo menos um tema para iniciar.");
            return;
        }

        view.setFeedback("");

        int questionsPerTheme = view.getQuestionCount();
        RoundState roundState;
        if (view.isReinforcementMode()) {
            roundState = RoundState.createReinforcementRound(selectedThemes, questionsPerTheme, statsService);
        } else {
            roundState = new RoundState(selectedThemes, questionsPerTheme);
        }
        StudyRoundView studyRoundView = new StudyRoundView();
        StudyRoundController studyRoundController = new StudyRoundController(roundState, studyRoundView, screenController, statsService);
        studyRoundController.setOnRoundEndCallback(() -> {
        refreshScores();
            screenController.switchTo("themeSelection");
        });
        studyRoundController.initialize();
    }

    private void handleRelatorios() {
        if (reportsController == null) {
            ReportsView reportsView = new ReportsView();
            reportsController = new ReportsController(statsService, reportsView, screenController);
            reportsController.initialize();
        } else {
            reportsController.refresh();
            screenController.switchTo("reports");
        }
    }
}
