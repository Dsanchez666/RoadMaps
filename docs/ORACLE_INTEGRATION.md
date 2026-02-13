# Oracle Database Integration Guide

## Overview

This guide provides detailed instructions for setting up and using Oracle Database with the ROADMAP MVP application.

## Prerequisites

### Oracle Database
- Oracle Database 19c or higher (11g, 12c, 18c, 19c, 21c, 23c supported)
- Oracle listener running and configured
- Database instance created and accessible

### Java Environment
- Java 17 or higher
- Oracle JDBC Driver (`ojdbc11`) - automatically downloaded by Maven

### Network Access
- Direct TCP/IP connection to Oracle server (port 1521 by default)
- Firewall rules allowing the connection
- Network alias or TNS entry (optional but recommended)

## Installation Steps

### Step 1: Install Oracle Database

#### Option A: Oracle Express Edition (Free)
```bash
# Download from: https://www.oracle.com/database/technologies/xe-downloads.html
# Installation varies by OS, typically:
# Windows: Run installer wizard
# Linux: Follow installation guide
```

#### Option B: Docker (Quick Setup)
```bash
docker pull container-registry.oracle.com/database/express:latest

docker run -d \
  -p 1521:1521 \
  -e ORACLE_PASSWORD=oracle \
  --name oracle_express \
  container-registry.oracle.com/database/express:latest

# Wait for initialization (5-10 minutes)
# Then connect: sqlplus system/oracle@localhost:1521/XE
```

#### Option C: Oracle Cloud Database
- Set up Oracle Autonomous Database Cloud
- Get connection details from cloud console

### Step 2: Verify Oracle Connectivity

```bash
# Using SQL*Plus
sqlplus system/oracle@localhost:1521:ORCL

# Verify database is up
SQL> SELECT NAME FROM v$database;
```

### Step 3: Create Application Schema (Optional)

```bash
# Connect as SYSTEM
sqlplus system/oracle@ORCL

# Run the Oracle setup script
SQL> @oracle_setup.sql

# Verify tables created
SQL> SELECT table_name FROM user_tables;
```

### Step 4: Build Backend with Oracle Support

```bash
cd backend
mvn clean install
```

Maven will automatically download the Oracle JDBC driver (`ojdbc11`).

## Configuration Methods

### Method 1: Programmatic Configuration (Java)

#### Direct Method Call
```java
import com.example.roadmap.MySQLConnection;

Connection conn = MySQLConnection.connectToOracle(
    "oracle-server.company.com",  // host
    "1521",                        // port
    "ORCL",                        // SID
    "system",                      // username
    "oracle"                       // password
);

if (MySQLConnection.isConnected()) {
    System.out.println("Connected: " + MySQLConnection.getConnectionInfo());
}
```

#### Using DatabaseConnection Class
```java
import com.example.roadmap.DatabaseConnection;

DatabaseConnection.setOracleParams(
    "oracle-server.company.com",
    "1521",
    "ORCL",
    "system",
    "oracle"
);
DatabaseConnection.setDatabaseType("ORACLE");

Connection conn = DatabaseConnection.connect();

// Use the connection
try (Statement stmt = conn.createStatement()) {
    ResultSet rs = stmt.executeQuery("SELECT 1 FROM dual");
    if (rs.next()) {
        System.out.println("Oracle connection working!");
    }
} finally {
    DatabaseConnection.disconnect();
}
```

### Method 2: REST API Configuration

#### Connect to Oracle
```bash
curl -X POST "http://localhost:8080/api/database/connect/oracle" \
  -d "host=oracle-server.company.com" \
  -d "port=1521" \
  -d "sid=ORCL" \
  -d "user=system" \
  -d "password=oracle"
```

#### Response
```json
{
  "status": "SUCCESS",
  "message": "Successfully connected to Oracle database",
  "type": "ORACLE",
  "connectionUrl": "jdbc:oracle:thin:@oracle-server.company.com:1521:ORCL",
  "host": "oracle-server.company.com",
  "port": "1521",
  "sid": "ORCL"
}
```

#### Check Status
```bash
curl -X GET "http://localhost:8080/api/database/status"
```

### Method 3: Spring Boot Configuration Properties

Create `application-oracle.properties`:

```properties
# Oracle Database Configuration
spring.datasource.url=jdbc:oracle:thin:@oracle-server.company.com:1521:ORCL
spring.datasource.username=system
spring.datasource.password=oracle
spring.datasource.driver-class-name=oracle.jdbc.driver.OracleDriver

# Connection Pool Settings
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5

# Logging
logging.level.oracle.jdbc=DEBUG
logging.level.sql.jdbc=DEBUG

# JPA/Hibernate Settings (if using)
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.database-platform=org.hibernate.dialect.Oracle10gDialect
```

Run with profile:
```bash
java -Dspring.profiles.active=oracle -jar target/roadmap-backend.jar
```

## Oracle Connection Parameters Reference

| Parameter | Description | Example | Required |
|-----------|-------------|---------|----------|
| **host** | Oracle server hostname or IP | `oracle-server.company.com` or `192.168.1.10` | Yes |
| **port** | Oracle TNS listener port | `1521` | Yes (default: 1521) |
| **sid** | System Identifier (database name) | `ORCL`, `PROD`, `DEV`, `XE` | Yes |
| **user** | Oracle username | `system`, `dba_user` | Yes |
| **password** | Oracle user password | `oracle123` | Yes |

## Oracle Connection String Formats

### Standard SID Connection
```
jdbc:oracle:thin:@oracle-server:1521:ORCL
```

### Service Name Connection
```
jdbc:oracle:thin:@oracle-server:1521/SERVICE_NAME
```

### TNS Connection String
```
jdbc:oracle:oci:@TNSNAME
```

## Common Oracle SIDs and Service Names

| Type | Default Values | Notes |
|------|---|---|
| **Express Edition (XE)** | SID: `XE`<br>Service: `XE` | Free, limited version |
| **Standard Edition 2** | SID: `ORCL`<br>Service: `ORCL` | Standard installation |
| **Enterprise Edition** | Custom SID | Configurable during install |
| **Autonomous Database** | Cloud default | Check cloud console |

## Troubleshooting Oracle Connections

### Issue 1: "No suitable driver found"
**Error Message:**
```
java.sql.SQLException: No suitable driver found for jdbc:oracle:thin:@...
```

**Solutions:**
1. Run `mvn clean install` to download JDBC driver
2. Check `pom.xml` includes `ojdbc11` dependency
3. Verify classpath includes Oracle JDBC JAR

### Issue 2: "TNS listener could not resolve SERVICE_NAME"
**Error Message:**
```
ORA-12514: TNS:listener could not resolve SERVICE_NAME in connect descriptor
```

**Causes & Solutions:**
- **Wrong SID**: Verify SID with Oracle DBA
  ```sql
  SELECT NAME FROM v$database;
  ```
- **Listener not running**: 
  ```bash
  lsnrctl status
  lsnrctl start
  ```
- **Wrong host/port**: Check network connectivity
  ```bash
  telnet oracle-server 1521
  ```

### Issue 3: "Connection refused"
**Error Message:**
```
java.net.ConnectException: Connection refused
```

**Solutions:**
- Verify server is running: `lsnrctl status`
- Verify host and port are correct
- Check firewall allows port 1521
- Verify network connectivity: `ping oracle-server`

### Issue 4: "Invalid username/password"
**Error Message:**
```
ORA-01017: invalid username/password; logon denied
```

**Solutions:**
- Verify username and password are correct
- Check user account is unlocked: `ALTER USER system ACCOUNT UNLOCK;`
- Verify user has appropriate role/privileges
- Note: Default SYSTEM password for Express Edition is often `oracle`

### Issue 5: "Character set mismatch"
**Error Message:**
```
ORA-12514 or character encoding issues
```

**Solution:**
Add connection properties:
```java
Properties props = new Properties();
props.put("oracle.jdbc.defaultNChar", "false");
props.put("oracle.jdbc.autoCommitSpecified", "true");
Connection conn = DriverManager.getConnection(url, props);
```

## Performance Optimization

### Connection Pooling
For production, use HikariCP with Oracle:

Add to `pom.xml`:
```xml
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.0.1</version>
</dependency>
```

Configuration:
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.connection-timeout=30000
```

### Query Performance
Enable query tracing:
```sql
-- As SYSTEM user
ALTER SESSION SET sql_trace=true;
```

## Security Best Practices

### 1. Use Environment Variables
```java
String host = System.getenv("ORACLE_HOST");
String password = System.getenv("ORACLE_PASSWORD");

DatabaseConnection.setOracleParams(host, "1521", "ORCL", "system", password);
```

### 2. Use a Limited Privilege User
```sql
-- Create dedicated application user
CREATE USER app_user IDENTIFIED BY secure_password;

-- Grant minimal required privileges
GRANT CREATE SESSION TO app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON roadmap TO app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON iniciativas TO app_user;
```

### 3. Use SSL/TLS for Remote Connections
```
jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcps)(HOST=oracle-server)(PORT=2484))(CONNECT_DATA=(SERVICE_NAME=ORCL))(SECURITY=(SSL_SERVER_CERT_DN="CN=oracle-server")))
```

### 4. Store Credentials Securely
- Use environment variables
- Use configuration management tools
- Never hardcode passwords
- Use Oracle Wallet for advanced security

## Migration from MySQL to Oracle

### Data Type Mapping

| MySQL | Oracle | Notes |
|-------|--------|-------|
| `VARCHAR2` | `VARCHAR2(size)` | Specify size |
| `INT` | `NUMBER(10)` | Use NUMBER type |
| `DATETIME` | `TIMESTAMP` | Better precision |
| `TEXT` | `VARCHAR2(4000)` or `CLOB` | VARCHAR2 max 4000 |
| `BLOB` | `BLOB` | Same |

### SQL Syntax Differences

```sql
-- MySQL
LIMIT 10
AUTO_INCREMENT
IFNULL(column, 0)

-- Oracle
FETCH FIRST 10 ROWS ONLY
Oracle Sequence (manual increment)
NVL(column, 0)
```

### Example Migration Script

See `oracle_setup.sql` in `/backend/database/` for complete schema migration.

## Testing Oracle Connectivity

### Test Connection
```bash
# Linux/Mac
sqlplus system/oracle@oracle-server:1521:ORCL

# Windows
sqlplus system/oracle@oracle-server:1521:ORCL
```

### Verify with Java
```bash
cd backend
mvn test
# Check logs for Oracle connection success
```

### Test via REST API
```bash
# Connect
curl -X POST "http://localhost:8080/api/database/connect/oracle" \
  -d "host=localhost&port=1521&sid=ORCL&user=system&password=oracle"

# Check status
curl -X GET "http://localhost:8080/api/database/status"

# Create test record
curl -X POST "http://localhost:8080/api/roadmaps" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Roadmap","description":"Test"}'
```

## Additional Resources

- [Oracle JDBC Documentation](https://docs.oracle.com/cd/E11882_01/java.112/e16548/toc.htm)
- [Oracle Database Installation Guide](https://docs.oracle.com/en/database/oracle/oracle-database/21/install-and-upgrade.html)
- [OJDBC Driver Release Notes](https://docs.oracle.com/en/database/oracle/oracle-database/23/jajdb/release-changes.html)
- [Oracle Autonomous Database](https://www.oracle.com/autonomous-database/)
- [Oracle Docker Images](https://hub.docker.com/_/oracle-database-enterprise-edition)

## Support and Troubleshooting

For issues:
1. Check [DATABASE_CONFIGURATION.md](./DATABASE_CONFIGURATION.md)
2. Review Oracle error codes in Oracle documentation
3. Enable debug logging:
   ```properties
   logging.level.oracle.jdbc=DEBUG
   logging.level.oracle.jdbc.driver=DEBUG
   ```

## Example Complete Setup

### Quick Setup with Docker
```bash
# 1. Start Oracle
docker run -d -p 1521:1521 \
  -e ORACLE_PASSWORD=oracle \
  --name oracle-roadmap \
  container-registry.oracle.com/database/express:latest

# 2. Wait 5-10 minutes for initialization

# 3. Create tables
docker exec -i oracle-roadmap sh -c 'sqlplus -S system/oracle@localhost:1521:XE' < oracle_setup.sql

# 4. Build backend
mvn clean install

# 5. Run with Oracle
java -cp target/roadmap-backend.jar \
  -Doracle.host=localhost \
  -Doracle.port=1521 \
  -Doracle.sid=XE \
  -Doracle.user=system \
  -Doracle.password=oracle \
  com.example.roadmap.Application
```

### Connect from Application
```bash
curl -X POST "http://localhost:8080/api/database/connect/oracle" \
  -d "host=localhost&port=1521&sid=XE&user=system&password=oracle"
```

## Next Steps

1. Set up Oracle Database (local or cloud)
2. Run `oracle_setup.sql` to create schema
3. Configure connection parameters
4. Build and run the application
5. Test via REST API endpoints
