package org.IsmaelSS.controller;

import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
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

        // Resumo Geral — keep existing overall stats display
        OverallStats overall = statsService.getOverallStats();
        view.getOverallBox().getChildren().add(
                new Label("Total de questões respondidas: " + overall.getTotalAnswered()));
        view.getOverallBox().getChildren().add(
                new Label("Total de acertos: " + overall.getTotalCorrect()));

        // Accordion: one TitledPane per theme with data
        List<String> themesWithData = statsService.getAllThemesWithData();
        if (themesWithData.isEmpty()) {
            view.getAccordion().getPanes().add(
                    new TitledPane("Nenhum dado disponível ainda.", new Label("")));
        } else {
            for (String themeName : themesWithData) {
                // Get per-theme lowest-score questions (limit 10)
                List<Map.Entry<String, Integer>> lowestQuestions =
                        statsService.getLowestScoreQuestionsByTheme(themeName, 10);

                // Build TitledPane content
                VBox themeContent = new VBox(5);
                themeContent.setPadding(new Insets(5));

                if (lowestQuestions.isEmpty()) {
                    themeContent.getChildren().add(
                            new Label("Nenhuma questão respondida ainda."));
                } else {
                    for (Map.Entry<String, Integer> entry : lowestQuestions) {
                        themeContent.getChildren().add(
                                new Label(entry.getKey() + " — Pontuação: " + entry.getValue()));
                    }
                }

                // "Copiar prompt IA" button
                Button aiButton = new Button("Copiar prompt IA");
                aiButton.setOnAction(e -> copyAIPrompt(themeName, lowestQuestions));
                themeContent.getChildren().add(aiButton);

                // TitledPane title: theme name + dominio
                String dominio = statsService.getDominio(themeName);
                String dominioDisplay = "N/A".equals(dominio) ? "N/A" : dominio + "%";
                ThemeStats ts = statsService.getThemeStats(themeName);
                String titleText = themeName + " (Domínio: " + dominioDisplay + ")";
                if (ts != null) {
                    titleText += " — " + ts.getTotalCorrect() + "/" + ts.getTotalAnswered() + " acertos";
                }

                TitledPane pane = new TitledPane(titleText, themeContent);
                pane.setExpanded(false);  // drawers initially collapsed
                view.getAccordion().getPanes().add(pane);
            }
        }
    }

    private void copyAIPrompt(String themeName, List<Map.Entry<String, Integer>> lowestQuestions) {
        StringBuilder sb = new StringBuilder();
        sb.append("Explique os seguintes tópicos de ").append(themeName).append(" em detalhes:\n\n");
        for (Map.Entry<String, Integer> entry : lowestQuestions) {
            sb.append("- Questão ").append(entry.getKey())
              .append(" (pontuação: ").append(entry.getValue()).append(")\n");
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }
}
