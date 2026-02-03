package com.jitech.mindsync.dto;

public class ProfileUpdateRequest {
    private String name;
    private String gender;
    private String occupation;

    public ProfileUpdateRequest() {}

    public ProfileUpdateRequest(String name, String gender, String occupation) {
        this.name = name;
        this.gender = gender;
        this.occupation = occupation;
    }

    // Getters
    public String getName() { return name; }
    public String getGender() { return gender; }
    public String getOccupation() { return occupation; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setGender(String gender) { this.gender = gender; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
}
