package org.IsmaelSS.model;

import org.IsmaelSS.service.StatsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoundStateCustomStudyTest {

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

    private List<Integer> collectIds(RoundState state) {
        List<Integer> ids = new ArrayList<>();
        while (!state.isComplete()) {
            ids.add(state.getCurrentQuestion().getId());
            state.advanceToNext();
        }
        return ids;
    }

    private List<String> collectThemeNames(RoundState state) {
        List<String> names = new ArrayList<>();
        while (!state.isComplete()) {
            names.add(state.getCurrentThemeName());
            state.advanceToNext();
        }
        return names;
    }

    @Test
    void overloadedDueReviewRespectsLimit() {
        StatsService service = new StatsService();
        Theme t1 = new Theme("t1", Arrays.asList(
                createQ(0, "q0", "a", "b", "c", "d"),
                createQ(1, "q1", "a", "b", "c", "d"),
                createQ(2, "q2", "a", "b", "c", "d"),
                createQ(3, "q3", "a", "b", "c", "d"),
                createQ(4, "q4", "a", "b", "c", "d")
        ));

        service.recordRound(Arrays.asList(
                new RoundResult("t1", "q0", 0, true),
                new RoundResult("t1", "q1", 1, true),
                new RoundResult("t1", "q2", 2, true),
                new RoundResult("t1", "q3", 3, true),
                new RoundResult("t1", "q4", 4, true)
        ));

        // All questions have future nextReviewTimestamp → fallback to all questions
        RoundState state = RoundState.createDueReviewRound(t1, service, 3);

        List<Integer> ids = collectIds(state);
        assertEquals(3, ids.size(), "should respect maxQuestions limit");
    }

    @Test
    void overloadedDueReviewLimitExceedsAvailable() {
        StatsService service = new StatsService();
        Theme t1 = new Theme("t1", Arrays.asList(
                createQ(0, "q0", "a", "b", "c", "d"),
                createQ(1, "q1", "a", "b", "c", "d")
        ));

        RoundState state = RoundState.createDueReviewRound(t1, service, 10);

        List<Integer> ids = collectIds(state);
        assertEquals(2, ids.size(), "should return all available when limit exceeds");
    }

    @Test
    void mixedThemes() {
        Theme t1 = new Theme("t1", Arrays.asList(
                createQ(0, "q0", "a", "b", "c", "d"),
                createQ(1, "q1", "a", "b", "c", "d"),
                createQ(2, "q2", "a", "b", "c", "d")
        ));
        Theme t2 = new Theme("t2", Arrays.asList(
                createQ(3, "q3", "a", "b", "c", "d"),
                createQ(4, "q4", "a", "b", "c", "d"),
                createQ(5, "q5", "a", "b", "c", "d")
        ));

        RoundState state = RoundState.createCustomStudyRound(Arrays.asList(t1, t2), 2, new StatsService());

        List<Integer> ids = collectIds(state);
        assertEquals(4, ids.size(), "should have 2 from t1 + 2 from t2");
    }

    @Test
    void limitRespected() {
        Theme t1 = new Theme("t1", Arrays.asList(
                createQ(0, "q0", "a", "b", "c", "d"),
                createQ(1, "q1", "a", "b", "c", "d"),
                createQ(2, "q2", "a", "b", "c", "d"),
                createQ(3, "q3", "a", "b", "c", "d"),
                createQ(4, "q4", "a", "b", "c", "d")
        ));

        RoundState state = RoundState.createCustomStudyRound(Arrays.asList(t1), 3, new StatsService());

        List<Integer> ids = collectIds(state);
        assertEquals(3, ids.size(), "should limit to 3 per theme");
    }

    @Test
    void limitExceedsAvailable() {
        Theme t1 = new Theme("t1", Arrays.asList(
                createQ(0, "q0", "a", "b", "c", "d"),
                createQ(1, "q1", "a", "b", "c", "d")
        ));

        RoundState state = RoundState.createCustomStudyRound(Arrays.asList(t1), 10, new StatsService());

        List<Integer> ids = collectIds(state);
        assertEquals(2, ids.size(), "should return all available when limit exceeds");
    }

    @Test
    void shuffledOptionsArePermutation() {
        Theme t1 = new Theme("t1", Arrays.asList(
                createQ(0, "q0", "a", "b", "c", "d"),
                createQ(1, "q1", "w", "x", "y", "z")
        ));

        RoundState state = RoundState.createCustomStudyRound(Arrays.asList(t1), 2, new StatsService());

        List<String> original0 = Arrays.asList("a", "b", "c", "d");
        List<String> original1 = Arrays.asList("w", "x", "y", "z");

        while (!state.isComplete()) {
            List<String> opts = new ArrayList<>(state.getCurrentOptions());
            int qId = state.getCurrentQuestion().getId();
            List<String> sorted = new ArrayList<>(opts);
            Collections.sort(sorted);
            List<String> expected = new ArrayList<>(qId == 0 ? original0 : original1);
            Collections.sort(expected);
            assertEquals(expected, sorted, "shuffled options should be a permutation of original");
            state.advanceToNext();
        }
    }

    @Test
    void themeNamePreserved() {
        Theme t1 = new Theme("alpha", Arrays.asList(
                createQ(0, "q0", "a", "b", "c", "d")
        ));
        Theme t2 = new Theme("beta", Arrays.asList(
                createQ(1, "q1", "a", "b", "c", "d")
        ));

        RoundState state = RoundState.createCustomStudyRound(Arrays.asList(t1, t2), 1, new StatsService());

        List<String> names = collectThemeNames(state);
        assertEquals(2, names.size());
        assertTrue(names.contains("alpha"));
        assertTrue(names.contains("beta"));
    }

    @Test
    void emptyThemesList() {
        RoundState state = RoundState.createCustomStudyRound(Collections.emptyList(), 5, new StatsService());

        assertEquals(0, state.getSelectedQuestionsCount(), "empty list should produce 0 questions");
        assertTrue(state.isComplete(), "empty round should be immediately complete");
    }
}
