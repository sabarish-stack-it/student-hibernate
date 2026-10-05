# Student Hibernate Application

A Maven-based Java console application that maps a `Student` entity to a MySQL table, inserts a student, updates the course, and verifies both committed changes by reading the record through fresh Hibernate sessions.

## Requirements

- JDK 17
- Maven 3.9 or later (`mvn -version` should report Java 17 or later)
- MySQL Server 8.0 or 8.4, running on localhost port 3306

Dependencies are pinned in `pom.xml`: Hibernate ORM 6.6.0.Final and MySQL Connector/J 8.4.0. Maven downloads them on the first build.

## Project structure

```text
student-hibernate/
  pom.xml
  README.md
  .gitignore
  .github/workflows/verify.yml
  sql/schema.sql
  sql/verify.sql
  src/main/java/com/example/student/
    Student.java
    HibernateUtil.java
    App.java
```

## 1. Create the database and table

Open a terminal in this project's folder and connect as a MySQL administrator:

```sh
mysql -u root -p
```

Run the following in the MySQL prompt. Replace the password placeholder with a password you choose; do not upload your actual password to GitHub.

```sql
SOURCE sql/schema.sql;
CREATE USER IF NOT EXISTS 'student_app'@'localhost' IDENTIFIED BY 'REPLACE_WITH_YOUR_PASSWORD';
GRANT SELECT, INSERT, UPDATE ON student_db.* TO 'student_app'@'localhost';
EXIT;
```

If using MySQL Workbench, execute `sql/schema.sql` in a query tab, then execute the `CREATE USER` and `GRANT` statements above. If the user already exists, use its existing password or change it through your administrator. `CREATE USER IF NOT EXISTS` does not reset an existing password.

## 2. Configure credentials

Windows PowerShell:

```powershell
$env:DB_USER = "student_app"
$env:DB_PASSWORD = "REPLACE_WITH_YOUR_PASSWORD"
```

macOS/Linux terminal:

```sh
export DB_USER='student_app'
export DB_PASSWORD='REPLACE_WITH_YOUR_PASSWORD'
```

Run the application in that same terminal. The default JDBC URL is:

```text
jdbc:mysql://localhost:3306/student_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

Set `DB_URL` if your host or port differs. The default connection options are for a local classroom database; use your provider's TLS-enabled URL for a remote database. The application reads environment variables directly; it does not load `.env` files.

## 3. Run

```sh
mvn clean compile exec:java
```

The application:

1. Opens Hibernate and validates the table against the entity mapping.
2. Creates a student with ID, name, email, and course.
3. Persists and commits the student.
4. Reads it in a fresh session and checks every field.
5. Changes the course and commits the update using Hibernate dirty checking.
6. Reads it in another fresh session and checks every field again.

Every run creates a new student with a unique example email. Earlier records are retained.

Expected output (illustrative, not a captured run):

```text
INSERT VERIFIED: Student{id=1, name='Sample Student', email='student.<unique-id>@example.com', course='Java Fundamentals'}
UPDATE VERIFIED: Student{id=1, name='Sample Student', email='student.<unique-id>@example.com', course='Hibernate with MySQL'}
SUCCESS: Insert and update verified in MySQL.
Check in MySQL: SELECT id, name, email, course FROM students WHERE id = 1;
```

Hibernate SQL and startup messages also appear. IDs and emails will vary. A verification failure throws an exception and makes the Maven command fail.

## 4. Verify directly in MySQL

```sh
mysql -u student_app -p student_db
```

```sql
SELECT id, name, email, course FROM students ORDER BY id DESC;
```

The newest row should show `Hibernate with MySQL`. To inspect the exact row, use the ID printed by the program. Capture the console's two verification messages and the MySQL query result if your instructor asks for evidence. Do not submit the illustrative output as real evidence.

## 5. Publish to GitHub

Create a **public** repository named `student-hibernate` at https://github.com/new (unless your course requires private access). For the commands below, create an empty repository without an automatically generated README, license, or .gitignore.

Open a terminal inside the extracted project folder:

```sh
git init
git add .
git commit -m "Add Maven Hibernate Student application"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/student-hibernate.git
git push -u origin main
```

Replace `YOUR_USERNAME` with your real GitHub username. Authenticate using GitHub's supported sign-in flow; never put a token in the repository URL or source files.

Alternatively use GitHub Desktop: add the project as a local repository, commit all project files including `.github/workflows/verify.yml`, and publish it. Make sure `pom.xml` and this README appear at the repository root. Upload the extracted files, not just the ZIP.

In the repository's **Actions** tab, open **Verify Hibernate against MySQL**. The workflow starts a fresh MySQL 8.4 service, creates the schema, compiles and runs the application, and checks the updated record with the MySQL client. Its passwords are disposable CI-only values, not personal database credentials. Wait for a green run before claiming verification. If Actions is disabled, enable it or run the project locally instead.

## 6. Submit in ByteXL

Copy the repository URL from your browser:

```text
https://github.com/YOUR_USERNAME/student-hibernate
```

Open the Week 4 Hibernate assignment, click **Submit**, and paste the URL into the project/repository link field if that is what the submission form requests. Open the URL while signed out to confirm an instructor can access a public repository. Follow any additional requirements displayed by the form.

This project is a console application. A GitHub repository publishes its source; GitHub Pages cannot run a Java/MySQL application. The supplied assignment screenshot does not show the submission form, so it does not establish whether ByteXL asks for another kind of link. If a deployed application URL is explicitly required, clarify that requirement with the instructor before submitting.

## Mapping and transaction notes

| Entity field | MySQL column | Mapping |
| --- | --- | --- |
| id | id BIGINT | Primary key, auto-increment |
| name | name VARCHAR(100) | Required |
| email | email VARCHAR(150) | Required, unique |
| course | course VARCHAR(100) | Required |

`@Entity` marks the persistent class, `@Table` selects `students`, and `@Id`/`@GeneratedValue` map the generated primary key. `session.persist()` inserts the entity. Changing a managed entity inside a transaction triggers an update on commit. Each operation uses a separate session; failed transactions are rolled back and resources are closed. The schema mode is `validate`, so Hibernate does not drop or silently recreate tables.

## Troubleshooting

- **mvn not found:** Install Maven and add its `bin` folder to PATH; reopen your terminal.
- **Wrong Java version:** Set `JAVA_HOME` to a JDK 17+ installation and check `mvn -version`.
- **Connection refused:** Start MySQL and check `DB_URL` and port 3306.
- **Access denied:** Check the MySQL username/password and grants. Set environment variables in the same terminal used for Maven.
- **Unknown database / missing table:** Run `sql/schema.sql` as an administrator first.
- **Schema validation error:** Compare the existing `students` table with `sql/schema.sql`; `CREATE TABLE IF NOT EXISTS` does not alter an existing table.

## Validation status

The generated files were checked for structure and configuration consistency. A local compile/database run was not possible in the preparation environment because a working JDK, Maven, and MySQL were unavailable. No successful GitHub Actions run is claimed until the repository has been published and that workflow has finished.

## References

- Hibernate getting started: https://docs.hibernate.org/orm/6.6/quickstart/html_single/
- Hibernate ORM user guide: https://docs.hibernate.org/orm/6.6/userguide/html_single/
- MySQL Connector/J Maven setup: https://dev.mysql.com/doc/connector-j/en/connector-j-installing-maven.html
