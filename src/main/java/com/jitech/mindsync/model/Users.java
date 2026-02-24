package com.jitech.mindsync.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gender_id", nullable = false)
    private Genders gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "occupation_id", nullable = false)
    private Occupations occupation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_rmt_id", nullable = false)
    private WorkRemotes workRmt;

    @NotNull
    @Column(name = "name", length = 120, nullable = false)
    private String name;

    @NotNull
    @Column(name = "email", length = 120, unique = true, nullable = false)
    private String email;

    @NotNull
    @Column(name = "username", length = 60, unique = true, nullable = false)
    private String username;

    @NotNull
    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @NotNull
    @Column(name = "dob",  nullable = false)
    private LocalDate dob;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Users() {}

    // Getters
    public UUID getUserId() { return userId; }
    public Genders getGender() { return gender; }
    public Occupations getOccupation() { return occupation; }
    public WorkRemotes getWorkRmt() { return workRmt; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public LocalDate getDob() { return dob; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setGender(Genders gender) { this.gender = gender; }
    public void setOccupation(Occupations occupation) { this.occupation = occupation; }
    public void setWorkRmt(WorkRemotes workRmt) { this.workRmt = workRmt; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}