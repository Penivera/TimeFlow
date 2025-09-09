# TimeFlow - Comprehensive Timetable Management System

## Overview

TimeFlow is a sophisticated Java-based desktop application designed for comprehensive timetable management in educational institutions. It provides an intuitive interface for managing courses, departments, users, and detecting scheduling conflicts automatically.

## 🎯 Key Features

### Core Functionality
- **Multi-user system** with role-based access control
- **Comprehensive timetable management** with drag-and-drop scheduling
- **Real-time conflict detection** for scheduling overlaps
- **Department and course management** with hierarchical organization
- **User authentication and authorization**
- **Email notifications** for schedule changes and conflicts
- **Detailed reporting** capabilities

### Advanced Features
- **PostgreSQL database** with Hibernate ORM for data persistence
- **Modern UI** with FlatLaf theme for professional appearance
- **Environment-based configuration** using dotenv
- **Comprehensive logging** with SLF4J and Logback
- **Password hashing** with Spring Security Crypto
- **Email integration** for notifications

## 🏗️ Architecture

### Technology Stack
- **Backend**: Java 16, Hibernate ORM 7.1.0, PostgreSQL
- **Frontend**: Java Swing with FlatLaf modern theme
- **Build Tool**: Maven
- **Database**: PostgreSQL with connection pooling
- **Security**: Spring Security Crypto for password hashing
- **Email**: JavaMail API for notifications

### Project Structure
```
TimeFlow/
├── src/main/java/org/timeflow/
│   ├── dao/           # Data Access Objects
│   ├── entity/        # Hibernate entities
│   ├── service/       # Business logic services
│   ├── ui/           # Swing user interface
│   └── util/         # Utility classes
├── src/main/resources/
│   ├── hibernate.cfg.xml    # Database configuration
│   └── logback.xml         # Logging configuration
├── pom.xml          # Maven configuration
└── .env.example    # Environment variables template
```

## 🚀 Installation & Setup

### Prerequisites
- Java 16 or higher
- Maven 3.6+
- PostgreSQL 12+
- SMTP server access (for email notifications)

### Step 1: Database Setup
```bash
# Create PostgreSQL database
createdb timeflow_db

# Create user with permissions
createuser timeflow_user
psql -c "ALTER USER timeflow_user WITH PASSWORD 'admin';"
psql -c "GRANT ALL PRIVILEGES ON DATABASE timeflow_db TO timeflow_user;"
```

### Step 2: Environment Configuration
Create `.env` file in project root:
```bash
# Email Configuration
EMAIL=your-email@domain.com
EMAIL_PASSWORD=your-app-password
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587

# Database (optional - overrides hibernate.cfg.xml)
DB_URL=jdbc:postgresql://localhost:5432/timeflow_db
DB_USERNAME=timeflow_user
DB_PASSWORD=admin
```

### Step 3: Build & Run
```bash
# Clone repository
git clone <repository-url>
cd TimeFlow

# Build project
mvn clean compile

# Run application
mvn exec:java -Dexec.mainClass="org.timeflow.Main"
```

## 📊 Database Schema

### Entity Relationships
```
User (1) ----< (N) Timetable
User (1) ----< (N) Notification
Department (1) ----< (N) Course
Course (1) ----< (N) Timetable
Semester (1) ----< (N) Course
```

### Core Entities

#### User Entity
- **Fields**: id, username, email, password, role, createdAt, updatedAt
- **Roles**: ADMIN, FACULTY, STUDENT
- **Relationships**: One-to-many with Timetable, Notification

#### Course Entity
- **Fields**: id, name, code, description, credits, department, semester
- **Relationships**: Many-to-one with Department, Semester
- **Constraints**: Unique course code per department

#### Timetable Entity
- **Fields**: id, user, course, dayOfWeek, startTime, endTime, location
- **Validation**: No overlapping schedules for same user
- **Features**: Automatic conflict detection

#### Department Entity
- **Fields**: id, name, code, description
- **Relationships**: One-to-many with Course

#### Semester Entity
- **Fields**: id, name, startDate, endDate, academicYear
- **Relationships**: One-to-many with Course

## 🔧 Configuration

### Hibernate Configuration
Located in `src/main/resources/hibernate.cfg.xml`:
- **Dialect**: PostgreSQL10Dialect
- **Connection Pool**: 10 connections
- **DDL**: Auto-update schema
- **SQL Logging**: Enabled for development

### Logging Configuration
- **Framework**: Logback with SLF4J
- **Level**: INFO for production, DEBUG for development
- **Output**: Console and file rotation

## 🎨 User Interface

### Main Components

#### Login Frame
- **Purpose**: User authentication
- **Features**: Remember me, password recovery
- **Security**: Password hashing, session management

#### Main Dashboard
- **Purpose**: Central navigation hub
- **Features**: Quick actions, notifications, user profile
- **Access Control**: Role-based menu items

#### Timetable Frame
- **Purpose**: Visual timetable management
- **Features**: 
  - Drag-and-drop scheduling
  - Color-coded courses
  - Conflict highlighting
  - Print/export functionality

#### Course Management
- **Purpose**: Course CRUD operations
- **Features**: 
  - Batch import/export
  - Department filtering
  - Semester organization
  - Prerequisite management

#### Conflict Management
- **Purpose**: Resolve scheduling conflicts
- **Features**:
  - Automatic detection
  - Suggested resolutions
  - Manual override options
  - Email notifications

## 🔐 Security Features

### Authentication
- **Password Storage**: BCrypt hashing with salt
- **Session Management**: Secure session tokens
- **Role-based Access**: Granular permissions
- **Password Policies**: Complexity requirements

### Data Protection
- **SQL Injection Prevention**: Hibernate parameterized queries
- **Input Validation**: Comprehensive validation framework
- **Audit Logging**: All changes tracked with user attribution

## 📈 Performance Optimization

### Database Optimization
- **Connection Pooling**: HikariCP integration
- **Query Optimization**: Indexed foreign keys
- **Lazy Loading**: Strategic fetch strategies
- **Caching**: Second-level cache for reference data

### UI Responsiveness
- **Lazy Loading**: Data loaded on demand
- **Pagination**: Large datasets paginated
- **Background Processing**: Long operations in worker threads
- **Caching**: UI state persistence

## 🧪 Testing

### Unit Tests
```bash
# Run unit tests
mvn test

# Run with coverage
mvn test jacoco:report
```

### Integration Tests
```bash
# Run integration tests
mvn verify

# Database integration tests
mvn test -Dtest.profile=integration
```

### Manual Testing Checklist
- [ ] User registration and authentication
- [ ] Course creation and management
- [ ] Timetable scheduling and conflicts
- [ ] Email notification delivery
- [ ] Report generation
- [ ] Database backup and restore

## 📊 Monitoring & Maintenance

### Health Checks
- **Database Connectivity**: Connection pool monitoring
- **Email Service**: SMTP connectivity checks
- **Disk Space**: Log file rotation monitoring
- **Memory Usage**: JVM heap monitoring

### Backup Strategy
- **Database**: Daily PostgreSQL dumps
- **Configuration**: Version controlled .env files
- **Logs**: Centralized log aggregation

### Maintenance Tasks
```bash
# Update dependencies
mvn versions:display-dependency-updates

# Security scan
mvn dependency-check:check

# Database maintenance
vacuumdb -d timeflow_db -f -v
```

## 🚨 Troubleshooting

### Common Issues

#### Database Connection Failed
```bash
# Check PostgreSQL service
sudo systemctl status postgresql

# Verify database exists
psql -l | grep timeflow_db

# Test connection
psql -h localhost -U timeflow_user -d timeflow_db
```

#### Email Notifications Not Working
```bash
# Check SMTP configuration
telnet smtp.gmail.com 587

# Verify .env settings
cat .env | grep -E "EMAIL|SMTP"
```

#### Build Failures
```bash
# Clean build
mvn clean

# Force update dependencies
mvn dependency:purge-local-repository

# Check Java version
java -version
mvn -version
```

### Debug Mode
```bash
# Enable debug logging
mvn exec:java -Dexec.mainClass="org.timeflow.Main" -Dlogback.configurationFile=logback-debug.xml
```

## 🤝 Contributing

### Development Setup
```bash
# Fork repository
git clone <your-fork-url>
git checkout -b feature/your-feature

# Development build
mvn clean compile exec:java -Dspring.profiles.active=dev

# Run tests
mvn test
```

### Code Style
- **Java Format**: Google Java Format
- **Checkstyle**: Maven checkstyle plugin
- **SonarQube**: Static code analysis

### Pull Request Process
1. Create feature branch
2. Write tests for new functionality
3. Ensure all tests pass
4. Update documentation
5. Submit pull request with description

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support, email support@timeflow.com or create an issue in the GitHub repository.

## 📚 Additional Resources

- [User Manual](docs/user-manual.md)
- [API Documentation](docs/api-documentation.md)
- [Database Schema](docs/database-schema.md)
- [Deployment Guide](docs/deployment-guide.md)
- [Video Tutorials](https://youtube.com/timeflow-tutorials)
