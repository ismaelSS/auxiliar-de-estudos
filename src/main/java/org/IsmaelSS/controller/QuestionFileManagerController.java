package org.IsmaelSS.controller;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.IsmaelSS.model.Question;
import org.IsmaelSS.service.ThemeLoader;
import org.IsmaelSS.view.QuestionFileManagerView;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the QuestionFileManagerView.
 * Handles file CRUD (create/delete .json files) and question editing with auto-save.
 */
public class QuestionFileManagerController {
    private final ThemeLoader themeLoader;
    private final ThemeSelectionController themeSelectionController;
    private final QuestionFileManagerView view;

    private String currentFileName;
    private List<Question> currentQuestions;

    public QuestionFileManagerController(ThemeLoader themeLoader,
                                          ThemeSelectionController themeSelectionController,
                                          QuestionFileManagerView view) {
        this.themeLoader = themeLoader;
        this.themeSelectionController = themeSelectionController;
        this.view = view;
    }

    public void initialize() {
        loadFileList();

        view.getNovoArquivoButton().setOnAction(e -> handleNewFile());
        view.getAdicionarQuestaoButton().setOnAction(e -> handleAddQuestion());
    }

    /**
     * Loads all .json files from themes/ and displays them as clickable rows.
     */
    public void loadFileList() {
        VBox container = view.getFileListContainer();
        container.getChildren().clear();

        List<String> fileNames = themeLoader.listThemeFiles();

        if (fileNames.isEmpty()) {
            Label emptyLabel = new Label("Nenhum arquivo encontrado.");
            emptyLabel.getStyleClass().add("label");
            container.getChildren().add(emptyLabel);
            return;
        }

        for (String name : fileNames) {
            List<Question> questions = themeLoader.loadThemeQuestions(name);
            int count = questions.size();

            Label nameLabel = new Label(name + " (" + count + " questões)");
            nameLabel.getStyleClass().add("label");

            Button selectBtn = new Button("Abrir");
            selectBtn.getStyleClass().add("button-secondary");
            selectBtn.setOnAction(e -> handleSelectFile(name));

            Button deleteBtn = new Button("×");
            deleteBtn.getStyleClass().add("button-secondary");
            deleteBtn.setOnAction(e -> handleDeleteFile(name));

            HBox row = new HBox(10, nameLabel, selectBtn, deleteBtn);
            row.setPadding(new javafx.geometry.Insets(5));
            row.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 4;");
            HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);

            container.getChildren().add(row);
        }
    }

    private void handleNewFile() {
        try {
            File themesDir = new File(System.getProperty("user.dir"), "themes");
            if (!themesDir.exists()) {
                themesDir.mkdirs();
            }
            Desktop.getDesktop().open(themesDir);
            loadFileList();
        } catch (Exception ex) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Não foi possível abrir a pasta de arquivos.");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private void handleDeleteFile(String name) {
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Exclusão");
        confirm.setHeaderText("Deseja excluir o arquivo " + name + ".json?");
        confirm.setContentText("Esta ação não pode ser desfeita.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                themeLoader.deleteTheme(name);
                if (name.equals(currentFileName)) {
                    currentFileName = null;
                    currentQuestions = null;
                    view.hideEditor();
                }
                loadFileList();
                themeSelectionController.refreshScores();
            }
        });
    }

    private void handleSelectFile(String name) {
        currentFileName = name;
        currentQuestions = themeLoader.loadThemeQuestions(name);

        List<String> texts = new ArrayList<>();
        List<List<String>> allOptions = new ArrayList<>();
        List<Integer> correctAnswers = new ArrayList<>();

        for (Question q : currentQuestions) {
            texts.add(q.getQuestion() != null ? q.getQuestion() : "");
            allOptions.add(q.getOptions() != null ? q.getOptions() : List.of("", "", "", "", ""));
            correctAnswers.add(q.getCorrect());
        }

        view.showEditor(name);
        view.buildQuestionEditor(currentQuestions.size(), texts, allOptions, correctAnswers);
        bindEditorListeners();
    }

    private void bindEditorListeners() {
        // Question text field focus-lost listeners
        for (int i = 0; i < view.getQuestionFields().size(); i++) {
            final int idx = i;
            view.getQuestionFields().get(i).focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    currentQuestions.get(idx).setQuestion(view.getQuestionFields().get(idx).getText());
                    autoSave();
                }
            });
        }

        // Option text field focus-lost listeners
        for (int i = 0; i < view.getOptionFields().size(); i++) {
            final int qIdx = i;
            for (int j = 0; j < view.getOptionFields().get(i).size(); j++) {
                final int oIdx = j;
                view.getOptionFields().get(i).get(j).focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) {
                        List<String> opts = new ArrayList<>(currentQuestions.get(qIdx).getOptions());
                        while (opts.size() <= oIdx) {
                            opts.add("");
                        }
                        opts.set(oIdx, view.getOptionFields().get(qIdx).get(oIdx).getText());
                        currentQuestions.get(qIdx).setOptions(opts);
                        autoSave();
                    }
                });
            }
        }

        // Correct answer ComboBox change listeners
        for (int i = 0; i < view.getCorrectComboBoxes().size(); i++) {
            final int idx = i;
            view.getCorrectComboBoxes().get(i).valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    currentQuestions.get(idx).setCorrect(newVal);
                    autoSave();
                }
            });
        }

        // Remove button listeners
        for (int i = 0; i < view.getRemoveButtons().size(); i++) {
            final int idx = i;
            view.getRemoveButtons().get(i).setOnAction(e -> handleRemoveQuestion(idx));
        }
    }

    private void handleAddQuestion() {
        if (currentFileName == null || currentQuestions == null) {
            return;
        }

        int nextId = 1;
        for (Question q : currentQuestions) {
            if (q.getId() >= nextId) {
                nextId = q.getId() + 1;
            }
        }

        Question newQuestion = new Question(nextId, "", List.of("", "", "", "", ""), 0);
        currentQuestions.add(newQuestion);
        autoSave();

        // Rebuild editor
        handleSelectFile(currentFileName);
    }

    private void handleRemoveQuestion(int index) {
        if (currentQuestions == null || index < 0 || index >= currentQuestions.size()) {
            return;
        }

        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Exclusão");
        confirm.setHeaderText("Deseja excluir a questão " + (index + 1) + "?");
        confirm.setContentText("Esta ação não pode ser desfeita.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                currentQuestions.remove(index);

                // Re-index IDs sequentially
                for (int i = 0; i < currentQuestions.size(); i++) {
                    currentQuestions.get(i).setId(i + 1);
                }

                autoSave();
                handleSelectFile(currentFileName);
            }
        });
    }

    private void autoSave() {
        if (currentFileName == null || currentQuestions == null) {
            return;
        }
        themeLoader.saveTheme(currentFileName, currentQuestions);
    }

    /**
     * Sanitizes a title for use as a filename:
     * trim, lowercase, replace non-alphanumeric with -, remove leading/trailing dashes.
     */
    static String sanitizeTitle(String title) {
        String sanitized = title.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return sanitized.isEmpty() ? "novo-tema" : sanitized;
    }
}
