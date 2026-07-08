package org.IsmaelSS.service;

import org.IsmaelSS.model.RoundResult;
import org.IsmaelSS.model.StatsData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StatsServiceTest {

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
    void constructorCreatesEmptyDataWhenNoFile() {
        StatsService service = new StatsService();
        assertEquals(0, service.getOverallStats().getTotalAnswered());
        assertEquals(0, service.getOverallStats().getTotalCorrect());
    }

    @Test
    void recordRoundSingleCorrectAnswer() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                new RoundResult("t1", "q1 text?", "q1", true)
        ));
        assertEquals(1, service.getOverallStats().getTotalAnswered());
        assertEquals(1, service.getOverallStats().getTotalCorrect());
        assertEquals("2", service.getAproveitamento("t1"));  // +2 for one correct
    }

    @Test
    void recordRoundSingleWrongAnswer() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                new RoundResult("t1", "q1 text?", "q1", false)
        ));
        assertEquals(1, service.getOverallStats().getTotalAnswered());
        assertEquals(0, service.getOverallStats().getTotalCorrect());
        assertEquals("-3", service.getAproveitamento("t1"));  // -3 for one wrong
    }

    @Test
    void scoreCapsAtFive() {
        StatsService service = new StatsService();
        // 10 correct answers → score should cap at +5
        for (int i = 0; i < 10; i++) {
            service.recordRound(List.of(
                    new RoundResult("t1", "q1 text?", "q1", true)
            ));
        }
        List<Map.Entry<String, Integer>> lowest = service.getLowestScoreQuestions(10);
        assertEquals(1, lowest.size());
        assertEquals(5, lowest.get(0).getValue());  // capped at +5
    }

    @Test
    void scoreFloorsAtMinusTen() {
        StatsService service = new StatsService();
        // 10 wrong answers → score should floor at -10
        for (int i = 0; i < 10; i++) {
            service.recordRound(List.of(
                    new RoundResult("t1", "q1 text?", "q1", false)
            ));
        }
        List<Map.Entry<String, Integer>> lowest = service.getLowestScoreQuestions(10);
        assertEquals(1, lowest.size());
        assertEquals(-10, lowest.get(0).getValue());  // floored at -10
    }

    @Test
    void aproveitamentoCalculation() {
        StatsService service = new StatsService();
        // q1: correct (+2) → score=+2 → weight +2
        service.recordRound(List.of(new RoundResult("t1", "q1?", "q1", true)));
        // q2: wrong (-3) → score=-3 → weight -3
        service.recordRound(List.of(new RoundResult("t1", "q2?", "q2", false)));
        // q3: no answers → score=0 → weight 0
        // Total weight: +2 + (-3) + 0 = -1
        assertEquals("-1", service.getAproveitamento("t1"));
    }

    @Test
    void aproveitamentoReturnsNAForNoData() {
        StatsService service = new StatsService();
        assertEquals("N/A", service.getAproveitamento("nonexistent"));
    }

    @Test
    void lowestScoreQuestionsRanking() {
        StatsService service = new StatsService();
        // q1: correct (+2)
        service.recordRound(List.of(new RoundResult("t1", "q1?", "q1", true)));
        // q2: wrong (-3)
        service.recordRound(List.of(new RoundResult("t1", "q2?", "q2", false)));
        // q3: wrong twice (-3, -3 = -6)
        service.recordRound(List.of(new RoundResult("t1", "q3?", "q3", false)));
        service.recordRound(List.of(new RoundResult("t1", "q3?", "q3", false)));

        List<Map.Entry<String, Integer>> lowest = service.getLowestScoreQuestions(5);
        assertEquals(3, lowest.size());
        // q3 should be lowest (-6), then q2 (-3), then q1 (+2)
        assertEquals("q3", lowest.get(0).getKey());
        assertEquals(-6, lowest.get(0).getValue());
        assertEquals("q2", lowest.get(1).getKey());
        assertEquals(-3, lowest.get(1).getValue());
        assertEquals("q1", lowest.get(2).getKey());
        assertEquals(2, lowest.get(2).getValue());
    }

    @Test
    void lowestScoreQuestionsRespectsLimit() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                new RoundResult("t1", "q1?", "q1", false),
                new RoundResult("t1", "q2?", "q2", false),
                new RoundResult("t1", "q3?", "q3", false)
        ));
        assertEquals(2, service.getLowestScoreQuestions(2).size());
    }

    @Test
    void lowestScoreQuestionsEmptyWhenNoData() {
        StatsService service = new StatsService();
        assertTrue(service.getLowestScoreQuestions(5).isEmpty());
    }

    @Test
    void recordRoundEmptyListDoesNotCrash() {
        StatsService service = new StatsService();
        service.recordRound(List.of());
        assertEquals(0, service.getOverallStats().getTotalAnswered());
    }

    @Test
    void getAllThemesWithData() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                new RoundResult("t1", "q1?", "q1", true),
                new RoundResult("t2", "q2?", "q2", false)
        ));
        List<String> themes = service.getAllThemesWithData();
        assertEquals(2, themes.size());
        assertTrue(themes.contains("t1"));
        assertTrue(themes.contains("t2"));
    }

    @Test
    void getThemeStats() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                new RoundResult("t1", "q1?", "q1", true)
        ));
        StatsData.ThemeStats ts = service.getThemeStats("t1");
        assertNotNull(ts);
        assertEquals(1, ts.getTotalAnswered());
        assertEquals(1, ts.getTotalCorrect());
        assertNull(service.getThemeStats("nonexistent"));
    }

    @Test
    void persistDataAcrossInstances() {
        StatsService writer = new StatsService();
        writer.recordRound(List.of(
                new RoundResult("t1", "q1?", "q1", true)
        ));
        StatsService reader = new StatsService();
        assertEquals(1, reader.getOverallStats().getTotalAnswered());
        assertEquals(1, reader.getOverallStats().getTotalCorrect());
    }

    @Test
    void multipleRoundsAccumulate() {
        StatsService service = new StatsService();
        service.recordRound(List.of(new RoundResult("t1", "q1?", "q1", true)));
        service.recordRound(List.of(new RoundResult("t1", "q2?", "q2", false)));
        service.recordRound(List.of(new RoundResult("t1", "q3?", "q3", true)));
        assertEquals(3, service.getOverallStats().getTotalAnswered());
        assertEquals(2, service.getOverallStats().getTotalCorrect());
        // q1=+2, q2=-3, q3=+2 → weights: +2, -3, +2 = +1
        assertEquals("1", service.getAproveitamento("t1"));
    }

    @Test
    void migrationFromOldFormatPreservesThemeTotals() throws Exception {
        // Write old-format JSON
        String oldJson = "{"
                + "\"themes\":{\"t1\":{\"totalAnswered\":5,\"totalCorrect\":3,"
                + "\"questions\":{\"A long question text with spaces?\":" 
                + "{\"answered\":2,\"correct\":1}}}},"
                + "\"overall\":{\"totalAnswered\":5,\"totalCorrect\":3}}";
        java.nio.file.Files.writeString(
                java.nio.file.Paths.get(STATS_FILE), oldJson);

        StatsService service = new StatsService();
        assertEquals(5, service.getOverallStats().getTotalAnswered());
        assertEquals(3, service.getOverallStats().getTotalCorrect());
        // Per-question stats reset — score is 0 for any new round
        StatsData.ThemeStats ts = service.getThemeStats("t1");
        assertNotNull(ts);
        assertEquals(0, ts.getQuestions().size());  // old per-question data was reset
    }
}
