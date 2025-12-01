//package com.jitech.demo.model;
//
//import java.time.LocalDate;
//
//import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.NotNull;
//
//
//public class RegisterRequest {
//    @NotNull
//    @Email
//    private String email;
//
//    @NotNull
//    private String password;
//
//    @NotNull
//    private String username;
//
//    @NotNull
//    private String name;
//
//    @NotNull
//    private LocalDate dob;
//
//    @NotNull
//    private Genders gender;
//
//    @NotNull
//    private String occupation;
//
//    @NotNull
//    private WorkMode workMode;
//
//    public String getEmail() {return email;}
//    public void setEmail(String email) {this.email = email;}
//    public String getPassword() {return password;}
//    public void setPassword(String password) {this.password = password;}
//    public String getUsername() {return username;}
//    public void setUsername(String username) {this.username = username;}
//    public String getName() {return name;}
//    public void setName(String name) {this.name = name;}
//    public LocalDate getDob() {return dob;}
//    public void setDob(LocalDate dob) {this.dob = dob;}
//    public Genders getGender() {return gender;}
//    public void setGender(Genders gender) {this.gender = gender;}
//    public String getOccupation() {return occupation;}
//    public void setOccupation(String occupation){this.occupation = occupation;}
//    public WorkMode getWorkMode() {return workMode;}
//    public void setWorkMode(WorkMode workMode) {this.workMode = workMode;}
//}
