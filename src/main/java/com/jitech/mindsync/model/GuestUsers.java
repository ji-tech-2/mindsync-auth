package com.jitech.mindsync.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "guest_users")
public class GuestUsers {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "guest_id", nullable = false)
    private UUID guestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gender_id", nullable = false)
    private Genders gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "occupation_id",  nullable = false)
    private Occupations occupation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_rmt_id", nullable = false)
    private WorkRemotes workRmt;

    @Column(name = "session_token", length = 255, nullable = false)
    private String sessionToken;

    @Column(name = "age", nullable = false)
    private int age;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "guestUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Predictions> predictionsSet =  new HashSet<Predictions>();

    public GuestUsers() {};

    public UUID getGuestId() {return guestId;}
    public void setGuestId(UUID guestId) {this.guestId = guestId;}
    public Genders getGender() {return gender;}
    public void setGender(Genders gender) {this.gender = gender;}
    public Occupations getOccupation() {return occupation;}
    public void setOccupation(Occupations occupation) {this.occupation = occupation;}
    public WorkRemotes getWorkRmt() {return workRmt;}
    public void setWorkRmt(WorkRemotes workRmt) {this.workRmt = workRmt;}
    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
    public  int getAge() {return age;}
    public void setAge(int age) {this.age = age;}
    public Set<Predictions> getPredictionsSet() {return predictionsSet;}
    public void setPredictionsSet(Set<Predictions> predictionsSet) {this.predictionsSet = predictionsSet;}
}