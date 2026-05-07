# Recruitment Setup, Configuration, and Run Guide

## 1. Software Overview
Edu-TA Recruitment is a JavaFX desktop application for international school teaching assistant recruitment. It supports job posting, student applications, resume upload, AI-assisted screening, account management, and administrative statistics.

The system has three roles: `APPLICANT`, `TEACHER`, and `ADMIN`. Each role sees a different navigation menu after login.

## 2. Environment Requirements

| Item | Requirement |
| --- | --- |
| **JDK** | Java 17 or later |
| **Build Tool** | Maven 3.8 or later |
| **UI Framework** | JavaFX 17.0.2, downloaded by Maven |
| **Operating System** | Windows, macOS, or Linux with a desktop environment |

## 3. Project Structure

| Path | Description |
| --- | --- |
| `src/main/java/com/qq/recruitment` | Java source code, including controllers, services, models, DAO, and utilities. |
| `src/main/resources/view` | JavaFX FXML views and CSS styles. |
| `src/main/resources/data` | JSON data files for users, jobs, applications, profiles, and favorites. |
| `src/main/resources/data/resumes` | Resume storage directory. Uploaded resumes are copied here. |
| `pom.xml` | Maven project configuration. |
| `target` | Maven build output directory. |

## 4. Initial Demo Accounts
The project includes demo accounts for all roles. Passwords are intentionally simple for demonstration: each password is the same as the username.

| Role | Username | Password | Purpose |
| --- | --- | --- | --- |
| `ADMIN` | `admin` | `admin` | View dashboard statistics and manage users and jobs. |
| `TEACHER` | `teacher` | `teacher` | Post and manage Java, Python, and Math-related jobs. |
| `TEACHER` | `teacher2` | `teacher2` | Post and manage English and robotics-related jobs. |
| `APPLICANT` | `student` | `student` | Computer Science applicant with sample applications. |
| `APPLICANT` | `student2` | `student2` | English Education applicant for language job demos. |
| `APPLICANT` | `student3` | `student3` | Engineering applicant for robotics and STEM demos. |

## 5. Setup Steps

### 5.1 Install Java 17
Install JDK 17 or later and configure `JAVA_HOME`. Verify the installation with:
```bash
java -version
```
The output should show Java version 17 or later.

### 5.2 Install Maven
Install Maven and make sure the `mvn` command is available from the terminal.
```bash
mvn -version
```

### 5.3 Open the Project Directory
Place the project on your local machine and open a terminal in the project root directory.

## 6. Configuration

### 6.1 Data Files
The application stores data in flat JSON files under `src/main/resources/data`.

| File | Purpose |
| --- | --- |
| `users.json` | Stores usernames, passwords, full names, and roles. |
| `jobs.json` | Stores job postings. |
| `applications.json` | Stores job application records. |
| `profiles.json` | Stores applicant profiles and workload settings. |
| `favorites.json` | Stores applicant favorite jobs. |

### 6.2 AI Service
The project includes the Volcengine Ark SDK for AI-assisted application analysis. If a real API key is not configured or the API call fails, the application can continue with a mock fallback result for demonstration purposes.

### 6.3 Resume Directory
Uploaded resumes are saved to the following directory. If the directory does not exist, the application creates it automatically.
```text
src/main/resources/data/resumes
```

## 7. Build and Run

### 7.1 Compile the Project
```bash
mvn clean compile
```

### 7.2 Run the JavaFX Application
```bash
mvn javafx:run
```
After the command starts successfully, the Edu-TA Recruitment login window will appear.

### 7.3 Run Tests
```bash
mvn test
```

### 7.4 Package the Application
```bash
mvn package
```

### 7.5 Run the Packaged JAR
```bash
java -jar target/edu-ta-1.0-SNAPSHOT.jar
```

## 8. Basic User Workflows

### 8.1 Administrator Workflow
* Log in with `admin` / `admin` and select the `ADMIN` role.
* Open **Dashboard** to view application status distribution, applications per job, TA workload distribution, and accepted counts.
* Open **Manage Accounts** to reset passwords, edit names, set applicant workload limits, and manage job status.

### 8.2 Teacher Workflow
* Log in with `teacher` / `teacher` or `teacher2` / `teacher2` and select the `TEACHER` role.
* Open **Post a Job** to create a new teaching assistant position.
* Open **Manage My Jobs** to edit job details or open and close jobs.
* Open **Screening** to review applications, check AI analysis, open resumes, and accept or reject candidates.

### 8.3 Applicant Workflow
* Log in with `student` / `student`, `student2` / `student2`, or `student3` / `student3` and select the `APPLICANT` role.
* Open **My Profile** to update major, student ID, skills, and bio.
* Open **Available Jobs** to browse jobs, favorite jobs, and apply with a resume file.
* Open **My Favorites** to view saved jobs.
* Open **My Applications** to view application status, withdraw pending applications, or update resumes.

## 9. Common Issues

### 9.1 Maven Cannot Download Dependencies
Check the network connection and Maven mirror configuration. The first build downloads JavaFX, Jackson, JUnit, and the Volcengine SDK.

### 9.2 Login Fails
Make sure the username, password, and selected role match. For example, the admin account uses username `admin`, password `admin`, and role `ADMIN`.

### 9.3 Application Submission Fails
Possible reasons include duplicate application, full job capacity, full applicant workload, or missing resume file.

### 9.4 Teacher Cannot See Some Applications
Teachers can only screen applications for jobs they posted. Administrators can view broader system statistics and management data.

### 9.5 Editing Demo Data
You may edit JSON files under `src/main/resources/data`. Keep usernames, job IDs, and application records consistent.

## 10. Command Summary

| Command | Description |
| --- | --- |
| `mvn clean compile` | Clean and compile the project. |
| `mvn javafx:run` | Run the JavaFX desktop application. |
| `mvn test` | Run automated tests. |
| `mvn package` | Build the packaged JAR file. |
| `java -jar target/edu-ta-1.0-SNAPSHOT.jar` | Run the packaged application. |
