package org.timeflow.dao;

import org.timeflow.entity.Room;
import org.hibernate.Session;
import java.util.List;

public class RoomDAO extends BaseDAO<Room, Long> {

    public RoomDAO() {
        super(Room.class);
    }

    @Override
    public List<Room> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Room r ORDER BY r.id", Room.class).getResultList();
        }
    }
}