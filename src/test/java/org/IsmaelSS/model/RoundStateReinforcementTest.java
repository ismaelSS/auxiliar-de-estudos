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

    private Question createQ(int id, String text, String... options) {
        return new Question(id, text, Arrays.asList(options), 0);
    }

    private Theme createTheme(String name, Question... questions) {
        return new Theme(name, Arrays.asList(questions));
    }

    private List<Integer> collectIds(RoundState state) {
        List<Integer> ids = new ArrayList<>();
        while (!state.isComplete()) {
            ids.add(state.getCurrentQuestion().getId());
            state.advanceToNext();
        }
        return ids;
    }

    @Test
    void createReinforcementRoundWithEmptyStatsReturnsCorrectCount() {
        Theme t1 = createTheme("t1",
                createQ(0, "text1", "a", "b", "c", "d"),
                createQ(1, "text2", "a", "b", "c", "d"),
                createQ(2, "text3", "a", "b", "c", "d"),
                createQ(3, "text4", "a", "b", "c", "d"),
                createQ(4, "text5", "a", "b", "c", "d")
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
                new RoundResult("t1", "text1", 0, false),
                new RoundResult("t1", "text1", 0, false),
                new RoundResult("t1", "text2", 1, false),
                new RoundResult("t1", "text2", 1, true),
                new RoundResult("t1", "text3", 2, true)
        ));

        Theme t1 = createTheme("t1",
                createQ(0, "text1", "a", "b", "c", "d"),
                createQ(1, "text2", "a", "b", "c", "d"),
                createQ(2, "text3", "a", "b", "c", "d"),
                createQ(3, "text4", "a", "b", "c", "d"),
                createQ(4, "text5", "a", "b", "c", "d")
        );

        RoundState state = RoundState.createReinforcementRound(Arrays.asList(t1), 2, service);

        List<Integer> ids = collectIds(state);
        assertEquals(2, ids.size());
        assertTrue(ids.contains(0));
        assertTrue(ids.contains(1));
    }

    @Test
    void createReinforcementRoundFillsRemainingWithFreshWhenFewErrors() {
        StatsService service = new StatsService();
        service.recordRound(Arrays.asList(
                new RoundResult("t1", "text1", 0, false)
        ));

        Theme t1 = createTheme("t1",
                createQ(0, "text1", "a", "b", "c", "d"),
                createQ(1, "text2", "a", "b", "c", "d"),
                createQ(2, "text3", "a", "b", "c", "d"),
                createQ(3, "text4", "a", "b", "c", "d"),
                createQ(4, "text5", "a", "b", "c", "d")
        );

        RoundState state = RoundState.createReinforcementRound(Arrays.asList(t1), 4, service);

        List<Integer> ids = collectIds(state);
        assertEquals(4, ids.size());
        assertTrue(ids.contains(0));
        long freshCount = ids.stream().filter(id -> id != 0).count();
        assertEquals(3, freshCount);
    }

    @Test
    void createReinforcementRoundRespectsQuestionCountPerTheme() {
        StatsService service = new StatsService();
        Theme t1 = createTheme("t1",
                createQ(0, "text1", "a", "b", "c", "d"),
                createQ(1, "text2", "a", "b", "c", "d"),
                createQ(2, "text3", "a", "b", "c", "d"),
                createQ(3, "text4", "a", "b", "c", "d"),
                createQ(4, "text5", "a", "b", "c", "d"),
                createQ(5, "text6", "a", "b", "c", "d"),
                createQ(6, "text7", "a", "b", "c", "d"),
                createQ(7, "text8", "a", "b", "c", "d"),
                createQ(8, "text9", "a", "b", "c", "d"),
                createQ(9, "text10", "a", "b", "c", "d")
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
                createQ(0, "text_a", "a1", "b1", "c1"),
                createQ(1, "text_b", "a2", "b2", "c2"),
                createQ(2, "text_c", "a3", "b3", "c3"),
                createQ(3, "text_d", "a4", "b4", "c4"),
                createQ(4, "text_e", "a5", "b5", "c5")
        );

        RoundState state = new RoundState(Arrays.asList(t1), 3);
        assertEquals(3, state.getSelectedQuestionsCount());
        assertFalse(state.isComplete());
    }

    @Test
    void normalConstructorDoesNotUseStatsService() {
        Theme t1 = createTheme("t1",
                createQ(0, "text only", "a", "b", "c", "d")
        );
        RoundState state = new RoundState(Arrays.asList(t1), 1);
        assertEquals(1, state.getSelectedQuestionsCount());
        assertFalse(state.isComplete());
        assertEquals(0, state.getCurrentQuestion().getId());
    }

    @Test
    void createReinforcementRoundMultipleThemesAndCrossThemeLowScores() {
        StatsService service = new StatsService();
        service.recordRound(Arrays.asList(
                new RoundResult("math", "m1 text", 0, false),
                new RoundResult("math", "m1 text", 0, false),
                new RoundResult("math", "m2 text", 1, false),
                new RoundResult("hist", "h1 text", 3, false)
        ));

        Theme math = createTheme("math",
                createQ(0, "m1 text", "a", "b", "c", "d"),
                createQ(1, "m2 text", "a", "b", "c", "d"),
                createQ(2, "m3 text", "a", "b", "c", "d")
        );
        Theme hist = createTheme("hist",
                createQ(3, "h1 text", "a", "b", "c", "d"),
                createQ(4, "h2 text", "a", "b", "c", "d"),
                createQ(5, "h3 text", "a", "b", "c", "d")
        );

        RoundState state = RoundState.createReinforcementRound(Arrays.asList(math, hist), 2, service);

        List<Integer> ids = collectIds(state);
        assertEquals(4, ids.size());
        assertTrue(ids.contains(0));
        assertTrue(ids.contains(1));
        assertTrue(ids.contains(3));
    }
}
