package org.IsmaelSS.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import org.IsmaelSS.model.Theme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThemeSelectionView {
    private final Scene scene;
    private final TabPane root;
    private final Tab jogarTab;
    private final Tab relatoriosTab;
    private final Tab gerenciarTab;
    private final VBox themeListContainer;
    private final Spinner<Integer> questionCountSpinner;
    private final CheckBox reforcoCheckBox;
    private final Button startButton;
    private final Label feedbackLabel;

    private final Map<String, CheckBox> themeCheckboxes = new HashMap<>();
    private final Map<String, Label> themeLabels = new HashMap<>();
    private final Map<String, Theme> themeMap = new HashMap<>();
    private final Map<String, String> themeScores = new HashMap<>();
    private final Map<String, String> themeDominioText = new HashMap<>();

    public ThemeSelectionView() {
        // Jogar tab content
        Label title = new Label("Selecione os temas");
        title.getStyleClass().add("title");

        themeListContainer = new VBox(5);

        Label countLabel = new Label("Quantidade de questões por tema:");
        questionCountSpinner = new Spinner<>(1, 100, 5);
        questionCountSpinner.setEditable(true);

        reforcoCheckBox = new CheckBox("Modo Reforço (priorizar questões com mais erros)");

        startButton = new Button("Iniciar");

        feedbackLabel = new Label();
        feedbackLabel.getStyleClass().add("error-text");

        VBox jogarContent = new VBox(10);
        jogarContent.setPadding(new Insets(20));
        jogarContent.getChildren().addAll(title, themeListContainer, countLabel, questionCountSpinner, reforcoCheckBox, startButton, feedbackLabel);

        jogarTab = new Tab("Treinar", jogarContent);
        jogarTab.setClosable(false);

        relatoriosTab = new Tab("Relatórios");
        relatoriosTab.setClosable(false);

        gerenciarTab = new Tab("Gerenciar");
        gerenciarTab.setClosable(false);

        root = new TabPane(jogarTab, relatoriosTab, gerenciarTab);
        root.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        scene = new Scene(root);
    }

    public Scene getScene() {
        return scene;
    }

    public TabPane getTabPane() {
        return root;
    }

    public Tab getJogarTab() {
        return jogarTab;
    }

    public Tab getRelatoriosTab() {
        return relatoriosTab;
    }

    public Tab getGerenciarTab() {
        return gerenciarTab;
    }

    public void setThemes(List<Theme> themes) {
        themeListContainer.getChildren().clear();
        themeCheckboxes.clear();
        themeLabels.clear();
        themeMap.clear();
        themeScores.clear();
        themeDominioText.clear();

        for (Theme theme : themes) {
            themeMap.put(theme.getName(), theme);

            CheckBox checkBox = new CheckBox();
            Label info = new Label(theme.getName() + " (" + theme.getQuestionCount() + " perguntas) — Pontuação: N/A | Domínio: N/A");

            themeCheckboxes.put(theme.getName(), checkBox);
            themeLabels.put(theme.getName(), info);

            VBox row = new VBox(2, checkBox, info);
            row.setPadding(new Insets(5, 0, 5, 10));
            themeListContainer.getChildren().add(row);
        }

        if (themes.isEmpty()) {
            themeListContainer.getChildren().add(new Label("Nenhum tema encontrado. Adicione arquivos JSON à pasta themes/."));
        }
    }

    public List<Theme> getSelectedThemes() {
        List<Theme> selected = new ArrayList<>();
        for (Map.Entry<String, CheckBox> entry : themeCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selected.add(themeMap.get(entry.getKey()));
            }
        }
        return selected;
    }

    public int getQuestionCount() {
        return questionCountSpinner.getValue();
    }

    public boolean isReinforcementMode() {
        return reforcoCheckBox.isSelected();
    }

    public CheckBox getReforcoCheckBox() {
        return reforcoCheckBox;
    }

    public Button getStartButton() {
        return startButton;
    }

    public void setFeedback(String message) {
        feedbackLabel.setText(message);
    }

    public void updateAproveitamento(String themeName, String score) {
        themeScores.put(themeName, score);
        Label info = themeLabels.get(themeName);
        if (info != null) {
            Theme theme = themeMap.get(themeName);
            if (theme != null) {
                String dominio = themeDominioText.getOrDefault(themeName, "N/A");
                info.setText(theme.getName() + " (" + theme.getQuestionCount()
                        + " perguntas) — Pontuação: " + score + " | Domínio: " + dominio);
            }
        }
    }

    public void updateDominio(String themeName, String dominio) {
        themeDominioText.put(themeName, dominio);
        Label info = themeLabels.get(themeName);
        if (info != null) {
            Theme theme = themeMap.get(themeName);
            if (theme != null) {
                String score = themeScores.getOrDefault(themeName, "N/A");
                info.setText(theme.getName() + " (" + theme.getQuestionCount()
                        + " perguntas) — Pontuação: " + score + " | Domínio: " + dominio);
            }
        }
    }

    public void updateQuestionCountRange(int max) {
        questionCountSpinner.setValueFactory(
                new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(1, Math.max(1, max), Math.min(5, max)));
    }
}
