package org.IsmaelSS.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;
import org.IsmaelSS.model.Theme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThemeSelectionView {
    private final Scene scene;
    private final VBox root;
    private final VBox themeListContainer;
    private final Spinner<Integer> questionCountSpinner;
    private final CheckBox reforcoCheckBox;
    private final Button startButton;
    private final Button relatoriosButton;
    private final Label feedbackLabel;

    private final Map<String, CheckBox> themeCheckboxes = new HashMap<>();
    private final Map<String, Label> themeLabels = new HashMap<>();
    private final Map<String, Theme> themeMap = new HashMap<>();
    private final Map<String, String> themeScores = new HashMap<>();
    private final Map<String, String> themeDominioText = new HashMap<>();

    public ThemeSelectionView() {
        root = new VBox(10);
        root.setPadding(new Insets(20));

        Label title = new Label("Selecione os temas");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        themeListContainer = new VBox(5);

        Label countLabel = new Label("Quantidade de questões por tema:");
        questionCountSpinner = new Spinner<>(1, 100, 5);
        questionCountSpinner.setEditable(true);

        reforcoCheckBox = new CheckBox("Modo Reforço (priorizar questões com mais erros)");

        startButton = new Button("Iniciar");

        relatoriosButton = new Button("Relatórios");

        feedbackLabel = new Label();
        feedbackLabel.setStyle("-fx-text-fill: red;");

        root.getChildren().addAll(title, themeListContainer, countLabel, questionCountSpinner, reforcoCheckBox, startButton, relatoriosButton, feedbackLabel);

        scene = new Scene(root, 600, 400);
    }

    public Scene getScene() {
        return scene;
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

    public Button getRelatoriosButton() {
        return relatoriosButton;
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
