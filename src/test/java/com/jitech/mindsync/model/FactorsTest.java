package com.jitech.mindsync.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FactorsTest {

    @Test
    void testNoArgConstructor() {
        Factors factor = new Factors();
        assertNotNull(factor);
    }

    @Test
    void testGettersAndSetters() {
        Factors factor = new Factors();

        // factorId is auto-generated and has an empty setter, so we don't test it here
        factor.setFactorName("Screen Time");
        factor.setDescription("Impact of screen time on mental health");
        factor.setImportance(0.85f);

        Set<PredictedFactors> predictedFactors = new HashSet<>();
        factor.setPredictedFactorsSet(predictedFactors);

        Set<FactorAdvices> factorAdvices = new HashSet<>();
        factor.setFactorAdvicesSet(factorAdvices);

        assertNotNull(factor);
        assertEquals("Screen Time", factor.getFactorName());
        assertEquals("Impact of screen time on mental health", factor.getDescription());
        assertEquals(0.85f, factor.getImportance(), 0.001);
        assertEquals(predictedFactors, factor.getPredictedFactorsSet());
        assertEquals(factorAdvices, factor.getFactorAdvicesSet());
    }

    @Test
    void testFactorNameLength() {
        Factors factor = new Factors();
        String longName = "A".repeat(100);
        factor.setFactorName(longName);
        assertEquals(longName, factor.getFactorName());
    }

    @Test
    void testImportanceRange() {
        Factors factor = new Factors();

        factor.setImportance(0.0f);
        assertEquals(0.0f, factor.getImportance());

        factor.setImportance(1.0f);
        assertEquals(1.0f, factor.getImportance());

        factor.setImportance(0.5f);
        assertEquals(0.5f, factor.getImportance());
    }
}
