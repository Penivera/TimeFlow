package org.timeflow.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String code;

    private int credits;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lecturer_id")
    private User lecturer;

    @Column(name = "academic_level")
    private int level;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Timetable> timetables;

    // Constructors
    public Course() {
    }

    public Course(String name, String code, int credits, Department department, User lecturer, int level) {
        this.name = name;
        this.code = code;
        this.credits = credits;
        this.department = department;
        this.lecturer = lecturer;
        this.level = level;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public User getLecturer() {
        return lecturer;
    }

    public void setLecturer(User lecturer) {
        this.lecturer = lecturer;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<Timetable> getTimetables() {
        return timetables;
    }

    public void setTimetables(List<Timetable> timetables) {
        this.timetables = timetables;
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
