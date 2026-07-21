package org.IsmaelSS.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.IsmaelSS.model.FixationPhase;
import org.IsmaelSS.model.StatsData.QuestionScore;
import org.IsmaelSS.model.StatsData.ThemeStats;
import org.IsmaelSS.model.Theme;
import org.IsmaelSS.service.StatsService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ReviewDashboardView {
    private final StatsService statsService;
    private final VBox root;
    private final TextField searchField;
    private final VBox cardContainer;
    private final TimelineView timelineView;
    private final ScrollPane scrollPane;
    private List<Theme> themes;

    public ReviewDashboardView(StatsService statsService, List<Theme> themes,
                               Function<Theme, Runnable> onReviewFactory,
                               Consumer<String> onMarkDone) {
        this.statsService = statsService;
        this.themes = new ArrayList<>(themes);

        VBox content = new VBox(16);
        content.getStyleClass().add("background");
        content.setPadding(new Insets(24));

        Label title = new Label("Revisão Espaçada");
        title.getStyleClass().add("title");

        searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText("Buscar tema...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterCards(newVal));

        cardContainer = new VBox(12);

        Separator sep = new Separator();

        Label timelineTitle = new Label("Histórico de Estudos");
        timelineTitle.getStyleClass().add("title");

        timelineView = new TimelineView();

        content.getChildren().addAll(title, searchField, cardContainer, sep, timelineTitle, timelineView);

        scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        root = new VBox(scrollPane);

        buildCards(onReviewFactory, onMarkDone);
        buildTimeline();
    }

    public void refresh(List<Theme> newThemes, Function<Theme, Runnable> onReviewFactory,
                        Consumer<String> onMarkDone) {
        this.themes = new ArrayList<>(newThemes);
        searchField.clear();
        buildCards(onReviewFactory, onMarkDone);
        buildTimeline();
    }

    public TextField getSearchField() { return searchField; }
    public VBox getContent() { return root; }

    private void buildCards(Function<Theme, Runnable> onReviewFactory, Consumer<String> onMarkDone) {
        cardContainer.getChildren().clear();
        List<Theme> sorted = new ArrayList<>(themes);
        sorted.sort(Comparator.comparing((Theme t) -> getThemePriority(t).ordinal())
                .thenComparing(Theme::getName));

        for (Theme theme : sorted) {
            ThemeStats ts = statsService.getThemeStats(theme.getName());
            int overdue = statsService.getDueCount(theme.getName());
            String dominio = statsService.getDominio(theme.getName());
            String dominioDisplay = "N/A".equals(dominio) ? "0" : dominio;
            Map<FixationPhase, Integer> dist = ThemeCardNode.computeFixationDist(ts);

            ThemeCardNode card = new ThemeCardNode(
                    theme.getName(), theme.getQuestionCount(), overdue,
                    dominioDisplay, dist,
                    onReviewFactory.apply(theme),
                    () -> onMarkDone.accept(theme.getName())
            );
            card.setPriority(getThemePriority(theme));
            cardContainer.getChildren().add(card);
        }
    }

    private void buildTimeline() {
        Map<LocalDate, Map<String, Integer>> data = statsService.getTimelineData();
        if (data.isEmpty()) {
            timelineView.showEmpty("Nenhum estudo registrado ainda.");
        } else {
            timelineView.setData(data);
        }
    }

    private void filterCards(String query) {
        String lower = query == null ? "" : query.toLowerCase();
        for (Node node : cardContainer.getChildren()) {
            if (node instanceof ThemeCardNode card) {
                boolean match = lower.isEmpty() || card.toString().toLowerCase().contains(lower);
                card.setVisible(match);
                card.setManaged(match);
            }
        }
    }

    private ThemeCardNode.Priority getThemePriority(Theme theme) {
        int dueCount = statsService.getDueCount(theme.getName());
        if (dueCount > 0) return ThemeCardNode.Priority.OVERDUE;
        ThemeStats ts = statsService.getThemeStats(theme.getName());
        if (ts != null) {
            long now = System.currentTimeMillis();
            long todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long todayEnd = todayStart + 86_400_000L;
            for (QuestionScore qs : ts.getQuestions().values()) {
                long nr = qs.getNextReviewTimestamp();
                if (nr > todayStart && nr <= todayEnd) return ThemeCardNode.Priority.TODAY;
            }
        }
        return ThemeCardNode.Priority.NONE;
    }
}
