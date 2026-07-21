package org.IsmaelSS.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class TimelineView extends VBox {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM");

    public TimelineView() {
        getStyleClass().add("background");
        setPadding(new Insets(0, 0, 0, 0));
        setSpacing(4);
    }

    public void setData(Map<LocalDate, Map<String, Integer>> timelineData) {
        getChildren().clear();
        if (timelineData.isEmpty()) {
            showEmpty("Nenhum estudo registrado ainda.");
            return;
        }
        for (Map.Entry<LocalDate, Map<String, Integer>> dayEntry : timelineData.entrySet()) {
            LocalDate date = dayEntry.getKey();
            Map<String, Integer> themes = dayEntry.getValue();
            int totalDay = themes.values().stream().mapToInt(Integer::intValue).sum();

            Label dateLabel = new Label(date.format(DATE_FMT));
            dateLabel.getStyleClass().add("timeline-date");
            dateLabel.setMinWidth(40);

            VBox line = new VBox();
            line.getStyleClass().add("timeline-line");
            line.setMinHeight(24);
            line.setMaxHeight(24);

            VBox entriesBox = new VBox(2);
            for (Map.Entry<String, Integer> te : themes.entrySet()) {
                Label entry = new Label(te.getKey() + " (" + te.getValue() + " questões)");
                entry.getStyleClass().add("timeline-entry");
                entriesBox.getChildren().add(entry);
            }

            HBox row = new HBox(8, dateLabel, line, entriesBox);
            row.setAlignment(Pos.TOP_LEFT);
            getChildren().add(row);
        }
    }

    public void showEmpty(String message) {
        getChildren().clear();
        Label empty = new Label(message);
        empty.getStyleClass().add("label");
        getChildren().add(empty);
    }
}
