package com.jitech.mindsync.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PredictedFactorsTest {

    @Test
    void testNoArgConstructor() {
        PredictedFactors pf = new PredictedFactors();
        assertNotNull(pf);
        assertNotNull(pf.getId());
    }

    @Test
    void testAllArgConstructor() {
        Predictions prediction = new Predictions();
        prediction.setPredId(java.util.UUID.randomUUID());

        Factors factor = new Factors();
        factor.setFactorId(5);

        PredictedFactors pf = new PredictedFactors(prediction, factor);

        assertEquals(prediction, pf.getPrediction());
        assertEquals(factor, pf.getFactor());
        assertNotNull(pf.getId());
    }

    @Test
    void testSettersAndGetters() {
        PredictedFactors pf = new PredictedFactors();

        PredictedFactorsId id = new PredictedFactorsId(java.util.UUID.randomUUID(), 10);
        Predictions prediction = new Predictions();
        Factors factor = new Factors();

        pf.setId(id);
        pf.setPrediction(prediction);
        pf.setFactor(factor);

        assertEquals(id, pf.getId());
        assertEquals(prediction, pf.getPrediction());
        assertEquals(factor, pf.getFactor());
    }
}
