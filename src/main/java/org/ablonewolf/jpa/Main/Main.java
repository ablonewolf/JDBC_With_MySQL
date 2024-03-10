package org.ablonewolf.jpa.Main;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.ablonewolf.jpa.entities.Artist;

public class Main {
    public static void main(String[] args) {
        EntityManager entityManager = null;
        EntityTransaction transaction = null;

        try {
            // Create EntityManager and start transaction
            var sessionFactory = Persistence.createEntityManagerFactory("org.ablonewolf.jpa.entities");
            entityManager = sessionFactory.createEntityManager();
            transaction = entityManager.getTransaction();
            transaction.begin();

            // Perform database operations (e.g., persisting entities)
            entityManager.persist(new Artist("Muddy Water"));

            // Commit the transaction if everything is successful
            transaction.commit();
        } catch (Exception e) {
            // Rollback the transaction if an exception occurs
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            // Close the EntityManager in the finally block to ensure proper cleanup
            if (entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }
}
