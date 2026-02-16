package com.jitech.mindsync.dto;

import java.time.LocalDate;

public class ProfileUpdateRequest {
    private String name;
    private LocalDate dob;
    private String gender;
    private String occupation;
    private String workRmt;

    public ProfileUpdateRequest() {
    }

    public ProfileUpdateRequest(String name, LocalDate dob, String gender, String occupation, String workRmt) {
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.occupation = occupation;
        this.workRmt = workRmt;
    }

    // Getters
    public String getName() {
        return name;
    }

    public LocalDate getDob() {
        return dob;
    }

    public String getGender() {
        return gender;
    }

    public String getOccupation() {
        return occupation;
    }

    public String getWorkRmt() {
        return workRmt;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public void setWorkRmt(String workRmt) {
        this.workRmt = workRmt;
    }
}
