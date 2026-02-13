# MySQL Connection Configuration

## Host
localhost

## Port
3306

## Database User
root

## Database Name
roadmap_mvp

## Connection String Example
jdbc:mysql://localhost:3306/roadmap_mvp?useSSL=false&serverTimezone=UTC

## Setup Instructions

1. Install MySQL Server (if not already installed)
   - Download from: https://dev.mysql.com/downloads/mysql/
   
2. Start MySQL Service
   - On Windows: Services -> MySQL80 (or your version)
   - On Linux: sudo systemctl start mysql
   - On macOS: mysql.server start

3. Create the database and tables
   - Open MySQL command line or MySQL Workbench
   - Run the schema.sql script:
     mysql -u root -p < backend/database/schema.sql
     
4. Add MySQL JDBC Driver
   - Download mysql-connector-java-8.0.33.jar (or latest version)
   - Place in: backend/lib/mysql-connector-java.jar

## Troubleshooting

- Connection refused: Ensure MySQL service is running
- Access denied: Check username and password
- Database not found: Run schema.sql to create database and tables
