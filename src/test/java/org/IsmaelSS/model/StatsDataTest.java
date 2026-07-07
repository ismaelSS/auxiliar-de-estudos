package org.IsmaelSS.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatsDataTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void roundTripSerialization() throws Exception {
        StatsData original = new StatsData();
        StatsData.ThemeStats matematica = new StatsData.ThemeStats();
        matematica.setTotalAnswered(10);
        matematica.setTotalCorrect(7);
        StatsData.QuestionStats q1 = new StatsData.QuestionStats();
        q1.setAnswered(3);
        q1.setCorrect(2);
        matematica.getQuestions().put("2+2?", q1);
        original.getThemes().put("matematica", matematica);
        original.getOverall().setTotalAnswered(10);
        original.getOverall().setTotalCorrect(7);

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(original);
        StatsData deserialized = mapper.readValue(json, StatsData.class);

        assertNotNull(deserialized.getThemes());
        assertEquals(1, deserialized.getThemes().size());
        StatsData.ThemeStats loaded = deserialized.getThemes().get("matematica");
        assertEquals(10, loaded.getTotalAnswered());
        assertEquals(7, loaded.getTotalCorrect());
        assertNotNull(loaded.getQuestions());
        StatsData.QuestionStats loadedQ = loaded.getQuestions().get("2+2?");
        assertEquals(3, loadedQ.getAnswered());
        assertEquals(2, loadedQ.getCorrect());
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
    void questionStatsIncrements() {
        StatsData.QuestionStats qs = new StatsData.QuestionStats();
        qs.setAnswered(5);
        qs.setCorrect(3);
        assertEquals(5, qs.getAnswered());
        assertEquals(3, qs.getCorrect());
    }
}
