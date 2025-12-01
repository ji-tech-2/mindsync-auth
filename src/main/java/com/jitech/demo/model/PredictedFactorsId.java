package com.jitech.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PredictedFactorsId implements Serializable {
    @Column(name = "pred_id")
    private UUID predId;

    @Column(name = "factor_id")
    private int factorId;

    public PredictedFactorsId() {}

    public PredictedFactorsId(UUID predId, int factorId) {
        this.predId = predId;
        this.factorId = factorId;
    }

    public UUID getPredId() {return predId;}
    public void setPredId(UUID pred_id) {this.predId = pred_id;}
    public int getFactorId() {return factorId;}
    public void setFactorId(int factorId) {this.factorId = factorId;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof PredictedFactorsId)) return false;
        PredictedFactorsId that = (PredictedFactorsId) o;
        return getFactorId() == that.getFactorId() &&
                Objects.equals(getPredId(), that.getPredId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPredId(), getFactorId());
    }
}