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
        private Map<String, QuestionScore> questions = new HashMap<>();

        public ThemeStats() {}

        public int getTotalAnswered() { return totalAnswered; }
        public void setTotalAnswered(int totalAnswered) { this.totalAnswered = totalAnswered; }
        public int getTotalCorrect() { return totalCorrect; }
        public void setTotalCorrect(int totalCorrect) { this.totalCorrect = totalCorrect; }
        public Map<String, QuestionScore> getQuestions() { return questions; }
        public void setQuestions(Map<String, QuestionScore> questions) { this.questions = questions; }
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuestionScore {
        private int score;  // bounded -10..+5, defaults to 0

        public QuestionScore() {}

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }

        /**
         * Correct answer: +2, capped at +5.
         * @return the new score (for testing convenience)
         */
        public int recordCorrect() {
            this.score = Math.min(5, this.score + 2);
            return this.score;
        }

        /**
         * Wrong answer: -3, floored at -10.
         * @return the new score (for testing convenience)
         */
        public int recordWrong() {
            this.score = Math.max(-10, this.score - 3);
            return this.score;
        }
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
