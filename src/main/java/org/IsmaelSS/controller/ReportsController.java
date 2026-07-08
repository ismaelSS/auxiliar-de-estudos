package org.IsmaelSS.controller;

import javafx.scene.control.Label;
import org.IsmaelSS.model.StatsData.OverallStats;
import org.IsmaelSS.model.StatsData.ThemeStats;
import org.IsmaelSS.service.StatsService;
import org.IsmaelSS.view.ReportsView;

import java.util.List;
import java.util.Map;

public class ReportsController {
    private final StatsService statsService;
    private final ReportsView view;
    private final ScreenController screenController;

    public ReportsController(StatsService statsService, ReportsView view, ScreenController screenController) {
        this.statsService = statsService;
        this.view = view;
        this.screenController = screenController;
    }

    public void initialize() {
        refresh();
        screenController.registerScreen("reports", view.getScene());
        view.getVoltarButton().setOnAction(e -> screenController.switchTo("themeSelection"));
    }

    public void refresh() {
        view.clearContent();

        OverallStats overall = statsService.getOverallStats();
        view.getOverallBox().getChildren().add(
                new Label("Total de questões respondidas: " + overall.getTotalAnswered()));
        view.getOverallBox().getChildren().add(
                new Label("Total de acertos: " + overall.getTotalCorrect()));

        List<String> themesWithData = statsService.getAllThemesWithData();
        if (themesWithData.isEmpty()) {
            view.getThemeBox().getChildren().add(new Label("Nenhum dado disponível ainda."));
        } else {
            for (String themeName : themesWithData) {
                String score = statsService.getAproveitamento(themeName);
                ThemeStats ts = statsService.getThemeStats(themeName);
                if (ts != null) {
                    view.getThemeBox().getChildren().add(
                            new Label("Tema: " + themeName + " — Acertos: " + ts.getTotalCorrect()
                                    + "/" + ts.getTotalAnswered() + " (Pontuação: " + score + ")"));
                } else {
                    view.getThemeBox().getChildren().add(
                            new Label("Tema: " + themeName + " — Nenhum dado ainda."));
                }
            }
        }

        List<Map.Entry<String, Integer>> lowestScores = statsService.getLowestScoreQuestions(5);
        if (lowestScores.isEmpty()) {
            view.getErrorBox().getChildren().add(new Label("Nenhum dado disponível ainda."));
        } else {
            for (Map.Entry<String, Integer> entry : lowestScores) {
                view.getErrorBox().getChildren().add(
                        new Label(entry.getKey() + " — Pontuação: " + entry.getValue()));
            }
        }
    }
}
