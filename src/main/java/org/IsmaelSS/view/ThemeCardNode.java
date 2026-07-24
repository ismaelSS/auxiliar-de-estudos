package org.IsmaelSS.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.IsmaelSS.model.FixationPhase;
import org.IsmaelSS.model.StatsData.QuestionScore;
import org.IsmaelSS.model.StatsData.ThemeStats;

import java.util.EnumMap;
import java.util.Map;

public class ThemeCardNode extends VBox {

    public enum Priority { OVERDUE, TODAY, NONE }

    private final String themeName;
    private final Label nameLabel;
    private final Label dominioLabel;
    private final HBox badgeRow;
    private final HBox fixationBar;
    private final Button revisarBtn;
    private final Button feitoBtn;
    private final CheckBox studyCheckBox;
    private Runnable onSelectionChange;

    private static final String[] PHASE_STYLES = {
            "fixation-segment-aprendendo",
            "fixation-segment-revisao",
            "fixation-segment-fixa",
            "fixation-segment-dominio"
    };

    public ThemeCardNode(String themeName, int questionCount, int overdueCount,
                         String dominioPercent, Map<FixationPhase, Integer> fixationDist,
                         Runnable onReview, Runnable onMarkDone) {
        getStyleClass().add("theme-card");
        setPadding(new Insets(12, 16, 12, 16));
        setSpacing(8);
        this.themeName = themeName;

        // Row 1: name + badges + dominio
        nameLabel = new Label(themeName + " (" + questionCount + " questões)");
        nameLabel.getStyleClass().add("title");

        Label dominioPrefix = new Label("Domínio: ");
        dominioPrefix.getStyleClass().add("label");
        dominioLabel = new Label(dominioPercent + "%");
        dominioLabel.getStyleClass().add("label");

        badgeRow = new HBox(6);
        badgeRow.setAlignment(Pos.CENTER_LEFT);

        studyCheckBox = new CheckBox();
        studyCheckBox.setVisible(false);
        studyCheckBox.setManaged(false);
        studyCheckBox.getStyleClass().add("theme-card-checkbox");
        studyCheckBox.selectedProperty().addListener((obs, old, val) -> {
            if (onSelectionChange != null) onSelectionChange.run();
        });

        badgeRow.getChildren().addAll(studyCheckBox, nameLabel, dominioPrefix, dominioLabel);
        updateBadge(overdueCount);

        // Row 2: fixation bar
        fixationBar = new HBox(2);
        fixationBar.getStyleClass().add("fixation-bar");
        fixationBar.setAlignment(Pos.CENTER_LEFT);
        fixationBar.setMinHeight(8);
        fixationBar.setMaxHeight(8);
        fixationBar.setPrefHeight(8);
        buildFixationBar(fixationDist, questionCount);

        // Row 3: action buttons
        revisarBtn = new Button("Estudar");
        revisarBtn.getStyleClass().add("button-revisar");
        revisarBtn.setOnAction(e -> onReview.run());

        feitoBtn = new Button("Marcar como feita");
        feitoBtn.getStyleClass().add("button-feito");
        feitoBtn.setOnAction(e -> onMarkDone.run());

        HBox buttonRow = new HBox(8, revisarBtn, feitoBtn);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(badgeRow, fixationBar, buttonRow);
    }

    public String getThemeName() { return themeName; }

    public void setPriority(Priority priority) {
        getStyleClass().removeAll("priority-overdue", "priority-today", "priority-none");
        switch (priority) {
            case OVERDUE -> getStyleClass().add("priority-overdue");
            case TODAY   -> getStyleClass().add("priority-today");
            case NONE    -> getStyleClass().add("priority-none");
        }
    }

    public void refresh(int overdueCount, String dominioPercent,
                        Map<FixationPhase, Integer> fixationDist, int questionCount) {
        updateBadge(overdueCount);
        dominioLabel.setText(dominioPercent + "%");
        buildFixationBar(fixationDist, questionCount);
    }

    public void setCustomStudyMode(boolean active) {
        studyCheckBox.setVisible(active);
        studyCheckBox.setManaged(active);
    }

    public boolean isStudySelected() {
        return studyCheckBox.isSelected();
    }

    public void setSelected(boolean selected) {
        studyCheckBox.setSelected(selected);
    }

    public void setOnSelectionChange(Runnable listener) {
        this.onSelectionChange = listener;
    }

    private void updateBadge(int overdueCount) {
        badgeRow.getChildren().removeIf(n -> n.getStyleClass().contains("badge-overdue"));
        if (overdueCount > 0) {
            Label badge = new Label("\u23F0 " + overdueCount + " atrasadas");
            badge.getStyleClass().add("badge-overdue");
            badgeRow.getChildren().add(0, badge);
        }
    }

    private void buildFixationBar(Map<FixationPhase, Integer> dist, int totalQuestions) {
        fixationBar.getChildren().clear();
        if (totalQuestions == 0) return;
        for (FixationPhase phase : FixationPhase.values()) {
            int count = dist.getOrDefault(phase, 0);
            if (count > 0) {
                Region segment = new Region();
                segment.getStyleClass().add(PHASE_STYLES[phase.ordinal()]);
                double pct = (count * 100.0) / totalQuestions;
                segment.prefWidthProperty().bind(fixationBar.widthProperty().multiply(pct / 100.0));
                fixationBar.getChildren().add(segment);
            }
        }
    }

    public static Map<FixationPhase, Integer> computeFixationDist(ThemeStats themeStats) {
        Map<FixationPhase, Integer> dist = new EnumMap<>(FixationPhase.class);
        for (FixationPhase p : FixationPhase.values()) dist.put(p, 0);
        if (themeStats == null) return dist;
        for (QuestionScore qs : themeStats.getQuestions().values()) {
            dist.merge(qs.getFixationPhase(), 1, Integer::sum);
        }
        return dist;
    }
}
