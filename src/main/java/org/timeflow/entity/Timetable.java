package org.timeflow.entity;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "timetables")
public class Timetable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id")
    private Course course;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id")
    private Room room;

    @Enumerated(EnumType.STRING)
    private ActivityType type;

    @Enumerated(EnumType.STRING)
    private TimetableStatus status;

    // --- MODIFIED: Changed from Semester object to SemesterType enum ---
    @Enumerated(EnumType.STRING)
    @Column(name = "semester")
    private SemesterType semester;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "specific_date")
    private LocalDate specificDate;

    private String description;

    public Timetable() {
    }


    public Timetable(Course course, DayOfWeek dayOfWeek, LocalTime startTime,
                     LocalTime endTime, Room room, ActivityType type, SemesterType semester) {
        this.course = course;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.type = type;
        this.semester = semester;
        this.status = TimetableStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }



    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public ActivityType getType() { return type; }
    public void setType(ActivityType type) { this.type = type; }
    public TimetableStatus getStatus() { return status; }
    public void setStatus(TimetableStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; }
    public LocalDate getSpecificDate() { return specificDate; }
    public void setSpecificDate(LocalDate specificDate) { this.specificDate = specificDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // --- MODIFIED: Getter and Setter for SemesterType ---
    public SemesterType getSemester() { return semester; }
    public void setSemester(SemesterType semester) { this.semester = semester; }
}