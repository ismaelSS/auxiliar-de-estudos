package org.IsmaelSS.view;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * View for managing question .json files and their questions.
 * Provides a file list at the top and a question editor below.
 * Returns a VBox as root (no Scene — embedded as tab content).
 */
public class QuestionFileManagerView {
    private final VBox root;
    private final VBox fileListContainer;
    private final VBox editorSection;
    private final VBox questionListContainer;
    private final Label selectedFileLabel;
    private final Label questionCountLabel;
    private final Button novoArquivoButton;
    private final Button adicionarQuestaoButton;

    // Question editor field references, indexed by question position
    private final List<TextField> questionFields = new ArrayList<>();
    private final List<List<TextField>> optionFields = new ArrayList<>();
    private final List<ComboBox<Integer>> correctComboBoxes = new ArrayList<>();
    private final List<Button> removeButtons = new ArrayList<>();

    public QuestionFileManagerView() {
        root = new VBox(10);
        root.getStyleClass().add("background");
        root.setPadding(new Insets(20));

        // --- Top section: File list ---
        Label fileSectionTitle = new Label("Arquivos de Questões");
        fileSectionTitle.getStyleClass().add("title");

        novoArquivoButton = new Button("+ Novo Arquivo");
        novoArquivoButton.getStyleClass().add("button-primary");

        fileListContainer = new VBox(5);
        fileListContainer.setPadding(new Insets(5, 0, 0, 0));

        VBox fileSection = new VBox(8, fileSectionTitle, novoArquivoButton, fileListContainer);

        // --- Bottom section: Question editor (hidden until file selected) ---
        selectedFileLabel = new Label("Nenhum arquivo selecionado");
        selectedFileLabel.getStyleClass().add("section-title");

        questionCountLabel = new Label("");
        questionCountLabel.getStyleClass().add("title");

        questionListContainer = new VBox(10);

        adicionarQuestaoButton = new Button("+ Adicionar Questão");
        adicionarQuestaoButton.getStyleClass().add("button-primary");

        editorSection = new VBox(8, selectedFileLabel, questionCountLabel, questionListContainer, adicionarQuestaoButton);
        editorSection.getStyleClass().add("editor-bg");
        editorSection.setVisible(false);
        editorSection.setManaged(false);

        ScrollPane scrollPane = new ScrollPane(editorSection);
        scrollPane.getStyleClass().add("editor-bg");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefHeight(400);

        root.getChildren().addAll(fileSection, scrollPane);
    }

    public VBox getRoot() {
        return root;
    }

    public VBox getFileListContainer() {
        return fileListContainer;
    }

    public VBox getQuestionListContainer() {
        return questionListContainer;
    }

    public Label getSelectedFileLabel() {
        return selectedFileLabel;
    }

    public Label getQuestionCountLabel() {
        return questionCountLabel;
    }

    public Button getNovoArquivoButton() {
        return novoArquivoButton;
    }

    public Button getAdicionarQuestaoButton() {
        return adicionarQuestaoButton;
    }

    /**
     * Clears the question editor and rebuilds it for the given number of questions.
     */
    public void buildQuestionEditor(int questionCount, List<String> texts,
                                     List<List<String>> allOptions,
                                     List<Integer> correctAnswers) {
        questionListContainer.getChildren().clear();
        questionFields.clear();
        optionFields.clear();
        correctComboBoxes.clear();
        removeButtons.clear();

        for (int i = 0; i < questionCount; i++) {
            VBox card = createQuestionCard(i, texts.get(i), allOptions.get(i), correctAnswers.get(i));
            questionListContainer.getChildren().add(card);
        }

        questionCountLabel.setText("Total: " + questionCount + " questão(ões)");
    }

    /**
     * Shows the editor section for a selected file.
     */
    public void showEditor(String fileName) {
        selectedFileLabel.setText("Editando: " + fileName);
        editorSection.setVisible(true);
        editorSection.setManaged(true);
    }

    /**
     * Hides the editor section.
     */
    public void hideEditor() {
        selectedFileLabel.setText("Nenhum arquivo selecionado");
        questionCountLabel.setText("");
        editorSection.setVisible(false);
        editorSection.setManaged(false);
    }

    private VBox createQuestionCard(int index, String text, List<String> options, int correct) {
        Label indexLabel = new Label("Questão " + (index + 1));
        indexLabel.getStyleClass().add("section-title");

        TextField questionField = new TextField(text);
        questionField.setPromptText("Texto da questão");
        questionField.getStyleClass().add("text-field");
        questionField.getProperties().put("questionIndex", index);

        HBox questionHeader = new HBox(10, indexLabel, questionField);
        questionHeader.setPadding(new Insets(0, 0, 0, 10));
        HBox.setHgrow(questionField, javafx.scene.layout.Priority.ALWAYS);

        VBox optionsBox = new VBox(3);
        String[] labels = {"A", "B", "C", "D", "E"};
        List<TextField> optFields = new ArrayList<>();
        for (int j = 0; j < 5; j++) {
            String optText = (j < options.size()) ? options.get(j) : "";
            TextField optField = new TextField(optText);
            optField.setPromptText("Alternativa " + labels[j]);
            optField.getStyleClass().add("text-field");
            optField.getProperties().put("questionIndex", index);
            optField.getProperties().put("optionIndex", j);
            optFields.add(optField);

            HBox optRow = new HBox(5, new Label(labels[j] + ")"), optField);
            HBox.setHgrow(optField, javafx.scene.layout.Priority.ALWAYS);
            optionsBox.getChildren().add(optRow);
        }
        optionFields.add(optFields);

        ComboBox<Integer> correctCombo = new ComboBox<>();
        correctCombo.getItems().addAll(0, 1, 2, 3, 4);
        correctCombo.setValue(correct);
        correctCombo.getProperties().put("questionIndex", index);

        Label correctLabel = new Label("Resposta correta:");
        correctLabel.getStyleClass().add("label");
        HBox correctRow = new HBox(5, correctLabel, correctCombo);

        Button removeBtn = new Button("×");
        removeBtn.getStyleClass().add("button-secondary");
        removeBtn.getProperties().put("questionIndex", index);

        questionFields.add(questionField);
        correctComboBoxes.add(correctCombo);
        removeButtons.add(removeBtn);

        VBox card = new VBox(5, questionHeader, optionsBox, correctRow, removeBtn);
        card.setPadding(new Insets(8));
        card.getStyleClass().add("surface-card");

        return card;
    }

    public List<TextField> getQuestionFields() {
        return questionFields;
    }

    public List<List<TextField>> getOptionFields() {
        return optionFields;
    }

    public List<ComboBox<Integer>> getCorrectComboBoxes() {
        return correctComboBoxes;
    }

    public List<Button> getRemoveButtons() {
        return removeButtons;
    }
}
