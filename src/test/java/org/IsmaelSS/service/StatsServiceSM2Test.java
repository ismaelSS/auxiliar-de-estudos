package org.IsmaelSS.service;

import org.IsmaelSS.model.FixationPhase;
import org.IsmaelSS.model.RoundResult;
import org.IsmaelSS.model.StatsData.QuestionScore;
import org.IsmaelSS.model.StatsData.ThemeStats;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StatsServiceSM2Test {

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

    @Test
    void recordRoundAppliesSM2() {
        StatsService service = new StatsService();
        // Record a round with 2 correct and 1 wrong
        service.recordRound(List.of(
                new RoundResult("t1", "q1?", 0, true),
                new RoundResult("t1", "q2?", 1, true),
                new RoundResult("t1", "q3?", 2, false)
        ));

        ThemeStats ts = service.getThemeStats("t1");
        assertNotNull(ts);

        QuestionScore q1 = ts.getQuestions().get("0");
        QuestionScore q2 = ts.getQuestions().get("1");
        QuestionScore q3 = ts.getQuestions().get("2");

        assertNotNull(q1);
        assertNotNull(q2);
        assertNotNull(q3);

        // Correct answers have repCount > 0
        assertTrue(q1.getRepCount() > 0, "correct q1 should have repCount > 0");
        assertTrue(q2.getRepCount() > 0, "correct q2 should have repCount > 0");
        // Wrong answer: repCount stays 0, interval stays 0 → no nextReviewTimestamp
        assertEquals(0, q3.getRepCount(), "wrong q3 should have repCount = 0");

        // Correct answers should have nextReviewTimestamp > 0
        assertTrue(q1.getNextReviewTimestamp() > 0);
        assertTrue(q2.getNextReviewTimestamp() > 0);
    }

    @Test
    void getDueQuestions() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                new RoundResult("t1", "q1?", 0, true)
        ));

        // Manually set nextReviewTimestamp to past
        ThemeStats ts = service.getThemeStats("t1");
        QuestionScore qs = ts.getQuestions().get("0");
        qs.setNextReviewTimestamp(System.currentTimeMillis() - 86_400_000L);

        List<Map.Entry<String, QuestionScore>> due = service.getDueQuestions("t1");
        assertEquals(1, due.size(), "should find 1 due question");
        assertEquals("0", due.get(0).getKey());
    }

    @Test
    void getNewQuestions() {
        StatsService service = new StatsService();

        // Theme with no data should return empty
        List<Map.Entry<String, QuestionScore>> empty = service.getNewQuestions("nonexistent");
        assertTrue(empty.isEmpty(), "nonexistent theme should return empty");

        // Record a round and check
        service.recordRound(List.of(
                new RoundResult("t1", "q1?", 0, true)
        ));

        // After recording, q1 has repCount > 0, so it's no longer "new"
        List<Map.Entry<String, QuestionScore>> newQ = service.getNewQuestions("t1");
        assertTrue(newQ.isEmpty(), "after recording, no questions should be new");
    }

    @Test
    void getFixationPhases() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                new RoundResult("t1", "q1?", 0, true),
                new RoundResult("t1", "q2?", 1, false)
        ));

        Map<FixationPhase, Integer> phases = service.getFixationPhases("t1");
        assertNotNull(phases);
        assertFalse(phases.isEmpty(), "fixation phase map should not be empty");
        // Both questions should be APRENDENDO (correct→interval=1, wrong→repCount=0)
        assertEquals(2, phases.get(FixationPhase.APRENDENDO).intValue());
        assertEquals(0, phases.get(FixationPhase.REVISAO).intValue());
        assertEquals(0, phases.get(FixationPhase.FIXA).intValue());
        assertEquals(0, phases.get(FixationPhase.DOMINIO).intValue());
    }

    @Test
    void getTimelineData() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                new RoundResult("t1", "q1?", 0, true)
        ));

        Map<LocalDate, Map<String, Integer>> timeline = service.getTimelineData();
        assertNotNull(timeline);
        // Should contain today's date
        LocalDate today = LocalDate.now();
        assertTrue(timeline.containsKey(today), "timeline should contain today's date");
        // t1 should have 1 question reviewed
        assertEquals(1, timeline.get(today).get("t1").intValue());
    }

    @Test
    void markThemeAsDone() {
        StatsService service = new StatsService();
        // Record a wrong answer
        service.recordRound(List.of(
                new RoundResult("t1", "q1?", 0, false)
        ));

        ThemeStats ts = service.getThemeStats("t1");
        QuestionScore qs = ts.getQuestions().get("0");

        // Due count should be 0 since just reviewed
        assertEquals(0, service.getDueCount("t1"));

        // Manually set nextReviewTimestamp to past
        qs.setNextReviewTimestamp(System.currentTimeMillis() - 86_400_000L);
        assertEquals(1, service.getDueCount("t1"), "should be 1 due after setting past timestamp");

        // Mark as done
        service.markThemeAsDone("t1");

        // Due count should be 0 now
        assertEquals(0, service.getDueCount("t1"), "should be 0 due after markThemeAsDone");

        // repCount should have increased (updateSM2 was called)
        assertTrue(qs.getRepCount() > 0, "repCount should increase after markThemeAsDone");
    }
}
