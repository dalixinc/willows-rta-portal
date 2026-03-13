# Logging Configuration Guide

## Quick Reference

### Setting Log Levels in application.properties

```properties
# Specific class
logging.level.com.willows.rta.service.MemberExportService=INFO

# Entire package
logging.level.com.willows.rta.service=INFO

# Entire application
logging.level.com.willows.rta=INFO

# Global root level (all libraries)
logging.level.root=INFO
```

---

## Log Levels (Most to Least Verbose)

| Level | Usage | Shows In Logs |
|-------|-------|---------------|
| `TRACE` | Very detailed debugging | Everything |
| `DEBUG` | Development/debugging info | DEBUG, INFO, WARN, ERROR |
| `INFO` | Important events (recommended) | INFO, WARN, ERROR |
| `WARN` | Potential issues | WARN, ERROR |
| `ERROR` | Errors only | ERROR only |
| `OFF` | Disable logging | Nothing |

---

## Default Behavior

**If no logging configuration is set:**

- **Default Level**: `INFO`
- **What Shows**:
  - ✅ `logger.info()` - Shows
  - ❌ `logger.debug()` - Hidden
  - ✅ `logger.warn()` - Shows
  - ✅ `logger.error()` - Shows

---

## Recommended Configurations

### Development (Local)

See detailed debug information:

```properties
# application.properties (local development)
logging.level.com.willows.rta=DEBUG
logging.level.org.springframework=INFO
logging.level.org.hibernate=WARN
```

### Production (Railway)

Only important events and errors:

```properties
# application.properties (production)
logging.level.com.willows.rta=INFO
logging.level.org.springframework=WARN
logging.level.org.hibernate=ERROR
```

### Debugging Specific Issues

Temporarily enable DEBUG for specific class:

```properties
# Debug just the export service
logging.level.com.willows.rta.service.MemberExportService=DEBUG
```

---

## Usage in Code

### Creating a Logger

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemberExportService {
    private static final Logger logger = LoggerFactory.getLogger(MemberExportService.class);
    
    // ... rest of class
}
```

### Using Different Log Levels

```java
// TRACE - Very detailed (rarely used)
logger.trace("Entering method exportToExcel with {} members", members.size());

// DEBUG - Detailed debugging info
logger.debug("Excel export: Using autoSizeColumn (GUI available)");

// INFO - Important events (use this for normal operations)
logger.info("Excel export: Headless server detected, using fixed width");

// WARN - Potential issues
logger.warn("Large export requested: {} members", members.size());

// ERROR - Actual errors
logger.error("Failed to export members", exception);
```

---

## Common Patterns

### Logging with Parameters

```java
// Good - uses placeholders (efficient)
logger.info("Exported {} members to {}", count, format);

// Bad - string concatenation (always evaluates)
logger.info("Exported " + count + " members to " + format);
```

### Conditional Logging

```java
// Only if DEBUG is enabled (expensive operation)
if (logger.isDebugEnabled()) {
    logger.debug("Member details: {}", expensiveToString());
}
```

### Exception Logging

```java
try {
    // code
} catch (Exception e) {
    logger.error("Failed to export members", e);  // Logs stack trace
}
```

---

## Log Output Format

Spring Boot default format:
```
2026-03-11 10:23:45.123  INFO 12345 --- [nio-8080-exec-1] c.w.r.s.MemberExportService : Excel export: Headless server detected
│                           │    │           │                        │
│                           │    │           │                        └─ Log message
│                           │    │           └─ Abbreviated class name
│                           │    └─ Thread name
│                           └─ Log level
└─ Timestamp
```

---

## Railway-Specific Configuration

Railway shows all logs by default. To reduce noise:

```properties
# Only show our app logs at INFO level
logging.level.com.willows.rta=INFO

# Reduce Spring framework noise
logging.level.org.springframework=WARN

# Reduce Hibernate SQL noise
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type=WARN
```

---

## Troubleshooting

### Logs Not Appearing

**Problem**: `logger.debug()` messages don't show

**Solution**: Check log level is DEBUG or lower
```properties
logging.level.com.willows.rta=DEBUG
```

### Too Much Noise

**Problem**: Too many Spring/Hibernate logs

**Solution**: Raise their log levels
```properties
logging.level.org.springframework=WARN
logging.level.org.hibernate=ERROR
```

### Logger Not Found

**Problem**: `LoggerFactory` not recognized

**Solution**: Check dependencies (should be in Spring Boot by default)
```xml
<!-- Usually included automatically with spring-boot-starter -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
</dependency>
```

---

## Best Practices

### ✅ DO:

- Use appropriate log levels (INFO for important events, DEBUG for details)
- Include context in log messages (user IDs, counts, etc.)
- Use parameterized logging for efficiency
- Log exceptions with stack traces
- Keep log messages concise and actionable

### ❌ DON'T:

- Log sensitive data (passwords, tokens, personal info)
- Use string concatenation in log statements
- Log inside tight loops (creates noise)
- Use `System.out.println()` in production code
- Log and re-throw exceptions (causes duplicate logs)

---

## Example: MemberExportService

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemberExportService {
    private static final Logger logger = LoggerFactory.getLogger(MemberExportService.class);

    public void exportToExcel(List<Member> members) {
        logger.info("Starting Excel export for {} members", members.size());
        
        try {
            // Try auto-sizing columns
            for (int i = 0; i < headers.length; i++) {
                try {
                    sheet.autoSizeColumn(i);
                    if (i == 0) {
                        logger.debug("Excel export: Using autoSizeColumn (GUI available)");
                    }
                } catch (HeadlessException | UnsatisfiedLinkError e) {
                    if (i == 0) {
                        logger.info("Excel export: Headless server detected, using fixed width");
                    }
                    sheet.setColumnWidth(i, 20 * 256);
                }
            }
            
            logger.info("Excel export completed successfully");
        } catch (Exception e) {
            logger.error("Excel export failed", e);
            throw e;
        }
    }
}
```

---

## Quick Setup Checklist

- [ ] Add logger to your class
- [ ] Use `logger.info()` for important events
- [ ] Use `logger.debug()` for detailed debugging
- [ ] Set log level in application.properties
- [ ] Test locally with DEBUG level
- [ ] Set to INFO for production (Railway)
- [ ] Monitor Railway logs for your messages

---

## See Also

- [Spring Boot Logging Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)
- [SLF4J Documentation](https://www.slf4j.org/manual.html)
- [Logback Configuration](https://logback.qos.ch/manual/configuration.html)
