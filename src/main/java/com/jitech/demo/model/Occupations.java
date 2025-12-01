package com.jitech.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "occupations")
public class Occupations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "occupation_id")
    private int occupationId;

    @NotNull
    @Column(name = "occupation_name", length = 100, unique = true, nullable = false)
    private String occupationName;

    public Occupations() {}

    public int getOccupationId() {return occupationId;}
    public void setOccupationId(int occupationId) {this.occupationId = occupationId;}
    public String getOccupationName() {return occupationName;}
    public void setOccupationName(String occupationName) {this.occupationName = occupationName;}
}
