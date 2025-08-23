package org.timeflow.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.NaturalId;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "semesters")
public class Semester {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NaturalId
    private String name;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "academic_year")
    private String academicYear;

    @Column(name = "is_current")
    private boolean isCurrent;

    @OneToMany(mappedBy = "semester", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Timetable> timetables;

    // Constructors
    public Semester() {
    }

    public Semester(String name, LocalDate startDate, LocalDate endDate, String academicYear) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.academicYear = academicYear;
        this.isCurrent = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    public List<Timetable> getTimetables() {
        return timetables;
    }

    public void setTimetables(List<Timetable> timetables) {
        this.timetables = timetables;
    }

    @Override
    public String toString() {
        return name + " (" + academicYear + ")";
    }
}
