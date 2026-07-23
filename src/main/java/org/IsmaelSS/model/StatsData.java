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

        // SM-2 spaced repetition fields
        private double easeFactor = 2.5;  // default 2.5 per locked decision
        private int interval;              // days, default 0
        private int repCount;              // consecutive correct, default 0
        private long lastReviewTimestamp;  // millis since epoch, default 0
        private long nextReviewTimestamp;  // millis since epoch, default 0

        public QuestionScore() {}

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }

        public double getEaseFactor() { return easeFactor; }
        public void setEaseFactor(double easeFactor) { this.easeFactor = easeFactor; }

        public int getInterval() { return interval; }
        public void setInterval(int interval) { this.interval = interval; }

        public int getRepCount() { return repCount; }
        public void setRepCount(int repCount) { this.repCount = repCount; }

        public long getLastReviewTimestamp() { return lastReviewTimestamp; }
        public void setLastReviewTimestamp(long lastReviewTimestamp) { this.lastReviewTimestamp = lastReviewTimestamp; }

        public long getNextReviewTimestamp() { return nextReviewTimestamp; }
        public void setNextReviewTimestamp(long nextReviewTimestamp) { this.nextReviewTimestamp = nextReviewTimestamp; }

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

        /**
         * SM-2 binary update: correct→4 equivalent, wrong→1 equivalent.
         * Sets easeFactor, interval, repCount, and timestamps per SM-2 algorithm.
         * @param correct true if answer was correct, false if wrong
         */
        public void updateSM2(boolean correct) {
            if (correct) {
                this.easeFactor = Math.min(2.5, this.easeFactor + 0.1);
                if (this.repCount == 0) {
                    this.interval = 1;
                } else {
                    this.interval = (int) Math.ceil(this.interval * this.easeFactor);
                }
                this.repCount++;
            } else {
                this.easeFactor = Math.max(1.3, this.easeFactor - 0.2);
            }
            this.lastReviewTimestamp = System.currentTimeMillis();
            if (this.interval > 0) {
                this.nextReviewTimestamp = System.currentTimeMillis() + (long) this.interval * 86_400_000L;
            }
        }

        /**
         * Derives the FixationPhase from SM-2 fields.
         * @return the fixation phase based on repCount and interval thresholds
         */
        public FixationPhase getFixationPhase() {
            if (repCount == 0 || interval <= 1) return FixationPhase.APRENDENDO;
            if (repCount <= 2 && interval <= 7) return FixationPhase.REVISAO;
            if (repCount <= 5 && interval <= 30) return FixationPhase.FIXA;
            return FixationPhase.DOMINIO;
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
