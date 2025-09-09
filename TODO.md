# Hibernate 17.1.0 Migration TODO

## Phase 1: Update Dependencies and Configuration

- [ ] Update pom.xml to Hibernate 17.1.0.Final
- [ ] Update JPA API to Jakarta EE 3.1.0
- [ ] Update hibernate.cfg.xml configuration
- [ ] Update HibernateUtil for modern Hibernate

## Phase 2: Update Entity Classes

- [ ] Update jakarta.persistence imports to jakarta.persistence
- [ ] Verify all entity annotations compatibility
- [ ] Update any deprecated annotations

## Phase 3: Update DAO Implementation

- [ ] Update BaseDAO with modern patterns
- [ ] Update all DAO classes to use new approach
- [ ] Implement proper transaction management
- [ ] Add EntityManager support

## Phase 4: Testing and Validation

- [ ] Test all CRUD operations
- [ ] Verify transaction management
- [ ] Check performance improvements
