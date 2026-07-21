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

class RoundStateDueReviewTest {

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

    @Test
    void picksDueQuestions() {
        StatsService service = new StatsService();
        Theme t1 = new Theme("t1", Arrays.asList(
                createQ(0, "text1", "a", "b", "c", "d"),
                createQ(1, "text2", "a", "b", "c", "d"),
                createQ(2, "text3", "a", "b", "c", "d")
        ));

        // Record a round so questions are in the stats
        service.recordRound(Arrays.asList(
                new RoundResult("t1", "text1", 0, true),
                new RoundResult("t1", "text2", 1, true),
                new RoundResult("t1", "text3", 2, true)
        ));

        // Manually set 2 of them to have past nextReviewTimestamp (overdue)
        StatsData.ThemeStats ts = service.getThemeStats("t1");
        ts.getQuestions().get("0").setNextReviewTimestamp(System.currentTimeMillis() - 86_400_000L);
        ts.getQuestions().get("1").setNextReviewTimestamp(System.currentTimeMillis() - 86_400_000L);
        // Question 2 stays with current timestamp (not overdue)

        RoundState state = RoundState.createDueReviewRound(t1, service);

        List<Integer> ids = collectIds(state);
        assertEquals(2, ids.size(), "should pick exactly 2 due questions");
        assertTrue(ids.contains(0));
        assertTrue(ids.contains(1));
    }

    @Test
    void fallsBackToNewQuestions() {
        StatsService service = new StatsService();
        Theme t1 = new Theme("t1", Arrays.asList(
                createQ(0, "text1", "a", "b", "c", "d"),
                createQ(1, "text2", "a", "b", "c", "d"),
                createQ(2, "text3", "a", "b", "c", "d")
        ));

        // Record the theme so it appears in stats, but with future nextReview
        // so none are "due" — they'll have repCount=0 (new)
        service.recordRound(Arrays.asList(
                new RoundResult("t1", "text1", 0, true),
                new RoundResult("t1", "text2", 1, true),
                new RoundResult("t1", "text3", 2, true)
        ));

        // Set nextReviewTimestamp far in the future so they're not "due"
        StatsData.ThemeStats ts = service.getThemeStats("t1");
        long farFuture = System.currentTimeMillis() + 365L * 86_400_000L;
        ts.getQuestions().get("0").setNextReviewTimestamp(farFuture);
        ts.getQuestions().get("1").setNextReviewTimestamp(farFuture);
        ts.getQuestions().get("2").setNextReviewTimestamp(farFuture);

        // After recording with updateSM2(true), repCount > 0, so they aren't "new" either.
        // Instead, manually set repCount = 0 to simulate unreviewed questions in stats
        ts.getQuestions().get("0").setRepCount(0);
        ts.getQuestions().get("1").setRepCount(0);
        ts.getQuestions().get("2").setRepCount(0);
        // Also reset interval to 0 so they match "unreviewed" state
        ts.getQuestions().get("0").setInterval(0);
        ts.getQuestions().get("1").setInterval(0);
        ts.getQuestions().get("2").setInterval(0);

        // Not due (future timestamps) but repCount=0 → fallback to new questions
        RoundState state = RoundState.createDueReviewRound(t1, service);

        List<Integer> ids = collectIds(state);
        assertEquals(3, ids.size(), "should fall back to all 3 new questions");
        assertTrue(ids.contains(0));
        assertTrue(ids.contains(1));
        assertTrue(ids.contains(2));
    }
}
