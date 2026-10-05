package com.example.student;

import java.util.UUID;
import java.util.function.Function;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public final class App {
    private App() { }

    public static void main(String[] args) {
        // A unique email allows repeated demonstrations without deleting earlier records.
        String email = "student." + UUID.randomUUID() + "@example.com";
        Student student = new Student("Sample Student", email, "Java Fundamentals");

        try (SessionFactory factory = HibernateUtil.buildSessionFactory()) {
            Long id = inTransaction(factory, session -> {
                session.persist(student);
                session.flush();
                return student.getId();
            });

            // Fresh sessions ensure verification reads the committed database record.
            Student inserted = inTransaction(factory, session -> session.find(Student.class, id));
            verify(inserted, id, student.getName(), email, "Java Fundamentals");
            System.out.println("INSERT VERIFIED: " + inserted);

            inTransaction(factory, session -> {
                Student managed = session.find(Student.class, id);
                if (managed == null) throw new IllegalStateException("Student was not found: " + id);
                managed.setCourse("Hibernate with MySQL");
                // Hibernate dirty checking writes UPDATE when the transaction commits.
                return null;
            });

            Student updated = inTransaction(factory, session -> session.find(Student.class, id));
            verify(updated, id, student.getName(), email, "Hibernate with MySQL");
            System.out.println("UPDATE VERIFIED: " + updated);
            System.out.println("SUCCESS: Insert and update verified in MySQL.");
            System.out.println("Check in MySQL: SELECT id, name, email, course FROM students WHERE id = " + id + ";");
        }
    }

    private static void verify(Student actual, Long id, String name, String email, String course) {
        if (actual == null || !id.equals(actual.getId()) || !name.equals(actual.getName())
                || !email.equals(actual.getEmail()) || !course.equals(actual.getCourse())) {
            throw new IllegalStateException("Database verification failed for student " + id);
        }
    }

    private static <T> T inTransaction(SessionFactory factory, Function<Session, T> work) {
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                T result = work.apply(session);
                transaction.commit();
                return result;
            } catch (RuntimeException failure) {
                try {
                    if (transaction.isActive()) transaction.rollback();
                } catch (RuntimeException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }
                throw failure;
            }
        }
    }
}
