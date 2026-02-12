package com.jitech.mindsync.dto;

import java.time.LocalDate;

public class RegisterRequest {
    private String email;
    private String password;
    private String name;
    private LocalDate dob;
    private String gender;
    private String occupation; 
    private String workRmt;

    public RegisterRequest() {}

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public LocalDate getDob() { return dob; }
    public String getGender() { return gender; }

    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public void setGender(String gender) { this.gender = gender; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public String getWorkRmt() { return workRmt; }
    public void setWorkRmt(String workRmt) { this.workRmt = workRmt; }
}