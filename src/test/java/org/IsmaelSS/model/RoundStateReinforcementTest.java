package org.IsmaelSS.model;

import org.IsmaelSS.service.StatsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoundStateReinforcementTest {

    private static final String STATS_FILE = System.getProperty("user.dir")
            + File.separator + "flashcard-stats.json";

    @BeforeEach
    void cleanUp() {
        new File(STATS_FILE).delete();
    }

    @AfterEach
    void tearDown() {
        new File(STATS_FILE).delete();
    }

    private Question createQ(String text, String... options) {
        return new Question(text, Arrays.asList(options), 0);
    }

    private Theme createTheme(String name, Question... questions) {
        return new Theme(name, Arrays.asList(questions));
    }

    private List<String> collectTexts(RoundState state) {
        List<String> texts = new ArrayList<>();
        while (!state.isComplete()) {
            texts.add(state.getCurrentQuestion().getQuestion());
            state.advanceToNext();
        }
        return texts;
    }

    @Test
    void createReinforcementRoundWithEmptyStatsReturnsCorrectCount() {
        Theme t1 = createTheme("t1",
                createQ("t1q1", "a1", "b1", "c1", "d1"),
                createQ("t1q2", "a2", "b2", "c2", "d2"),
                createQ("t1q3", "a3", "b3", "c3", "d3"),
                createQ("t1q4", "a4", "b4", "c4", "d4"),
                createQ("t1q5", "a5", "b5", "c5", "d5")
        );
        StatsService service = new StatsService();

        RoundState state = RoundState.createReinforcementRound(Arrays.asList(t1), 3, service);

        assertEquals(3, state.getSelectedQuestionsCount());
        assertFalse(state.isComplete());
    }

    @Test
    void createReinforcementRoundPrioritizesHighErrorQuestions() {
        StatsService service = new StatsService();
        service.recordRound(Arrays.asList(
                new RoundResult("t1", "t1q1", false),
                new RoundResult("t1", "t1q1", false),
                new RoundResult("t1", "t1q2", false),
                new RoundResult("t1", "t1q2", true),
                new RoundResult("t1", "t1q3", true)
        ));

        Theme t1 = createTheme("t1",
                createQ("t1q1", "a", "b", "c", "d"),
                createQ("t1q2", "a", "b", "c", "d"),
                createQ("t1q3", "a", "b", "c", "d"),
                createQ("t1q4", "a", "b", "c", "d"),
                createQ("t1q5", "a", "b", "c", "d")
        );

        RoundState state = RoundState.createReinforcementRound(Arrays.asList(t1), 2, service);

        List<String> texts = collectTexts(state);
        assertEquals(2, texts.size());
        assertTrue(texts.contains("t1q1"));
        assertTrue(texts.contains("t1q2"));
    }

    @Test
    void createReinforcementRoundFillsRemainingWithFreshWhenFewErrors() {
        StatsService service = new StatsService();
        service.recordRound(Arrays.asList(
                new RoundResult("t1", "t1q1", false)
        ));

        Theme t1 = createTheme("t1",
                createQ("t1q1", "a", "b", "c", "d"),
                createQ("t1q2", "a", "b", "c", "d"),
                createQ("t1q3", "a", "b", "c", "d"),
                createQ("t1q4", "a", "b", "c", "d"),
                createQ("t1q5", "a", "b", "c", "d")
        );

        RoundState state = RoundState.createReinforcementRound(Arrays.asList(t1), 4, service);

        List<String> texts = collectTexts(state);
        assertEquals(4, texts.size());
        assertTrue(texts.contains("t1q1"));
        long freshCount = texts.stream().filter(t -> !t.equals("t1q1")).count();
        assertEquals(3, freshCount);
    }

    @Test
    void createReinforcementRoundRespectsQuestionCountPerTheme() {
        StatsService service = new StatsService();
        Theme t1 = createTheme("t1",
                createQ("t1q1", "a", "b", "c", "d"),
                createQ("t1q2", "a", "b", "c", "d"),
                createQ("t1q3", "a", "b", "c", "d"),
                createQ("t1q4", "a", "b", "c", "d"),
                createQ("t1q5", "a", "b", "c", "d"),
                createQ("t1q6", "a", "b", "c", "d"),
                createQ("t1q7", "a", "b", "c", "d"),
                createQ("t1q8", "a", "b", "c", "d"),
                createQ("t1q9", "a", "b", "c", "d"),
                createQ("t1q10", "a", "b", "c", "d")
        );

        RoundState stateLow = RoundState.createReinforcementRound(Arrays.asList(t1), 3, service);
        assertEquals(3, stateLow.getSelectedQuestionsCount());

        RoundState stateHigh = RoundState.createReinforcementRound(Arrays.asList(t1), 7, service);
        assertEquals(7, stateHigh.getSelectedQuestionsCount());

        RoundState stateCap = RoundState.createReinforcementRound(Arrays.asList(t1), 20, service);
        assertEquals(10, stateCap.getSelectedQuestionsCount());
    }

    @Test
    void normalConstructorWorksBackwardCompatible() {
        Theme t1 = createTheme("t1",
                createQ("q_a", "a1", "b1", "c1"),
                createQ("q_b", "a2", "b2", "c2"),
                createQ("q_c", "a3", "b3", "c3"),
                createQ("q_d", "a4", "b4", "c4"),
                createQ("q_e", "a5", "b5", "c5")
        );

        RoundState state = new RoundState(Arrays.asList(t1), 3);

        assertEquals(3, state.getSelectedQuestionsCount());
        assertFalse(state.isComplete());
    }

    @Test
    void normalConstructorDoesNotUseStatsService() {
        Theme t1 = createTheme("t1",
                createQ("only_q", "a", "b", "c", "d")
        );

        RoundState state = new RoundState(Arrays.asList(t1), 1);

        assertEquals(1, state.getSelectedQuestionsCount());
        assertFalse(state.isComplete());
        assertEquals("only_q", state.getCurrentQuestion().getQuestion());
    }

    @Test
    void createReinforcementRoundMultipleThemesAndCrossThemeErrors() {
        StatsService service = new StatsService();
        service.recordRound(Arrays.asList(
                new RoundResult("math", "m1", false),
                new RoundResult("math", "m1", false),
                new RoundResult("math", "m2", false),
                new RoundResult("hist", "h1", false)
        ));

        Theme math = createTheme("math",
                createQ("m1", "a", "b", "c", "d"),
                createQ("m2", "a", "b", "c", "d"),
                createQ("m3", "a", "b", "c", "d")
        );
        Theme hist = createTheme("hist",
                createQ("h1", "a", "b", "c", "d"),
                createQ("h2", "a", "b", "c", "d"),
                createQ("h3", "a", "b", "c", "d")
        );

        RoundState state = RoundState.createReinforcementRound(Arrays.asList(math, hist), 2, service);

        List<String> texts = collectTexts(state);
        assertEquals(4, texts.size());
        assertTrue(texts.contains("m1"));
        assertTrue(texts.contains("m2"));
        assertTrue(texts.contains("h1"));
    }
}
