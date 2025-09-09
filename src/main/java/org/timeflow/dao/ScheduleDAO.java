package org.timeflow.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.timeflow.entity.Timetable;
import org.timeflow.entity.Department;
import org.timeflow.entity.User;
import org.timeflow.entity.Semester;
import org.timeflow.entity.Conflict;
import org.timeflow.entity.ConflictType;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public class ScheduleDAO extends BaseDAO<Timetable, Long> {
    public ScheduleDAO() {
        super(Timetable.class);
    }

    public List<Timetable> findByDepartment(Department department, Semester semester) {
        return executeQuery(
                "FROM Timetable t WHERE t.course.department = :department AND t.semester = :semester",
                Timetable.class,
                query -> {
                    query.setParameter("department", department);
                    query.setParameter("semester", semester);
                }
        );
    }

    public List<Timetable> findByLecturer(User lecturer, Semester semester) {
        return executeQuery(
                "FROM Timetable t WHERE t.course.lecturer = :lecturer AND t.semester = :semester",
                Timetable.class,
                query -> {
                    query.setParameter("lecturer", lecturer);
                    query.setParameter("semester", semester);
                }
        );
    }

    public List<Timetable> findConflicts(Timetable newTimetable) {
        LocalDate specificDate = newTimetable.getSpecificDate();
        if (specificDate != null) {
            // Check for specific date conflicts (e.g., exams)
            return executeQuery(
                    "FROM Timetable t WHERE t.semester = :semester AND t.specificDate = :specificDate " +
                            "AND t.startTime = :startTime AND t.room = :room AND t.id != :id",
                    Timetable.class,
                    query -> {
                        query.setParameter("semester", newTimetable.getSemester());
                        query.setParameter("specificDate", specificDate);
                        query.setParameter("startTime", newTimetable.getStartTime());
                        query.setParameter("room", newTimetable.getRoom());
                        query.setParameter("id", newTimetable.getId() != null ? newTimetable.getId() : -1L);
                    }
            );
        } else {
            // Check for recurring conflicts (e.g., lectures)
            return executeQuery(
                    "FROM Timetable t WHERE t.semester = :semester AND t.dayOfWeek = :dayOfWeek " +
                            "AND t.startTime = :startTime AND t.room = :room AND t.id != :id",
                    Timetable.class,
                    query -> {
                        query.setParameter("semester", newTimetable.getSemester());
                        query.setParameter("dayOfWeek", newTimetable.getDayOfWeek());
                        query.setParameter("startTime", newTimetable.getStartTime());
                        query.setParameter("room", newTimetable.getRoom());
                        query.setParameter("id", newTimetable.getId() != null ? newTimetable.getId() : -1L);
                    }
            );
        }
    }

    public void createConflict(Timetable timetable1, Timetable timetable2, ConflictType type) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Conflict conflict = new Conflict(timetable1, timetable2, type);
            session.persist(conflict);
            session.getTransaction().commit();
            logger.info("Conflict created between timetables {} and {}", timetable1.getId(), timetable2.getId());
        } catch (Exception e) {
            logger.error("Failed to create conflict: {}", e.getMessage(), e);
        }
    }

    private <T> List<T> executeQuery(String hql, Class<T> resultClass, Consumer<Query<T>> paramSetter) {
        try (Session session = sessionFactory.openSession()) {
            Query<T> query = session.createQuery(hql, resultClass);
            paramSetter.accept(query);
            return query.list();
        }
    }
}