package org.IsmaelSS.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatsDataTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void roundTripSerialization() throws Exception {
        StatsData original = new StatsData();
        StatsData.ThemeStats t1 = new StatsData.ThemeStats();
        t1.setTotalAnswered(10);
        t1.setTotalCorrect(7);
        StatsData.QuestionScore q1 = new StatsData.QuestionScore();
        q1.setScore(2);
        t1.getQuestions().put("q1-id", q1);
        original.getThemes().put("t1", t1);
        original.getOverall().setTotalAnswered(10);
        original.getOverall().setTotalCorrect(7);

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(original);
        StatsData deserialized = mapper.readValue(json, StatsData.class);

        assertNotNull(deserialized.getThemes());
        assertEquals(1, deserialized.getThemes().size());
        StatsData.ThemeStats loaded = deserialized.getThemes().get("t1");
        assertEquals(10, loaded.getTotalAnswered());
        assertEquals(7, loaded.getTotalCorrect());
        assertNotNull(loaded.getQuestions());
        StatsData.QuestionScore loadedQ = loaded.getQuestions().get("q1-id");
        assertEquals(2, loadedQ.getScore());
        assertEquals(10, deserialized.getOverall().getTotalAnswered());
        assertEquals(7, deserialized.getOverall().getTotalCorrect());
    }

    @Test
    void emptyStatsData() {
        StatsData data = new StatsData();
        assertTrue(data.getThemes().isEmpty());
        assertEquals(0, data.getOverall().getTotalAnswered());
        assertEquals(0, data.getOverall().getTotalCorrect());
    }

    @Test
    void themeWithZeroAnswers() {
        StatsData.ThemeStats ts = new StatsData.ThemeStats();
        assertEquals(0, ts.getTotalAnswered());
        assertEquals(0, ts.getTotalCorrect());
        assertTrue(ts.getQuestions().isEmpty());
    }

    @Test
    void questionScoreRecordCorrectCapsAtFive() {
        StatsData.QuestionScore qs = new StatsData.QuestionScore();
        for (int i = 0; i < 10; i++) {
            qs.recordCorrect();
        }
        assertEquals(5, qs.getScore());
    }

    @Test
    void questionScoreRecordWrongFloorsAtMinusTen() {
        StatsData.QuestionScore qs = new StatsData.QuestionScore();
        for (int i = 0; i < 10; i++) {
            qs.recordWrong();
        }
        assertEquals(-10, qs.getScore());
    }

    @Test
    void questionScoreMixedAnswers() {
        StatsData.QuestionScore qs = new StatsData.QuestionScore();
        qs.recordCorrect();  // +2 → 2
        qs.recordCorrect();  // +2 → 4
        qs.recordWrong();    // -3 → 1
        assertEquals(1, qs.getScore());
    }

    @Test
    void questionScoreDefaultsToZero() {
        StatsData.QuestionScore qs = new StatsData.QuestionScore();
        assertEquals(0, qs.getScore());
    }
}
