package com.jitech.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "factors")
public class Factors {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "factor_id")
    private int factorId;

    @NotNull
    @Column(name = "factor_name", length = 100, unique = true, nullable = false)
    private String factorName;

    @NotNull
    @Column(name = "description", nullable = false)
    private String description;

    @NotNull
    @Column(name = "importance", nullable = false)
    private float importance;

    @OneToMany(mappedBy = "factor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PredictedFactors> predictedFactorsSet =  new HashSet<PredictedFactors>();

    @OneToMany(mappedBy = "factor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FactorAdvices> factorAdvicesSet = new HashSet<FactorAdvices>();

    public Factors() {}

    public int getFactorId() {return factorId;}
    public void setFactorId(int factorId) {}
    public String getFactorName() {return factorName;}
    public void setFactorName(String factorName) {this.factorName = factorName;}
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}
    public float getImportance() {return importance;}
    public void setImportance(float importance) {this.importance = importance;}
    public Set<PredictedFactors> getPredictedFactorsSet() {return predictedFactorsSet;}
    public void setPredictedFactorsSet(Set<PredictedFactors> predictedFactorsSet) {this.predictedFactorsSet = predictedFactorsSet;}
    public Set<FactorAdvices>  getFactorAdvicesSet() {return factorAdvicesSet;}
    public void setFactorAdvicesSet(Set<FactorAdvices>  factorAdvicesSet) {this.factorAdvicesSet = factorAdvicesSet;}
}
