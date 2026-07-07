package org.IsmaelSS.model;

import java.util.HashMap;
import java.util.Map;

public class StatsData {
    private Map<String, ThemeStats> themes = new HashMap<>();
    private OverallStats overall = new OverallStats();

    public StatsData() {}

    public Map<String, ThemeStats> getThemes() { return themes; }
    public void setThemes(Map<String, ThemeStats> themes) { this.themes = themes; }
    public OverallStats getOverall() { return overall; }
    public void setOverall(OverallStats overall) { this.overall = overall; }

    public static class ThemeStats {
        private int totalAnswered;
        private int totalCorrect;
        private Map<String, QuestionStats> questions = new HashMap<>();

        public ThemeStats() {}

        public int getTotalAnswered() { return totalAnswered; }
        public void setTotalAnswered(int totalAnswered) { this.totalAnswered = totalAnswered; }
        public int getTotalCorrect() { return totalCorrect; }
        public void setTotalCorrect(int totalCorrect) { this.totalCorrect = totalCorrect; }
        public Map<String, QuestionStats> getQuestions() { return questions; }
        public void setQuestions(Map<String, QuestionStats> questions) { this.questions = questions; }
    }

    public static class QuestionStats {
        private int answered;
        private int correct;

        public QuestionStats() {}

        public int getAnswered() { return answered; }
        public void setAnswered(int answered) { this.answered = answered; }
        public int getCorrect() { return correct; }
        public void setCorrect(int correct) { this.correct = correct; }
    }

    public static class OverallStats {
        private int totalAnswered;
        private int totalCorrect;

        public OverallStats() {}

        public int getTotalAnswered() { return totalAnswered; }
        public void setTotalAnswered(int totalAnswered) { this.totalAnswered = totalAnswered; }
        public int getTotalCorrect() { return totalCorrect; }
        public void setTotalCorrect(int totalCorrect) { this.totalCorrect = totalCorrect; }
    }
}
