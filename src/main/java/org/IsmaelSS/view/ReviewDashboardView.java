package org.IsmaelSS.view;

import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import org.IsmaelSS.model.FixationPhase;
import org.IsmaelSS.model.StatsData.QuestionScore;
import org.IsmaelSS.model.StatsData.ThemeStats;
import org.IsmaelSS.model.Theme;
import org.IsmaelSS.service.StatsService;

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
    private final VBox upcomingContainer;
    private final TimelineView timelineView;
    private final ScrollPane cardScrollPane;
    private final ScrollPane upcomingScrollPane;
    private final ScrollPane timelineScrollPane;
    private List<Theme> themes;

    public ReviewDashboardView(StatsService statsService, List<Theme> themes,
                               Function<Theme, Runnable> onReviewFactory,
                               Consumer<String> onMarkDone) {
        this.statsService = statsService;
        this.themes = new ArrayList<>(themes);

        // Title
        Label title = new Label("Revisão Espaçada");
        title.getStyleClass().add("title");

        // Search field
        searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText("Buscar tema...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterCards(newVal));

        // Card section (70% of available space)
        cardContainer = new VBox(12);
        cardScrollPane = new ScrollPane(cardContainer);
        cardScrollPane.setFitToWidth(true);
        cardScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        cardScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(cardScrollPane, Priority.ALWAYS);

        // Upcoming section (left half of bottom 30%)
        Label upcomingTitle = new Label("Próximos 7 dias");
        upcomingTitle.getStyleClass().add("title");
        upcomingContainer = new VBox(4);
        upcomingScrollPane = new ScrollPane(new VBox(4, upcomingTitle, upcomingContainer));
        upcomingScrollPane.setFitToWidth(true);
        upcomingScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        upcomingScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Timeline section (right half of bottom 30%)
        Label timelineTitle = new Label("Histórico de Estudos");
        timelineTitle.getStyleClass().add("title");
        timelineView = new TimelineView();
        timelineScrollPane = new ScrollPane(new VBox(4, timelineTitle, timelineView));
        timelineScrollPane.setFitToWidth(true);
        timelineScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        timelineScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Bottom row: upcoming | timeline
        HBox bottomRow = new HBox(8, upcomingScrollPane, timelineScrollPane);
        HBox.setHgrow(upcomingScrollPane, Priority.ALWAYS);
        HBox.setHgrow(timelineScrollPane, Priority.ALWAYS);

        // Root: no scroll
        root = new VBox(0, title, searchField, cardScrollPane, bottomRow);
        root.getStyleClass().add("background");
        root.setPadding(new Insets(16));

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        root.maxWidthProperty().set(screenBounds.getWidth());
        root.maxHeightProperty().set(screenBounds.getHeight());

        // Proportion: cards 70%, bottom 30%
        root.heightProperty().addListener((obs, oldVal, newVal) -> {
            double total = newVal.doubleValue() - 80;
            if (total > 0) {
                cardScrollPane.maxHeightProperty().set(total * 0.70);
                bottomRow.maxHeightProperty().set(total * 0.30);
            }
        });

        buildCards(onReviewFactory, onMarkDone);
        buildUpcoming();
        buildTimeline();
    }

    public void refresh(List<Theme> newThemes, Function<Theme, Runnable> onReviewFactory,
                        Consumer<String> onMarkDone) {
        this.themes = new ArrayList<>(newThemes);
        searchField.clear();
        buildCards(onReviewFactory, onMarkDone);
        buildUpcoming();
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

    private void buildUpcoming() {
        upcomingContainer.getChildren().clear();
        Map<String, List<Map.Entry<String, Integer>>> upcoming = statsService.getUpcomingReviews(7);
        if (upcoming.isEmpty()) {
            Label empty = new Label("Nenhuma revisão agendada.");
            empty.getStyleClass().add("label");
            upcomingContainer.getChildren().add(empty);
            return;
        }
        for (Map.Entry<String, List<Map.Entry<String, Integer>>> themeEntry : upcoming.entrySet()) {
            int minDays = themeEntry.getValue().stream()
                    .mapToInt(Map.Entry::getValue).min().orElse(1);
            LocalDate reviewDate = LocalDate.now().plusDays(minDays - 1);
            String dateStr = String.format("%02d/%02d", reviewDate.getDayOfMonth(), reviewDate.getMonthValue());
            HBox row = new HBox(8);
            row.setPadding(new Insets(2, 0, 2, 0));
            Label name = new Label(themeEntry.getKey());
            name.getStyleClass().add("section-title");
            Label date = new Label(dateStr);
            date.getStyleClass().add("label");
            row.getChildren().addAll(name, date);
            upcomingContainer.getChildren().add(row);
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
                boolean match = lower.isEmpty() || card.getThemeName().toLowerCase().contains(lower);
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
