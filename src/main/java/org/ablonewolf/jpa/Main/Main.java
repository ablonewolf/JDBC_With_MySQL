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
            Artist newArtist = new Artist("Muddy Water");
            entityManager.persist(newArtist);
//          here, the id of the latest added row is 209. Hence, I passed this value.
//          In other DB, this value might be different.
            Artist artist = entityManager.find(Artist.class, newArtist.getId());
            System.out.println("Artist name: " + artist.getName());
            entityManager.remove(artist);
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
