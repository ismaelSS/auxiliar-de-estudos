package org.IsmaelSS.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public class StudyRoundView {
    private final Scene scene;
    private final VBox root;
    private final VBox questionContent;
    private final VBox completionContent;
    private final Label progressLabel;
    private final Label questionLabel;
    private final Button[] optionButtons;
    private final Button exitButton;
    private final Label completionLabel;
    private final Button voltarButton;
    private Consumer<Integer> onOptionClick;
    private Runnable onExit;
    private Runnable onVoltar;

    public StudyRoundView() {
        root = new VBox(10);
        root.setPadding(new Insets(20));

        progressLabel = new Label();
        progressLabel.setStyle("-fx-font-size: 14px;");

        questionContent = new VBox(10);
        questionLabel = new Label();
        questionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        questionLabel.setWrapText(true);

        optionButtons = new Button[5];
        for (int i = 0; i < 5; i++) {
            int index = i;
            Button btn = new Button();
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: black; -fx-cursor: hand; -fx-padding: 8;");
            btn.setOnAction(e -> {
                if (onOptionClick != null) onOptionClick.accept(index);
            });
            optionButtons[i] = btn;
        }

        exitButton = new Button("Sair");
        exitButton.setOnAction(e -> {
            if (onExit != null) onExit.run();
        });

        questionContent.getChildren().add(progressLabel);
        questionContent.getChildren().add(questionLabel);
        questionContent.getChildren().addAll(optionButtons);
        questionContent.getChildren().add(exitButton);

        completionContent = new VBox(10);
        completionLabel = new Label();
        completionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        voltarButton = new Button("Voltar");
        voltarButton.setOnAction(e -> {
            if (onVoltar != null) onVoltar.run();
        });

        completionContent.getChildren().addAll(completionLabel, voltarButton);
        completionContent.setVisible(false);

        root.getChildren().addAll(questionContent, completionContent);
        scene = new Scene(root, 600, 400);
    }

    public Scene getScene() {
        return scene;
    }

    public void setQuestion(String question, List<String> options) {
        questionContent.setVisible(true);
        completionContent.setVisible(false);
        questionLabel.setText(question);
        for (int i = 0; i < 5; i++) {
            optionButtons[i].setText(options.get(i));
            optionButtons[i].setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: black; -fx-cursor: hand; -fx-padding: 8;");
            optionButtons[i].setDisable(false);
        }
        exitButton.setDisable(false);
    }

    public void highlightCorrect(int correctIndex) {
        optionButtons[correctIndex].setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8;");
    }

    public void highlightWrong(int wrongIndex, int correctIndex) {
        optionButtons[wrongIndex].setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8;");
        optionButtons[correctIndex].setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8;");
    }

    public void disableOptions(boolean disabled) {
        for (Button btn : optionButtons) {
            btn.setDisable(disabled);
        }
        exitButton.setDisable(disabled);
    }

    public void updateProgress(String text) {
        progressLabel.setText(text);
    }

    public void showRoundComplete(int correct, int total) {
        questionContent.setVisible(false);
        completionContent.setVisible(true);
        completionLabel.setText("Round completo!\nAcertos: " + correct + " / " + total);
    }

    public void setOnOptionClick(Consumer<Integer> handler) {
        this.onOptionClick = handler;
    }

    public void setOnExit(Runnable handler) {
        this.onExit = handler;
    }

    public void setOnVoltar(Runnable handler) {
        this.onVoltar = handler;
    }
}
