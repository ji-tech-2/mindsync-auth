package com.jitech.mindsync.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "factor_advices")
public class FactorAdvices {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int adviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factor_id")
    private Factors factor;

    @NotNull
    @Column(name = "advice_text", nullable = false)
    private String adviceText;

    public FactorAdvices() {}

    public int getAdviceId() {return  adviceId;}
    public void setAdviceId(int adviceId) {this.adviceId = adviceId;}
    public Factors getFactor() {return factor;}
    public void setFactor(Factors factor) {this.factor = factor;}
    public String getAdviceText() {return adviceText;}
    public void setAdviceText(String adviceText) {this.adviceText = adviceText;}
}
