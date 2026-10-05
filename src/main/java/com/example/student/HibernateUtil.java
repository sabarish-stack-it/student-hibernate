package com.example.student;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public final class HibernateUtil {
    private HibernateUtil() { }

    public static SessionFactory buildSessionFactory() {
        String password = System.getenv("DB_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Set DB_PASSWORD before running the application.");
        }
        return new Configuration()
                .addAnnotatedClass(Student.class)
                .setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver")
                .setProperty("hibernate.connection.url", env("DB_URL",
                        "jdbc:mysql://localhost:3306/student_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"))
                .setProperty("hibernate.connection.username", env("DB_USER", "student_app"))
                .setProperty("hibernate.connection.password", password)
                .setProperty("hibernate.hbm2ddl.auto", "validate")
                .setProperty("hibernate.show_sql", "true")
                .setProperty("hibernate.format_sql", "true")
                .buildSessionFactory();
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }
}
