package com.unlp.payments.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "petri_net_state")
public class PetriNetStateEntity {

    @Id
    private String id;

    @Column(columnDefinition = "TEXT")
    private String currentMarking;

    @Column(columnDefinition = "TEXT")
    private String uuidCurrentMarking;

    @Column(columnDefinition = "TEXT")
    private String enabledTransitions;

    @Column(columnDefinition = "TEXT")
    private String timedEnablingTimes;

    @Column(columnDefinition = "TEXT")
    private String timedUuidEnablingTimes;

    private LocalDateTime updatedAt;

    public PetriNetStateEntity() {}

    public PetriNetStateEntity(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCurrentMarking() { return currentMarking; }
    public void setCurrentMarking(String currentMarking) { this.currentMarking = currentMarking; }

    public String getUuidCurrentMarking() { return uuidCurrentMarking; }
    public void setUuidCurrentMarking(String uuidCurrentMarking) { this.uuidCurrentMarking = uuidCurrentMarking; }

    public String getEnabledTransitions() { return enabledTransitions; }
    public void setEnabledTransitions(String enabledTransitions) { this.enabledTransitions = enabledTransitions; }

    public String getTimedEnablingTimes() { return timedEnablingTimes; }
    public void setTimedEnablingTimes(String timedEnablingTimes) { this.timedEnablingTimes = timedEnablingTimes; }

    public String getTimedUuidEnablingTimes() { return timedUuidEnablingTimes; }
    public void setTimedUuidEnablingTimes(String timedUuidEnablingTimes) { this.timedUuidEnablingTimes = timedUuidEnablingTimes; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
