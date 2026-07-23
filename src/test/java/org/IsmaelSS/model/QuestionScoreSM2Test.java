package org.IsmaelSS.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuestionScoreSM2Test {

    @Test
    void intervalIncreasesOnCorrect() {
        StatsData.QuestionScore qs = new StatsData.QuestionScore();
        // Offset from cap so EF progression is visible
        qs.setEaseFactor(2.0);

        qs.updateSM2(true);
        // First review: repCount==0 → interval=1, not ceil(1*EF)
        assertEquals(1, qs.getInterval(), "first correct should set interval=1");
        assertEquals(1, qs.getRepCount());
        assertEquals(2.1, qs.getEaseFactor(), 0.001, "EF should increase by 0.1");

        // Override interval so next call uses the formula, not the first-review special case
        qs.setInterval(5);

        qs.updateSM2(true);
        // interval = ceil(5 * 2.2) = ceil(11.0) = 11
        assertEquals(11, qs.getInterval(), "should use ceil(5 * 2.2) = 11");
        assertEquals(2, qs.getRepCount());
        assertEquals(2.2, qs.getEaseFactor(), 0.001, "EF should increase by 0.1 again");
    }

    @Test
    void intervalResetsOnWrong() {
        StatsData.QuestionScore qs = new StatsData.QuestionScore();
        // Start below caps so progression is visible
        qs.setEaseFactor(2.0);
        qs.updateSM2(true);  // interval=1 (first review), rep=1, ef=2.1
        qs.updateSM2(true);  // interval=3 (ceil(1*2.1)=3), rep=2, ef=2.2
        qs.updateSM2(true);  // interval=7 (ceil(3*2.2)=ceil(6.6)=7), rep=3, ef=2.3

        // Now wrong — interval and repCount preserved, only ef drops
        qs.updateSM2(false);

        assertEquals(7, qs.getInterval(), "wrong should not reset interval");
        assertEquals(3, qs.getRepCount(), "wrong should not reset repCount");
        assertEquals(2.1, qs.getEaseFactor(), 0.001, "ef = 2.3 - 0.2");
    }

    @Test
    void easeFactorClamps() {
        // Upper clamp: start at 2.5, call correct 10 times — never exceeds 2.5
        StatsData.QuestionScore qs = new StatsData.QuestionScore();
        for (int i = 0; i < 10; i++) {
            qs.updateSM2(true);
        }
        assertEquals(2.5, qs.getEaseFactor(), 0.001, "easeFactor should never exceed 2.5");

        // Lower clamp: start at 1.3, call wrong 5 times — never below 1.3
        StatsData.QuestionScore qs2 = new StatsData.QuestionScore();
        qs2.setEaseFactor(1.3);
        for (int i = 0; i < 5; i++) {
            qs2.updateSM2(false);
        }
        assertEquals(1.3, qs2.getEaseFactor(), 0.001, "easeFactor should never go below 1.3");
    }

    @Test
    void fixationPhases() {
        // repCount=0 → APRENDENDO
        StatsData.QuestionScore qs = new StatsData.QuestionScore();
        assertEquals(FixationPhase.APRENDENDO, qs.getFixationPhase());

        // repCount=1, interval=1 → APRENDENDO (interval<=1 overrides)
        qs.setRepCount(1);
        qs.setInterval(1);
        assertEquals(FixationPhase.APRENDENDO, qs.getFixationPhase());

        // repCount=1, interval=3 → REVISAO
        qs.setInterval(3);
        assertEquals(FixationPhase.REVISAO, qs.getFixationPhase());

        // repCount=2, interval=5 → REVISAO
        qs.setRepCount(2);
        qs.setInterval(5);
        assertEquals(FixationPhase.REVISAO, qs.getFixationPhase());

        // repCount=3, interval=8 → FIXA
        qs.setRepCount(3);
        qs.setInterval(8);
        assertEquals(FixationPhase.FIXA, qs.getFixationPhase());

        // repCount=5, interval=25 → FIXA
        qs.setRepCount(5);
        qs.setInterval(25);
        assertEquals(FixationPhase.FIXA, qs.getFixationPhase());

        // repCount=6, interval=31 → DOMINIO
        qs.setRepCount(6);
        qs.setInterval(31);
        assertEquals(FixationPhase.DOMINIO, qs.getFixationPhase());
    }

    @Test
    void timestampsSet() {
        StatsData.QuestionScore qs = new StatsData.QuestionScore();
        long before = System.currentTimeMillis();

        qs.updateSM2(true);

        long after = System.currentTimeMillis();
        assertTrue(qs.getLastReviewTimestamp() > 0, "lastReviewTimestamp should be > 0");
        assertTrue(qs.getNextReviewTimestamp() > qs.getLastReviewTimestamp(),
                "nextReviewTimestamp should be after lastReviewTimestamp");
        assertTrue(qs.getLastReviewTimestamp() >= before, "timestamp should be >= before");
        assertTrue(qs.getLastReviewTimestamp() <= after, "timestamp should be <= after");
    }
}
