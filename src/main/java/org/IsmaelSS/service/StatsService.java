package org.IsmaelSS.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.IsmaelSS.model.FixationPhase;
import org.IsmaelSS.model.RoundResult;
import org.IsmaelSS.model.StatsData;
import org.IsmaelSS.model.StatsData.OverallStats;
import org.IsmaelSS.model.StatsData.QuestionScore;
import org.IsmaelSS.model.StatsData.ThemeStats;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StatsService {
    private static final Logger LOG = Logger.getLogger(StatsService.class.getName());
    private static final String STATS_FILE = System.getProperty("user.dir")
            + File.separator + "flashcard-stats.json";

    private static final int SCORE_CORRECT_DELTA = 2;
    private static final int SCORE_MAX = 5;
    private static final int SCORE_WRONG_DELTA = -3;
    private static final int SCORE_MIN = -10;

    private final ObjectMapper mapper;
    private StatsData data;

    public StatsService() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.data = load();
    }

    private StatsData load() {
        File file = new File(STATS_FILE);
        if (!file.exists()) {
            return new StatsData();
        }
        try {
            StatsData data = mapper.readValue(file, StatsData.class);
            if (isOldFormat(data)) {
                LOG.info("Detected old stats format — migrating to new format...");
                data = migrateOldFormat(data);
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
            }
            return data;
        } catch (IOException e) {
            LOG.warning("Could not read stats file, starting fresh: " + e.getMessage());
            return new StatsData();
        }
    }

    private boolean isOldFormat(StatsData data) {
        for (StatsData.ThemeStats ts : data.getThemes().values()) {
            for (String key : ts.getQuestions().keySet()) {
                return key.length() > 20 || key.contains(" ");
            }
        }
        return false;
    }

    private StatsData migrateOldFormat(StatsData oldData) {
        StatsData newData = new StatsData();
        for (Map.Entry<String, StatsData.ThemeStats> themeEntry : oldData.getThemes().entrySet()) {
            StatsData.ThemeStats oldTs = themeEntry.getValue();
            StatsData.ThemeStats newTs = new StatsData.ThemeStats();
            newTs.setTotalAnswered(oldTs.getTotalAnswered());
            newTs.setTotalCorrect(oldTs.getTotalCorrect());
            newTs.setQuestions(new HashMap<>());
            newData.getThemes().put(themeEntry.getKey(), newTs);
        }
        newData.getOverall().setTotalAnswered(oldData.getOverall().getTotalAnswered());
        newData.getOverall().setTotalCorrect(oldData.getOverall().getTotalCorrect());
        return newData;
    }

    private void save() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(STATS_FILE), data);
        } catch (IOException e) {
            LOG.severe("Failed to save stats: " + e.getMessage());
        }
    }

    public void recordRound(List<RoundResult> results) {
        for (RoundResult result : results) {
            ThemeStats themeStats = data.getThemes()
                    .computeIfAbsent(result.themeName(), k -> new ThemeStats());
            themeStats.setTotalAnswered(themeStats.getTotalAnswered() + 1);
            if (result.wasCorrect()) {
                themeStats.setTotalCorrect(themeStats.getTotalCorrect() + 1);
            }

            QuestionScore qScore = themeStats.getQuestions()
                    .computeIfAbsent(String.valueOf(result.questionId()), k -> new QuestionScore());
            qScore.updateSM2(result.wasCorrect());
        }
        recalculateOverall();
        save();
    }

    private void recalculateOverall() {
        int totalAnswered = 0;
        int totalCorrect = 0;
        for (ThemeStats ts : data.getThemes().values()) {
            totalAnswered += ts.getTotalAnswered();
            totalCorrect += ts.getTotalCorrect();
        }
        data.getOverall().setTotalAnswered(totalAnswered);
        data.getOverall().setTotalCorrect(totalCorrect);
    }

    public OverallStats getOverallStats() {
        return data.getOverall();
    }

    public ThemeStats getThemeStats(String themeName) {
        return data.getThemes().get(themeName);
    }

    public String getAproveitamento(String themeName) {
        ThemeStats ts = data.getThemes().get(themeName);
        if (ts == null || ts.getQuestions().isEmpty()) return "N/A";
        int weightSum = 0;
        for (QuestionScore qs : ts.getQuestions().values()) {
            if (qs.getRepCount() > 0) weightSum += 1;
            else weightSum -= 1;
        }
        return String.valueOf(weightSum);
    }

    public List<Map.Entry<String, Integer>> getLowestScoreQuestions(int limit) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        for (Map.Entry<String, ThemeStats> themeEntry : data.getThemes().entrySet()) {
            for (Map.Entry<String, QuestionScore> questionEntry :
                    themeEntry.getValue().getQuestions().entrySet()) {
                entries.add(Map.entry(
                        questionEntry.getKey(),
                        questionEntry.getValue().getRepCount()
                ));
            }
        }
        entries.sort(Map.Entry.comparingByValue());
        return entries.subList(0, Math.min(limit, entries.size()));
    }

    /**
     * Returns the percentage of questions in DOMINIO phase for the given theme,
     * as an integer string like "80".
     * DOMINIO = repCount >= 6 && interval > 30.
     * Returns "N/A" if the theme has no data or no questions.
     */
    public String getDominio(String themeName) {
        ThemeStats ts = data.getThemes().get(themeName);
        if (ts == null || ts.getQuestions().isEmpty()) return "N/A";
        long dominioCount = ts.getQuestions().values().stream()
                .filter(qs -> qs.getRepCount() >= 6 && qs.getInterval() > 30)
                .count();
        int percentage = (int) ((dominioCount * 100) / ts.getQuestions().size());
        return String.valueOf(percentage);
    }

    /**
     * Returns up to {@code limit} questions from the specified theme,
     * sorted by repCount ascending (least reviewed first).
     * Returns an empty list if the theme is unknown or has no questions.
     */
    public List<Map.Entry<String, Integer>> getLowestScoreQuestionsByTheme(String themeName, int limit) {
        ThemeStats ts = data.getThemes().get(themeName);
        if (ts == null) return new ArrayList<>();
        List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        for (Map.Entry<String, QuestionScore> questionEntry : ts.getQuestions().entrySet()) {
            entries.add(Map.entry(questionEntry.getKey(), questionEntry.getValue().getRepCount()));
        }
        entries.sort(Map.Entry.comparingByValue());
        return entries.subList(0, Math.min(limit, entries.size()));
    }

    public List<String> getAllThemesWithData() {
        return new ArrayList<>(data.getThemes().keySet());
    }

    // == SM-2 query methods ==

    /**
     * Returns overdue questions for a theme: those with nextReviewTimestamp <= now and > 0.
     * Sorted by repCount ascending.
     */
    public List<Map.Entry<String, QuestionScore>> getDueQuestions(String themeName) {
        ThemeStats ts = data.getThemes().get(themeName);
        if (ts == null) return new ArrayList<>();
        long now = System.currentTimeMillis();
        return ts.getQuestions().entrySet().stream()
                .filter(e -> {
                    long nextReview = e.getValue().getNextReviewTimestamp();
                    return nextReview <= now && nextReview > 0;
                })
                .sorted(Map.Entry.comparingByValue(Comparator.comparingInt(qs -> qs.getRepCount())))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Count of overdue questions for a theme.
     */
    public int getDueCount(String themeName) {
        return getDueQuestions(themeName).size();
    }

    /**
     * Returns unreviewed/new questions for a theme: those with repCount == 0.
     */
    public List<Map.Entry<String, QuestionScore>> getNewQuestions(String themeName) {
        ThemeStats ts = data.getThemes().get(themeName);
        if (ts == null) return new ArrayList<>();
        return ts.getQuestions().entrySet().stream()
                .filter(e -> e.getValue().getRepCount() == 0)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns a map of FixationPhase to count for all questions in a theme.
     */
    public Map<FixationPhase, Integer> getFixationPhases(String themeName) {
        ThemeStats ts = data.getThemes().get(themeName);
        Map<FixationPhase, Integer> dist = new EnumMap<>(FixationPhase.class);
        for (FixationPhase p : FixationPhase.values()) {
            dist.put(p, 0);
        }
        if (ts == null) return dist;
        for (QuestionScore qs : ts.getQuestions().values()) {
            dist.merge(qs.getFixationPhase(), 1, Integer::sum);
        }
        return dist;
    }

    /**
     * Returns all review history aggregated by day. The outer map is a TreeMap in
     * descending order (most recent first). Each entry maps a LocalDate to a Map of
     * theme name → number of questions reviewed that day.
     */
    public Map<LocalDate, Map<String, Integer>> getTimelineData() {
        Map<LocalDate, Map<String, Integer>> timeline = new TreeMap<>(Collections.reverseOrder());
        for (Map.Entry<String, ThemeStats> themeEntry : data.getThemes().entrySet()) {
            for (QuestionScore qs : themeEntry.getValue().getQuestions().values()) {
                if (qs.getLastReviewTimestamp() > 0) {
                    LocalDate day = Instant.ofEpochMilli(qs.getLastReviewTimestamp())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    timeline.computeIfAbsent(day, k -> new HashMap<>())
                            .merge(themeEntry.getKey(), 1, Integer::sum);
                }
            }
        }
        return timeline;
    }

    /**
     * Returns questions due in the next N days, grouped by theme.
     * Each entry maps theme name to a list of (questionId, daysUntilDue).
     */
    public Map<String, List<Map.Entry<String, Integer>>> getUpcomingReviews(int days) {
        Map<String, List<Map.Entry<String, Integer>>> upcoming = new TreeMap<>();
        long now = System.currentTimeMillis();
        long end = now + (long) days * 86_400_000L;
        for (Map.Entry<String, ThemeStats> themeEntry : data.getThemes().entrySet()) {
            for (Map.Entry<String, QuestionScore> qe : themeEntry.getValue().getQuestions().entrySet()) {
                long nrt = qe.getValue().getNextReviewTimestamp();
                if (nrt > now && nrt <= end) {
                    int daysUntil = (int) ((nrt - now) / 86_400_000L) + 1;
                    upcoming.computeIfAbsent(themeEntry.getKey(), k -> new ArrayList<>())
                            .add(Map.entry(qe.getKey(), daysUntil));
                }
            }
        }
        return upcoming;
    }

    /**
     * Simulates a perfect SM-2 answer for all overdue questions in a theme.
     * Only affects questions where nextReviewTimestamp <= now and > 0.
     */
    public void markThemeAsDone(String themeName) {
        ThemeStats ts = data.getThemes().get(themeName);
        if (ts == null) return;
        long now = System.currentTimeMillis();
        for (QuestionScore qs : ts.getQuestions().values()) {
            long nrt = qs.getNextReviewTimestamp();
            if (nrt <= now && nrt > 0) {
                qs.updateSM2(true);
            }
        }
        save();
    }
}
