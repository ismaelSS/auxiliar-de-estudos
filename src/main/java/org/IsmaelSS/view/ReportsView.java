package org.IsmaelSS.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

public class ReportsView {
    private final Scene scene;
    private final VBox root;
    private final VBox overallBox;
    private final Accordion accordion;
    private final Button voltarButton;

    public ReportsView() {
        VBox content = new VBox(10);
        content.getStyleClass().add("background");
        content.setPadding(new Insets(20));

        Label title = new Label("Relatórios de Desempenho");
        title.getStyleClass().add("title");
        content.getChildren().add(title);

        Label section1 = new Label("Resumo Geral");
        section1.getStyleClass().add("section-title");
        content.getChildren().addAll(section1, new Separator());

        overallBox = new VBox(5);
        overallBox.setPadding(new Insets(5, 0, 10, 10));
        content.getChildren().add(overallBox);

        Label section2 = new Label("Questões de Menor Pontuação por Tema");
        section2.getStyleClass().add("section-title");
        content.getChildren().addAll(section2, new Separator());

        accordion = new Accordion();
        accordion.setMaxHeight(Double.MAX_VALUE);
        content.getChildren().add(accordion);

        voltarButton = new Button("Voltar");
        voltarButton.getStyleClass().add("button-primary");
        content.getChildren().add(voltarButton);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        root = new VBox(scrollPane);
        scene = new Scene(root);
    }

    public Scene getScene() { return scene; }
    public VBox getContent() { return root; }
    public VBox getOverallBox() { return overallBox; }
    public Accordion getAccordion() { return accordion; }
    public Button getVoltarButton() { return voltarButton; }

    public void clearContent() {
        overallBox.getChildren().clear();
        accordion.getPanes().clear();
    }
}
