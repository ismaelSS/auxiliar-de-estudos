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

    private Question createQ(String id, String text, String... options) {
        return new Question(id, text, Arrays.asList(options), 0);
    }

    private Theme createTheme(String name, Question... questions) {
        return new Theme(name, Arrays.asList(questions));
    }

    private List<String> collectIds(RoundState state) {
        List<String> ids = new ArrayList<>();
        while (!state.isComplete()) {
            ids.add(state.getCurrentQuestion().getId());
            state.advanceToNext();
        }
        return ids;
    }

    @Test
    void createReinforcementRoundWithEmptyStatsReturnsCorrectCount() {
        Theme t1 = createTheme("t1",
                createQ("t1q1", "text1", "a", "b", "c", "d"),
                createQ("t1q2", "text2", "a", "b", "c", "d"),
                createQ("t1q3", "text3", "a", "b", "c", "d"),
                createQ("t1q4", "text4", "a", "b", "c", "d"),
                createQ("t1q5", "text5", "a", "b", "c", "d")
        );
        StatsService service = new StatsService();

        RoundState state = RoundState.createReinforcementRound(Arrays.asList(t1), 3, service);

        assertEquals(3, state.getSelectedQuestionsCount());
        assertFalse(state.isComplete());
    }

    @Test
    void createReinforcementRoundPrioritizesLowestScoreQuestions() {
        StatsService service = new StatsService();
        service.recordRound(Arrays.asList(
                new RoundResult("t1", "text1", "t1q1", false),
                new RoundResult("t1", "text1", "t1q1", false),
                new RoundResult("t1", "text2", "t1q2", false),
                new RoundResult("t1", "text2", "t1q2", true),
                new RoundResult("t1", "text3", "t1q3", true)
        ));

        Theme t1 = createTheme("t1",
                createQ("t1q1", "text1", "a", "b", "c", "d"),
                createQ("t1q2", "text2", "a", "b", "c", "d"),
                createQ("t1q3", "text3", "a", "b", "c", "d"),
                createQ("t1q4", "text4", "a", "b", "c", "d"),
                createQ("t1q5", "text5", "a", "b", "c", "d")
        );

        RoundState state = RoundState.createReinforcementRound(Arrays.asList(t1), 2, service);

        List<String> ids = collectIds(state);
        assertEquals(2, ids.size());
        assertTrue(ids.contains("t1q1"));
        assertTrue(ids.contains("t1q2"));
    }

    @Test
    void createReinforcementRoundFillsRemainingWithFreshWhenFewErrors() {
        StatsService service = new StatsService();
        service.recordRound(Arrays.asList(
                new RoundResult("t1", "text1", "t1q1", false)
        ));

        Theme t1 = createTheme("t1",
                createQ("t1q1", "text1", "a", "b", "c", "d"),
                createQ("t1q2", "text2", "a", "b", "c", "d"),
                createQ("t1q3", "text3", "a", "b", "c", "d"),
                createQ("t1q4", "text4", "a", "b", "c", "d"),
                createQ("t1q5", "text5", "a", "b", "c", "d")
        );

        RoundState state = RoundState.createReinforcementRound(Arrays.asList(t1), 4, service);

        List<String> ids = collectIds(state);
        assertEquals(4, ids.size());
        assertTrue(ids.contains("t1q1"));
        long freshCount = ids.stream().filter(id -> !id.equals("t1q1")).count();
        assertEquals(3, freshCount);
    }

    @Test
    void createReinforcementRoundRespectsQuestionCountPerTheme() {
        StatsService service = new StatsService();
        Theme t1 = createTheme("t1",
                createQ("q1", "text1", "a", "b", "c", "d"),
                createQ("q2", "text2", "a", "b", "c", "d"),
                createQ("q3", "text3", "a", "b", "c", "d"),
                createQ("q4", "text4", "a", "b", "c", "d"),
                createQ("q5", "text5", "a", "b", "c", "d"),
                createQ("q6", "text6", "a", "b", "c", "d"),
                createQ("q7", "text7", "a", "b", "c", "d"),
                createQ("q8", "text8", "a", "b", "c", "d"),
                createQ("q9", "text9", "a", "b", "c", "d"),
                createQ("q10", "text10", "a", "b", "c", "d")
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
                createQ("id_a", "text_a", "a1", "b1", "c1"),
                createQ("id_b", "text_b", "a2", "b2", "c2"),
                createQ("id_c", "text_c", "a3", "b3", "c3"),
                createQ("id_d", "text_d", "a4", "b4", "c4"),
                createQ("id_e", "text_e", "a5", "b5", "c5")
        );

        RoundState state = new RoundState(Arrays.asList(t1), 3);
        assertEquals(3, state.getSelectedQuestionsCount());
        assertFalse(state.isComplete());
    }

    @Test
    void normalConstructorDoesNotUseStatsService() {
        Theme t1 = createTheme("t1",
                createQ("only_q", "text only", "a", "b", "c", "d")
        );
        RoundState state = new RoundState(Arrays.asList(t1), 1);
        assertEquals(1, state.getSelectedQuestionsCount());
        assertFalse(state.isComplete());
        assertEquals("only_q", state.getCurrentQuestion().getId());
    }

    @Test
    void createReinforcementRoundMultipleThemesAndCrossThemeLowScores() {
        StatsService service = new StatsService();
        service.recordRound(Arrays.asList(
                new RoundResult("math", "m1 text", "m1", false),
                new RoundResult("math", "m1 text", "m1", false),
                new RoundResult("math", "m2 text", "m2", false),
                new RoundResult("hist", "h1 text", "h1", false)
        ));

        Theme math = createTheme("math",
                createQ("m1", "m1 text", "a", "b", "c", "d"),
                createQ("m2", "m2 text", "a", "b", "c", "d"),
                createQ("m3", "m3 text", "a", "b", "c", "d")
        );
        Theme hist = createTheme("hist",
                createQ("h1", "h1 text", "a", "b", "c", "d"),
                createQ("h2", "h2 text", "a", "b", "c", "d"),
                createQ("h3", "h3 text", "a", "b", "c", "d")
        );

        RoundState state = RoundState.createReinforcementRound(Arrays.asList(math, hist), 2, service);

        List<String> ids = collectIds(state);
        assertEquals(4, ids.size());
        assertTrue(ids.contains("m1"));
        assertTrue(ids.contains("m2"));
        assertTrue(ids.contains("h1"));
    }
}
