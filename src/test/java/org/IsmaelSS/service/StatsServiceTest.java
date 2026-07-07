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
    void recordRoundSingleAnswer() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                new RoundResult("matematica", "2+2?", true)
        ));

        assertEquals(1, service.getOverallStats().getTotalAnswered());
        assertEquals(1, service.getOverallStats().getTotalCorrect());
        assertEquals("100%", service.getHitRate("matematica"));
        assertEquals("100%", service.getOverallHitRate());
    }

    @Test
    void recordRoundMultipleAnswers() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                new RoundResult("matematica", "2+2?", true),
                new RoundResult("matematica", "3+3?", false),
                new RoundResult("historia", "Ano do Brasil?", true),
                new RoundResult("historia", "Capital do Brasil?", false)
        ));

        assertEquals(4, service.getOverallStats().getTotalAnswered());
        assertEquals(2, service.getOverallStats().getTotalCorrect());
        assertEquals("50%", service.getHitRate("matematica"));
        assertEquals("50%", service.getHitRate("historia"));
    }

    @Test
    void hitRateReturnsNAForNoData() {
        StatsService service = new StatsService();
        assertEquals("N/A", service.getHitRate("nonexistent"));
    }

    @Test
    void hitRateReturnsNAForZeroAnswers() {
        StatsService service = new StatsService();
        StatsData.ThemeStats ts = new StatsData.ThemeStats();
        service.getOverallStats().setTotalAnswered(0);
        service.getOverallStats().setTotalCorrect(0);
        assertEquals("N/A", service.getOverallHitRate());
    }

    @Test
    void highestErrorQuestionsRanking() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                new RoundResult("matematica", "q1", true),
                new RoundResult("matematica", "q2", false),
                new RoundResult("matematica", "q3", false),
                new RoundResult("matematica", "q3", false)
        ));

        List<Map.Entry<String, Double>> top = service.getHighestErrorQuestions(5);
        assertEquals(3, top.size());
        assertEquals(1.0, top.get(0).getValue(), 0.001);
        assertEquals(1.0, top.get(1).getValue(), 0.001);
        assertEquals(0.0, top.get(2).getValue(), 0.001);
        assertTrue(top.get(0).getKey().equals("q2") || top.get(0).getKey().equals("q3"));
        assertTrue(top.get(1).getKey().equals("q2") || top.get(1).getKey().equals("q3"));
    }

    @Test
    void highestErrorQuestionsRespectsLimit() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                new RoundResult("matematica", "q1", false),
                new RoundResult("matematica", "q2", false),
                new RoundResult("matematica", "q3", false)
        ));

        List<Map.Entry<String, Double>> top = service.getHighestErrorQuestions(2);
        assertEquals(2, top.size());
    }

    @Test
    void highestErrorQuestionsEmptyWhenNoData() {
        StatsService service = new StatsService();
        assertTrue(service.getHighestErrorQuestions(5).isEmpty());
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
                new RoundResult("matematica", "q1", true),
                new RoundResult("historia", "q1", false)
        ));

        List<String> themes = service.getAllThemesWithData();
        assertEquals(2, themes.size());
        assertTrue(themes.contains("matematica"));
        assertTrue(themes.contains("historia"));
    }

    @Test
    void getThemeStats() {
        StatsService service = new StatsService();
        service.recordRound(List.of(
                new RoundResult("matematica", "q1", true)
        ));

        StatsData.ThemeStats ts = service.getThemeStats("matematica");
        assertNotNull(ts);
        assertEquals(1, ts.getTotalAnswered());
        assertEquals(1, ts.getTotalCorrect());
        assertNull(service.getThemeStats("nonexistent"));
    }

    @Test
    void persistDataAcrossInstances() {
        StatsService writer = new StatsService();
        writer.recordRound(List.of(
                new RoundResult("matematica", "q1", true)
        ));

        StatsService reader = new StatsService();
        assertEquals(1, reader.getOverallStats().getTotalAnswered());
        assertEquals(1, reader.getOverallStats().getTotalCorrect());
    }

    @Test
    void multipleRoundsAccumulate() {
        StatsService service = new StatsService();
        service.recordRound(List.of(new RoundResult("matematica", "q1", true)));
        service.recordRound(List.of(new RoundResult("matematica", "q2", false)));
        service.recordRound(List.of(new RoundResult("matematica", "q3", true)));

        assertEquals(3, service.getOverallStats().getTotalAnswered());
        assertEquals(2, service.getOverallStats().getTotalCorrect());
        assertEquals("67%", service.getHitRate("matematica"));
    }
}
