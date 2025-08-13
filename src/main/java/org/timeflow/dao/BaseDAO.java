package org.timeflow.dao;

import org.timeflow.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
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
            session.save(entity);
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
            return session.get(entityClass, id);
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
            session.update(entity);
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

    // Delete operation
    public void delete(T entity) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.delete(entity);
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

    // Find all entities
    public List<T> findAll() {
        try (Session session = sessionFactory.openSession()) {
            Query<T> query = session.createQuery("FROM " + entityClass.getName(), entityClass);
            return query.list();
        } catch (Exception e) {
            logger.error("Error finding all entities: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find entities", e);
        }
    }

    // Execute HQL query
    protected List<T> executeQuery(String hql, Object... params) {
        try (Session session = sessionFactory.openSession()) {
            Query<T> query = session.createQuery(hql, entityClass);
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i, params[i]);
            }
            return query.list();
        } catch (Exception e) {
            logger.error("Error executing query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute query", e);
        }
    }

    // Execute single result query
    protected T executeUniqueQuery(String hql, Object... params) {
        try (Session session = sessionFactory.openSession()) {
            Query<T> query = session.createQuery(hql, entityClass);
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i, params[i]);
            }
            return query.uniqueResult();
        } catch (Exception e) {
            logger.error("Error executing unique query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute query", e);
        }
    }
}
