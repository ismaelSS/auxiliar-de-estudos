package org.IsmaelSS.model;

import org.IsmaelSS.service.StatsService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RoundState {
    private final List<RoundQuestion> roundQuestions;
    private int currentIndex;
    private int correctCount;
    private int totalAnswered;

    public RoundState(List<Theme> themes, int questionsPerTheme) {
        this.roundQuestions = buildQuestions(themes, questionsPerTheme);
        this.currentIndex = 0;
        this.correctCount = 0;
        this.totalAnswered = 0;
    }

    private RoundState(List<RoundQuestion> questions, boolean dummy) {
        this.roundQuestions = questions;
        this.currentIndex = 0;
        this.correctCount = 0;
        this.totalAnswered = 0;
    }

    private static List<RoundQuestion> buildQuestions(List<Theme> themes, int questionsPerTheme) {
        List<RoundQuestion> questions = new ArrayList<>();
        for (Theme theme : themes) {
            List<Question> themeQuestions = theme.getQuestions();
            int take = Math.min(questionsPerTheme, themeQuestions.size());
            for (int i = 0; i < take; i++) {
                Question q = themeQuestions.get(i);
                List<String> original = q.getOptions();
                String correctText = original.get(q.getCorrect());
                List<String> shuffled = new ArrayList<>(original);
                Collections.shuffle(shuffled);
                int newCorrect = shuffled.indexOf(correctText);
                questions.add(new RoundQuestion(theme.getName(), q, shuffled, newCorrect));
            }
        }
        Collections.shuffle(questions);
        return questions;
    }

    public static RoundState createReinforcementRound(List<Theme> themes, int questionsPerTheme, StatsService statsService) {
        List<Map.Entry<String, Double>> topErrors = statsService.getHighestErrorQuestions(50);
        Set<String> errorTexts = topErrors.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        Map<String, Double> errorMap = new HashMap<>();
        for (Map.Entry<String, Double> e : topErrors) {
            errorMap.put(e.getKey(), e.getValue());
        }

        List<RoundQuestion> questions = new ArrayList<>();
        for (Theme theme : themes) {
            List<Question> themeQuestions = new ArrayList<>(theme.getQuestions());
            List<Question> errorQuestions = new ArrayList<>();
            List<Question> freshQuestions = new ArrayList<>();

            for (Question q : themeQuestions) {
                if (errorTexts.contains(q.getQuestion())) {
                    errorQuestions.add(q);
                } else {
                    freshQuestions.add(q);
                }
            }

            errorQuestions.sort((a, b) -> Double.compare(
                    errorMap.getOrDefault(b.getQuestion(), 0.0),
                    errorMap.getOrDefault(a.getQuestion(), 0.0)
            ));

            int take = Math.min(questionsPerTheme, themeQuestions.size());
            List<Question> selected = new ArrayList<>();
            for (Question q : errorQuestions) {
                if (selected.size() >= take) break;
                selected.add(q);
            }
            Collections.shuffle(freshQuestions);
            for (Question q : freshQuestions) {
                if (selected.size() >= take) break;
                selected.add(q);
            }

            for (Question q : selected) {
                List<String> original = q.getOptions();
                String correctText = original.get(q.getCorrect());
                List<String> shuffled = new ArrayList<>(original);
                Collections.shuffle(shuffled);
                int newCorrect = shuffled.indexOf(correctText);
                questions.add(new RoundQuestion(theme.getName(), q, shuffled, newCorrect));
            }
        }
        Collections.shuffle(questions);
        return new RoundState(questions, true);
    }

    public boolean isComplete() {
        return currentIndex >= roundQuestions.size();
    }

    public Question getCurrentQuestion() {
        return roundQuestions.get(currentIndex).question;
    }

    public List<String> getCurrentOptions() {
        return roundQuestions.get(currentIndex).shuffledOptions;
    }

    public int getCurrentCorrectIndex() {
        return roundQuestions.get(currentIndex).correctIndex;
    }

    public boolean checkAnswer(int selectedIndex) {
        boolean correct = selectedIndex == getCurrentCorrectIndex();
        if (correct) correctCount++;
        totalAnswered++;
        return correct;
    }

    public void advanceToNext() {
        currentIndex++;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getSelectedQuestionsCount() {
        return roundQuestions.size();
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getTotalAnswered() {
        return totalAnswered;
    }

    public String getCurrentThemeName() {
        return roundQuestions.get(currentIndex).themeName;
    }

    private static class RoundQuestion {
        final String themeName;
        final Question question;
        final List<String> shuffledOptions;
        final int correctIndex;

        RoundQuestion(String themeName, Question question, List<String> shuffledOptions, int correctIndex) {
            this.themeName = themeName;
            this.question = question;
            this.shuffledOptions = shuffledOptions;
            this.correctIndex = correctIndex;
        }
    }
}
