package com.jitech.mindsync.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "genders")
public class Genders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gender_id")
    private int genderId;

    @NotNull
    @Column(name = "gender_name", length = 50, unique = true, nullable = false)
    private String genderName;

    public Genders() {}

    public int getGenderId() {return genderId;}
    public void setGenderId(int genderId) {this.genderId = genderId;}
    public String getGenderName() {return genderName;}
    public void setGenderName(String genderName) {this.genderName = genderName;}
}
