package org.IsmaelSS.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.IsmaelSS.model.RoundResult;
import org.IsmaelSS.model.StatsData;
import org.IsmaelSS.model.StatsData.OverallStats;
import org.IsmaelSS.model.StatsData.QuestionScore;
import org.IsmaelSS.model.StatsData.ThemeStats;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
                    .computeIfAbsent(result.questionId(), k -> new QuestionScore());
            if (result.wasCorrect()) {
                qScore.recordCorrect();
            } else {
                qScore.recordWrong();
            }
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
            int score = qs.getScore();
            if (score < 0) weightSum += -3;
            else if (score > 0) weightSum += 2;
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
                        questionEntry.getValue().getScore()
                ));
            }
        }
        entries.sort(Map.Entry.comparingByValue());
        return entries.subList(0, Math.min(limit, entries.size()));
    }

    public List<String> getAllThemesWithData() {
        return new ArrayList<>(data.getThemes().keySet());
    }
}
