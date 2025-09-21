# Library-Management-System
This project is a Library Management System built with Java, Spring Boot, and a relational database as part of the CODE81 challenge.

## 🚀 Features

### Core Features
- **Books**: Manage book inventory and availability
- **System User**: Admin and staff user management
- **Members**: Handle member registrations and profiles
- **Borrowing**: Track book checkouts and returns

### Additional Services
- **Authors**: Manage author information
- **Publishers**: Track publisher details
- **Categories**: Organize books by categories
- **Security**: Secure authentication and authorization

## 🛠️ Tech Stack

- **Backend**: Java 21, Spring Boot 3.5.5
- **Database**: MySQL 
- **Build Tool**: Maven

## 🚀 Getting Started

1. **Clone and setup**
   ```bash  
   git clone [(https://github.com/abdelrahman998/Library-Management-System.git)]  
   cd Library-Management-System
   ```
2. **Create the database**
Make sure MySQL is running, then create the database manually:
   ```SQL
   CREATE DATABASE library_db;
   ```
3. **Configure the database** Create or update src/main/resources/application.properties
```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/library_db?createDatabaseIfNotExist=true&useSSL=false
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Hibernate Properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```
4. **Run the application**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
