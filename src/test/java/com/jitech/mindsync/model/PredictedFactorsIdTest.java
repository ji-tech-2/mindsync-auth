package com.jitech.mindsync.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PredictedFactorsIdTest {

    @Test
    void testNoArgConstructor() {
        PredictedFactorsId id = new PredictedFactorsId();
        assertNotNull(id);
    }

    @Test
    void testAllArgConstructor() {
        UUID predId = UUID.randomUUID();
        int factorId = 5;

        PredictedFactorsId id = new PredictedFactorsId(predId, factorId);

        assertEquals(predId, id.getPredId());
        assertEquals(factorId, id.getFactorId());
    }

    @Test
    void testSettersAndGetters() {
        PredictedFactorsId id = new PredictedFactorsId();
        UUID predId = UUID.randomUUID();
        int factorId = 10;

        id.setPredId(predId);
        id.setFactorId(factorId);

        assertEquals(predId, id.getPredId());
        assertEquals(factorId, id.getFactorId());
    }

    @Test
    void testEqualsWithSameObject() {
        PredictedFactorsId id = new PredictedFactorsId(UUID.randomUUID(), 5);
        assertEquals(id, id);
    }

    @Test
    void testEqualsWithEqualObjects() {
        UUID predId = UUID.randomUUID();
        PredictedFactorsId id1 = new PredictedFactorsId(predId, 5);
        PredictedFactorsId id2 = new PredictedFactorsId(predId, 5);

        assertEquals(id1, id2);
    }

    @Test
    void testEqualsWithDifferentPredId() {
        PredictedFactorsId id1 = new PredictedFactorsId(UUID.randomUUID(), 5);
        PredictedFactorsId id2 = new PredictedFactorsId(UUID.randomUUID(), 5);

        assertNotEquals(id1, id2);
    }

    @Test
    void testEqualsWithDifferentFactorId() {
        UUID predId = UUID.randomUUID();
        PredictedFactorsId id1 = new PredictedFactorsId(predId, 5);
        PredictedFactorsId id2 = new PredictedFactorsId(predId, 10);

        assertNotEquals(id1, id2);
    }

    @Test
    void testEqualsWithNull() {
        PredictedFactorsId id = new PredictedFactorsId(UUID.randomUUID(), 5);
        assertNotEquals(id, null);
    }

    @Test
    void testEqualsWithDifferentClass() {
        PredictedFactorsId id = new PredictedFactorsId(UUID.randomUUID(), 5);
        assertNotEquals(id, new Object());
    }

    @Test
    void testHashCodeConsistency() {
        UUID predId = UUID.randomUUID();
        PredictedFactorsId id = new PredictedFactorsId(predId, 5);

        int hash1 = id.hashCode();
        int hash2 = id.hashCode();

        assertEquals(hash1, hash2);
    }

    @Test
    void testHashCodeEqualityForEqualObjects() {
        UUID predId = UUID.randomUUID();
        PredictedFactorsId id1 = new PredictedFactorsId(predId, 5);
        PredictedFactorsId id2 = new PredictedFactorsId(predId, 5);

        assertEquals(id1.hashCode(), id2.hashCode());
    }
}
