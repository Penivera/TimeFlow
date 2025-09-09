package org.timeflow.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.timeflow.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

public abstract class BaseDAO<T, ID extends Serializable> {

    public static final Logger logger = LoggerFactory.getLogger(BaseDAO.class);
    private final Class<T> entityClass;
    protected SessionFactory sessionFactory;

    public BaseDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    // Create operation
    public T save(T entity) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(entity);
            transaction.commit();
            logger.info("Entity saved successfully: {}", entity.getClass().getSimpleName());
            return entity;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving entity: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save entity", e);
        }
    }

    // Read operation
    public T findById(ID id) {
        try (Session session = sessionFactory.openSession()) {
            return session.find(entityClass, id);
        } catch (Exception e) {
            logger.error("Error finding entity by id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to find entity", e);
        }
    }

    // Update operation
    public T update(T entity) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.merge(entity);
            transaction.commit();
            logger.info("Entity updated successfully: {}", entity.getClass().getSimpleName());
            return entity;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error updating entity: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update entity", e);
        }
    }

    // Fixed findAll to return List<T>
    public List<T> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM " + entityClass.getSimpleName(), entityClass)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error finding all entities: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find all entities", e);
        }
    }

    // --- START: ADD THESE TWO METHODS ---

    // Delete operation
    public void delete(T entity) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.remove(entity);
            transaction.commit();
            logger.info("Entity deleted successfully: {}", entity.getClass().getSimpleName());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting entity: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete entity", e);
        }
    }

    // Delete by ID operation
    public void deleteById(ID id) {
        T entity = findById(id);
        if (entity != null) {
            delete(entity);
        } else {
            logger.warn("Attempted to delete non-existent entity with id: {}", id);
        }
    }

    // --- END: ADD THESE TWO METHODS ---
}