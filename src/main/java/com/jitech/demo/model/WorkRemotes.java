package com.jitech.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "work_remotes")
public class WorkRemotes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_rmt_id")
    private int workRmtId;

    @NotNull
    @Column(name = "work_rmt_name", length = 100, unique = true, nullable = false)
    private String workRmtName;

    public WorkRemotes() {}

    public int getWorkRmtId() {return workRmtId;}
    public void setWorkRmtId(int workRmtId) {this.workRmtId = workRmtId;}
    public String getWorkRmtName() {return workRmtName;}
    public void setWorkRmtName(String workRmtName) {this.workRmtName = workRmtName;}
}
