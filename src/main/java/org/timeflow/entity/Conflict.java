package org.timeflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conflicts")
public class Conflict {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "timetable_id_1")
    private Timetable timetable1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "timetable_id_2")
    private Timetable timetable2;

    @Enumerated(EnumType.STRING)
    private ConflictStatus status;

    @Column(name = "resolution_notes")
    private String resolutionNotes;

    @Column(name = "detected_at")
    private LocalDateTime detectedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Enumerated(EnumType.STRING)
    private ConflictType type;

    // Constructors
    public Conflict() {
    }

    public Conflict(Timetable timetable1, Timetable timetable2, ConflictType type) {
        this.timetable1 = timetable1;
        this.timetable2 = timetable2;
        this.type = type;
        this.status = ConflictStatus.DETECTED;
        this.detectedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timetable getTimetable1() {
        return timetable1;
    }

    public void setTimetable1(Timetable timetable1) {
        this.timetable1 = timetable1;
    }

    public Timetable getTimetable2() {
        return timetable2;
    }

    public void setTimetable2(Timetable timetable2) {
        this.timetable2 = timetable2;
    }

    public ConflictStatus getStatus() {
        return status;
    }

    public void setStatus(ConflictStatus status) {
        this.status = status;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public User getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(User resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public ConflictType getType() {
        return type;
    }

    public void setType(ConflictType type) {
        this.type = type;
    }
}
