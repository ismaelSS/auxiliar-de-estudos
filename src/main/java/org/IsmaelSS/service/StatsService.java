package org.IsmaelSS.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.IsmaelSS.model.RoundResult;
import org.IsmaelSS.model.StatsData;
import org.IsmaelSS.model.StatsData.OverallStats;
import org.IsmaelSS.model.StatsData.QuestionStats;
import org.IsmaelSS.model.StatsData.ThemeStats;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class StatsService {
    private static final Logger LOG = Logger.getLogger(StatsService.class.getName());
    private static final String STATS_FILE = System.getProperty("user.dir")
            + File.separator + "flashcard-stats.json";

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
            return mapper.readValue(file, StatsData.class);
        } catch (IOException e) {
            LOG.warning("Could not read stats file, starting fresh: " + e.getMessage());
            return new StatsData();
        }
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
            QuestionStats qStats = themeStats.getQuestions()
                    .computeIfAbsent(result.questionText(), k -> new QuestionStats());
            qStats.setAnswered(qStats.getAnswered() + 1);
            if (result.wasCorrect()) {
                qStats.setCorrect(qStats.getCorrect() + 1);
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

    public String getHitRate(String themeName) {
        ThemeStats ts = data.getThemes().get(themeName);
        if (ts == null || ts.getTotalAnswered() == 0) return "N/A";
        double rate = (double) ts.getTotalCorrect() / ts.getTotalAnswered() * 100;
        return String.format("%.0f%%", rate);
    }

    public String getOverallHitRate() {
        OverallStats o = data.getOverall();
        if (o.getTotalAnswered() == 0) return "N/A";
        double rate = (double) o.getTotalCorrect() / o.getTotalAnswered() * 100;
        return String.format("%.0f%%", rate);
    }

    public OverallStats getOverallStats() {
        return data.getOverall();
    }

    public ThemeStats getThemeStats(String themeName) {
        return data.getThemes().get(themeName);
    }

    public List<Map.Entry<String, Double>> getHighestErrorQuestions(int limit) {
        List<Map.Entry<String, Double>> entries = new ArrayList<>();
        for (Map.Entry<String, ThemeStats> themeEntry : data.getThemes().entrySet()) {
            for (Map.Entry<String, QuestionStats> questionEntry : themeEntry.getValue().getQuestions().entrySet()) {
                QuestionStats qs = questionEntry.getValue();
                if (qs.getAnswered() == 0) continue;
                double errorRate = 1.0 - ((double) qs.getCorrect() / qs.getAnswered());
                entries.add(Map.entry(questionEntry.getKey(), errorRate));
            }
        }
        entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        return entries.subList(0, Math.min(limit, entries.size()));
    }

    public List<String> getAllThemesWithData() {
        return new ArrayList<>(data.getThemes().keySet());
    }
}
