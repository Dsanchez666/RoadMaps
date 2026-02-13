# Database Connection Configuration Guide

## Overview
The application now supports both **MySQL** and **Oracle** databases using JDBC. You can switch between them dynamically by configuring the appropriate connection parameters.

## MySQL Connection

### Default Configuration
```java
import com.example.roadmap.MySQLConnection;

// Set MySQL connection parameters (optional - defaults are shown)
MySQLConnection.setConnectionParams(
    "localhost",              // host
    "3306",                   // port
    "roadmap_mvp",            // database
    "root",                   // username
    ""                        // password
);

// Connect to MySQL database
Connection conn = MySQLConnection.connect();

// Check if connected
if (MySQLConnection.isConnected()) {
    System.out.println("Connected successfully!");
    System.out.println(MySQLConnection.getConnectionInfo());
}

// Close connection when done
MySQLConnection.disconnect();
```

### XML Configuration Alternative
If using Spring Boot configuration files, you can set properties via `application.properties`:

```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/roadmap_mvp
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

## Oracle Connection

### Using Direct Method
```java
import com.example.roadmap.MySQLConnection;

// Connect to Oracle database
Connection conn = MySQLConnection.connectToOracle(
    "oracle-server.example.com",  // host
    "1521",                         // port (default Oracle port)
    "ORCL",                         // SID (System Identifier)
    "system",                       // username
    "oracle"                        // password
);

// Check if connected
if (MySQLConnection.isConnected()) {
    System.out.println("Connected to Oracle successfully!");
    System.out.println(MySQLConnection.getConnectionInfo());
}

// Close connection when done
MySQLConnection.disconnect();
```

### Using Advanced Configuration
For more control, you can use the `DatabaseConnection` class directly:

```java
import com.example.roadmap.DatabaseConnection;

// Set Oracle parameters
DatabaseConnection.setOracleParams(
    "oracle-server.example.com",  // host
    "1521",                        // port
    "ORCL",                        // SID
    "system",                      // username
    "oracle"                       // password
);

// Set database type to Oracle
DatabaseConnection.setDatabaseType("ORACLE");

// Connect
Connection conn = DatabaseConnection.connect();

// Get connection info
System.out.println(DatabaseConnection.getDatabaseType()); // Output: ORACLE
System.out.println(DatabaseConnection.getConnectionInfo());
```

### Switching Databases at Runtime
```java
import com.example.roadmap.DatabaseConnection;

// Start with MySQL
DatabaseConnection.setDatabaseType("MYSQL");
DatabaseConnection.setMySQLParams("localhost", "3306", "roadmap_mvp", "root", "");
Connection mysqlConn = DatabaseConnection.connect();
// ... do work ...
DatabaseConnection.disconnect();

// Switch to Oracle
DatabaseConnection.setOracleParams("oracle-host", "1521", "ORCL", "system", "oracle");
DatabaseConnection.setDatabaseType("ORACLE");
Connection oracleConn = DatabaseConnection.connect();
// ... do work ...
DatabaseConnection.disconnect();
```

## Oracle Connection Parameters Explained

| Parameter | Description | Example |
|-----------|-------------|---------|
| **host** | Oracle database server hostname or IP address | `oracle.company.com` or `192.168.1.50` |
| **port** | Oracle listener port | `1521` (default) |
| **sid** | System Identifier - unique name of the Oracle database instance | `ORCL`, `PROD`, `DEV` |
| **user** | Oracle database username | `system`, `dba_user`, etc. |
| **password** | Oracle user password | Your secure password |

### Example Oracle Connection Strings
```
SID Connection: jdbc:oracle:thin:@oracle-server:1521:ORCL
With username system and password oracle
```

## Oracle Driver Information

The project now includes the Oracle JDBC driver (`ojdbc11`) which supports:
- Oracle 23c, 21c, 19c, 18c, 12c, 11g
- Java 8+

## Maven Dependencies

The `pom.xml` has been updated with both database drivers:

```xml
<!-- Oracle JDBC Driver -->
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc11</artifactId>
    <version>23.3.0.23.09</version>
</dependency>

<!-- MySQL JDBC Driver -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

## Complete Example: REST Controller with Configurable Database

```java
import com.example.roadmap.DatabaseConnection;
import org.springframework.web.bind.annotation.*;
import java.sql.Connection;

@RestController
@RequestMapping("/api/config")
public class DatabaseConfigController {
    
    @PostMapping("/connect/mysql")
    public String connectMySQL(
            @RequestParam String host,
            @RequestParam String port,
            @RequestParam String database,
            @RequestParam String user,
            @RequestParam String password) {
        try {
            DatabaseConnection.setMySQLParams(host, port, database, user, password);
            DatabaseConnection.setDatabaseType("MYSQL");
            Connection conn = DatabaseConnection.connect();
            if (conn != null) {
                return "{\"status\": \"Connected to MySQL\", \"url\": \"" + 
                       DatabaseConnection.getConnectionInfo() + "\"}";
            }
            return "{\"status\": \"Connection failed\"}";
        } catch (Exception e) {
            return "{\"status\": \"Error\", \"message\": \"" + e.getMessage() + "\"}";
        }
    }
    
    @PostMapping("/connect/oracle")
    public String connectOracle(
            @RequestParam String host,
            @RequestParam String port,
            @RequestParam String sid,
            @RequestParam String user,
            @RequestParam String password) {
        try {
            DatabaseConnection.setOracleParams(host, port, sid, user, password);
            DatabaseConnection.setDatabaseType("ORACLE");
            Connection conn = DatabaseConnection.connect();
            if (conn != null) {
                return "{\"status\": \"Connected to Oracle\", \"url\": \"" + 
                       DatabaseConnection.getConnectionInfo() + "\"}";
            }
            return "{\"status\": \"Connection failed\"}";
        } catch (Exception e) {
            return "{\"status\": \"Error\", \"message\": \"" + e.getMessage() + "\"}";
        }
    }
    
    @GetMapping("/status")
    public String getStatus() {
        boolean connected = DatabaseConnection.isConnected();
        return "{\"connected\": " + connected + 
               ", \"type\": \"" + DatabaseConnection.getDatabaseType() + 
               "\", \"url\": \"" + DatabaseConnection.getConnectionInfo() + "\"}";
    }
    
    @PostMapping("/disconnect")
    public String disconnect() {
        DatabaseConnection.disconnect();
        return "{\"status\": \"Disconnected\"}";
    }
}
```

## API Endpoints Example

### Connect to MySQL
```bash
curl -X POST "http://localhost:8080/api/config/connect/mysql?host=localhost&port=3306&database=roadmap_mvp&user=root&password="
```

### Connect to Oracle
```bash
curl -X POST "http://localhost:8080/api/config/connect/oracle?host=oracle-server.company.com&port=1521&sid=ORCL&user=system&password=oracle"
```

### Check Connection Status
```bash
curl -X GET "http://localhost:8080/api/config/status"
```

### Disconnect
```bash
curl -X POST "http://localhost:8080/api/config/disconnect"
```

## Troubleshooting

### Oracle Connection Issues

**Issue**: `java.sql.SQLException: No suitable driver found for jdbc:oracle:thin:@...`
- **Solution**: Ensure the Oracle JDBC driver JAR is in the classpath. Run `mvn clean install` to download dependencies.

**Issue**: `ORA-12514: TNS:listener could not resolve SERVICE_NAME`
- **Solution**: Verify the Oracle SID is correct. Check Oracle listener is running: `lsnrctl status`

**Issue**: `ORA-01017: invalid username/password; logon denied`
- **Solution**: Verify username and password are correct. Check Oracle user has appropriate privileges.

### MySQL Connection Issues

**Issue**: `Communications link failure`
- **Solution**: Verify MySQL server is running and accessible from your host/port.

**Issue**: `Access denied for user 'root'@'localhost'`
- **Solution**: Verify MySQL username and password are correct.

## Best Practices

1. **Use Environment Variables**: Store sensitive credentials in environment variables rather than hardcoding them.
   ```java
   String host = System.getenv("DB_HOST");
   String password = System.getenv("DB_PASSWORD");
   ```

2. **Connection Pooling**: For production, use HikariCP or similar connection pool.

3. **Error Handling**: Always handle SQLException appropriately.

4. **Resource Cleanup**: Always close connections when done.
   ```java
   try {
       Connection conn = DatabaseConnection.connect();
       // ... use connection ...
   } finally {
       DatabaseConnection.disconnect();
   }
   ```

5. **Logging**: Enable SQL logging for debugging:
   ```properties
   logging.level.com.mysql.cj.protocol.debug=true
   ```
