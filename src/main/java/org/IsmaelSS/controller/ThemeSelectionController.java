package org.IsmaelSS.controller;

import javafx.scene.control.Tab;
import org.IsmaelSS.model.RoundState;
import org.IsmaelSS.model.Theme;
import org.IsmaelSS.service.StatsService;
import org.IsmaelSS.service.ThemeLoader;
import org.IsmaelSS.view.QuestionFileManagerView;
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
    private QuestionFileManagerController questionFileManagerController;

    public ThemeSelectionController(ThemeLoader themeLoader, ThemeSelectionView view,
                                    ScreenController screenController, StatsService statsService) {
        this.themeLoader = themeLoader;
        this.view = view;
        this.screenController = screenController;
        this.statsService = statsService;
    }

    public void initialize() {
        refreshScores();

        screenController.registerScreen("themeSelection", view.getScene());

        view.getStartButton().setOnAction(e -> handleStart());

        view.getTabPane().getSelectionModel().selectedItemProperty().addListener(
                (obs, oldTab, newTab) -> handleTabSelection(newTab));

        screenController.switchTo("themeSelection");
    }

    /**
     * Reloads themes from disk and refreshes score displays.
     * Called after file create/delete to update the Jogar tab.
     */
    public void refreshScores() {
        themes = themeLoader.loadAllThemes();
        view.setThemes(themes);

        int maxQuestions = themes.stream()
                .mapToInt(Theme::getQuestionCount)
                .max()
                .orElse(1);
        view.updateQuestionCountRange(maxQuestions);

        for (Theme theme : themes) {
            String score = statsService.getAproveitamento(theme.getName());
            view.updateAproveitamento(theme.getName(), score);
            String dominio = statsService.getDominio(theme.getName());
            String dominioDisplay = "N/A".equals(dominio) ? "N/A" : dominio + "%";
            view.updateDominio(theme.getName(), dominioDisplay);
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
