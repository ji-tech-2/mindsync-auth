package com.jitech.mindsync.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "predictions")
public class Predictions {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "pred_id")
    private UUID predId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = true)
    private GuestUsers guestUser;

    @NotNull
    @Column(name = "pred_date",  nullable = false)
    private LocalDateTime predDate;

    @NotNull
    @Column(name = "screen_time",  nullable = false)
    private float screenTime;

    @NotNull
    @Column(name = "work_screen",  nullable = false)
    private float workScreen;

    @NotNull
    @Column(name = "leisure_screen",  nullable = false)
    private float leisureScreen;

    @NotNull
    @Column(name = "sleep_hours",  nullable = false)
    private float sleepHours;

    @NotNull
    @Column(name = "sleep_quality",  nullable = false)
    private int sleepQuality;

    @NotNull
    @Column(name = "stress_level",  nullable = false)
    private float stressLevel;

    @NotNull
    @Column(name = "productivity",  nullable = false)
    private float productivity;

    @NotNull
    @Column(name = "exercise",  nullable = false)
    private int exercise;

    @NotNull
    @Column(name = "social",  nullable = false)
    private float social;

    @NotNull
    @Column(name = "mental_index",  nullable = false)
    private float mentalIndex;

    @OneToMany(mappedBy = "prediction", fetch = FetchType.LAZY)
    private Set<PredictedFactors> predictedFactorsSet =  new HashSet<PredictedFactors>();

    public Predictions() {}

    // Getters
    public UUID getPredId() { return predId; }
    public Users getUser() { return user; }
    public GuestUsers getGuestUser() { return guestUser; }
    public LocalDateTime getPredDate() { return predDate; }
    public float getScreenTime() { return screenTime; }
    public float getWorkScreen() { return workScreen; }
    public float getLeisureScreen() { return leisureScreen; }
    public float getSleepHours() { return sleepHours; }
    public int getSleepQuality() { return sleepQuality; }
    public float getStressLevel() { return stressLevel; }
    public float getProductivity() { return productivity; }
    public int getExercise() { return exercise; }
    public float getSocial() { return social; }
    public float getMentalIndex() { return mentalIndex; }

    // Setters
    public void setPredId(UUID predId) { this.predId = predId; }
    public void setUser(Users user) { this.user = user; }
    public void setGuestUser(GuestUsers guestUser) { this.guestUser = guestUser; }
    public void setPredDate(LocalDateTime predDate) { this.predDate = predDate; }
    public void setScreenTime(float screenTime) { this.screenTime = screenTime; }
    public void setWorkScreen(float workScreen) { this.workScreen = workScreen; }
    public void setLeisureScreen(float leisureScreen) { this.leisureScreen = leisureScreen; }
    public void setSleepHours(float sleepHours) { this.sleepHours = sleepHours; }
    public void setSleepQuality(int sleepQuality) { this.sleepQuality = sleepQuality; }
    public void setStressLevel(float stressLevel) { this.stressLevel = stressLevel; }
    public void setProductivity(float productivity) { this.productivity = productivity; }
    public void setExercise(int exercise) { this.exercise = exercise; }
    public void setSocial(float social) { this.social = social; }
    public void setMentalIndex(float mentalIndex) { this.mentalIndex = mentalIndex; }
}
