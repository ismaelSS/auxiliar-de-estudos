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
                new RoundResult("t1", "q1 text?", 0, true)
        ));
        assertEquals(1, service.getOverallStats().getTotalAnswered());
        assertEquals(1, service.getOverallStats().getTotalCorrect());
        assertEquals("2", service.getAproveitamento("t1"));  // +2 for one correct
    }

    @Test
    void recordRoundSingleWrongAnswer() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                                    new RoundResult("t1", "q1 text?", 0, false)
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
                    new RoundResult("t1", "q1 text?", 0, true)
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
                new RoundResult("t1", "q1 text?", 0, false)
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
        service.recordRound(List.of(new RoundResult("t1", "q1?", 0, true)));
        // q2: wrong (-3) → score=-3 → weight -3
        service.recordRound(List.of(new RoundResult("t1", "q2?", 1, false)));
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
        service.recordRound(List.of(new RoundResult("t1", "q1?", 0, true)));
        // q2: wrong (-3)
        service.recordRound(List.of(new RoundResult("t1", "q2?", 1, false)));
        // q3: wrong twice (-3, -3 = -6)
        service.recordRound(List.of(new RoundResult("t1", "q3?", 2, false)));
        service.recordRound(List.of(new RoundResult("t1", "q3?", 2, false)));

        List<Map.Entry<String, Integer>> lowest = service.getLowestScoreQuestions(5);
        assertEquals(3, lowest.size());
        // q3 should be lowest (-6), then q2 (-3), then q1 (+2)
        assertEquals("2", lowest.get(0).getKey());
        assertEquals(-6, lowest.get(0).getValue());
        assertEquals("1", lowest.get(1).getKey());
        assertEquals(-3, lowest.get(1).getValue());
        assertEquals("0", lowest.get(2).getKey());
        assertEquals(2, lowest.get(2).getValue());
    }

    @Test
    void lowestScoreQuestionsRespectsLimit() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                new RoundResult("t1", "q1?", 0, false),
                new RoundResult("t1", "q2?", 1, false),
                new RoundResult("t1", "q3?", 2, false)
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
                new RoundResult("t1", "q1?", 0, true),
                new RoundResult("t2", "q2?", 0, false)
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
                new RoundResult("t1", "q1?", 0, true)
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
                new RoundResult("t1", "q1?", 0, true)
        ));
        StatsService reader = new StatsService();
        assertEquals(1, reader.getOverallStats().getTotalAnswered());
        assertEquals(1, reader.getOverallStats().getTotalCorrect());
    }

    @Test
    void multipleRoundsAccumulate() {
        StatsService service = new StatsService();
        service.recordRound(List.of(new RoundResult("t1", "q1?", 0, true)));
        service.recordRound(List.of(new RoundResult("t1", "q2?", 1, false)));
        service.recordRound(List.of(new RoundResult("t1", "q3?", 2, true)));
        assertEquals(3, service.getOverallStats().getTotalAnswered());
        assertEquals(2, service.getOverallStats().getTotalCorrect());
        // q1=+2, q2=-3, q3=+2 → weights: +2, -3, +2 = +1
        assertEquals("1", service.getAproveitamento("t1"));
    }

    // --- getDominio() tests ---

    @Test
    void dominioReturnsNAForNoData() {
        StatsService service = new StatsService();
        assertEquals("N/A", service.getDominio("nonexistent"));
    }

    @Test
    void dominioReturns100WhenAllCorrect() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                new RoundResult("t1", "q1?", 0, true),
                new RoundResult("t1", "q2?", 1, true),
                new RoundResult("t1", "q3?", 2, true)
        ));
        assertEquals("100", service.getDominio("t1"));
    }

    @Test
    void dominioReturns0WhenAllWrong() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                new RoundResult("t1", "q1?", 0, false),
                new RoundResult("t1", "q2?", 1, false),
                new RoundResult("t1", "q3?", 2, false)
        ));
        assertEquals("0", service.getDominio("t1"));
    }

    @Test
    void dominioCalculationWithMixedScores() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                new RoundResult("t1", "q1?", 0, true),   // score=2 (>0)
                new RoundResult("t1", "q2?", 1, true),   // score=2 (>0)
                new RoundResult("t1", "q3?", 2, false)   // score=-3 (<0)
        ));
        // 2 out of 3 positive → (2*100)/3 = 66
        assertEquals("66", service.getDominio("t1"));
    }

    // --- getLowestScoreQuestionsByTheme() tests ---

    @Test
    void lowestByThemeReturnsOnlyThemeQuestions() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                new RoundResult("t1", "q1?", 0, true),
                new RoundResult("t1", "q2?", 1, false),
                new RoundResult("t2", "q3?", 0, true)
        ));
        List<Map.Entry<String, Integer>> result = service.getLowestScoreQuestionsByTheme("t1", 10);
        // t1 has q1 (correct, score >= 0) and q2 (wrong, score < 0) — only q2 returned
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getKey());
    }

    @Test
    void lowestByThemeRespectsLimit() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                new RoundResult("t1", "q1?", 0, false),
                new RoundResult("t1", "q2?", 1, false),
                new RoundResult("t1", "q3?", 2, false)
        ));
        List<Map.Entry<String, Integer>> result = service.getLowestScoreQuestionsByTheme("t1", 2);
        assertEquals(2, result.size());
    }

    @Test
    void lowestByThemeReturnsEmptyForUnknownTheme() {
        StatsService service = new StatsService();
        List<Map.Entry<String, Integer>> result = service.getLowestScoreQuestionsByTheme("nonexistent", 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void lowestByThemeReturnsEmptyForEmptyTheme() {
        StatsService service = new StatsService();
        // Record data for t1 only — theme with no questions doesn't exist in data
        service.recordRound(List.of(
                new RoundResult("t1", "q1?", 0, true)
        ));
        List<Map.Entry<String, Integer>> result = service.getLowestScoreQuestionsByTheme("t1", 10);
        // t1 has questions, so this won't be empty — let's test a theme that's not recorded
        List<Map.Entry<String, Integer>> emptyResult = service.getLowestScoreQuestionsByTheme("t2", 10);
        assertTrue(emptyResult.isEmpty());
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
