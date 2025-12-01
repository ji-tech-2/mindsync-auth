package com.jitech.mindsync.model;

import jakarta.persistence.*;

@Entity
@Table(name = "predicted_factors")
public class PredictedFactors {
    @EmbeddedId
    private PredictedFactorsId id =  new PredictedFactorsId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("predId")
    @JoinColumn(name = "pred_id")
    private Predictions prediction;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("factorId")
    @JoinColumn(name = "factor_id")
    private Factors factor;

    public PredictedFactors() {}

    public PredictedFactors(Predictions prediction, Factors factor) {
        this.prediction = prediction;
        this.factor = factor;
        this.id = new PredictedFactorsId(prediction.getPredId(), factor.getFactorId());
    }

    public PredictedFactorsId getId() {return id;}
    public void setId(PredictedFactorsId id) {this.id = id;}
    public Predictions getPrediction() {return prediction;}
    public void setPrediction(Predictions prediction) {this.prediction = prediction;}
    public Factors getFactor() {return factor;}
    public void setFactor(Factors factor) {this.factor = factor;}
}
