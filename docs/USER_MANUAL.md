# TimeFlow User Manual

## Table of Contents
1. [Getting Started](#getting-started)
2. [User Registration & Login](#user-registration--login)
3. [Dashboard Navigation](#dashboard-navigation)
4. [Managing Courses](#managing-courses)
5. [Creating Timetables](#creating-timetables)
6. [Conflict Detection](#conflict-detection)
7. [Reports & Analytics](#reports--analytics)
8. [Settings & Preferences](#settings--preferences)

## Getting Started

### System Requirements
- Java 16 or higher installed
- PostgreSQL database running
- Minimum 4GB RAM
- 500MB disk space

### First Time Setup
1. **Launch Application**: Double-click the TimeFlow.jar file or run `java -jar TimeFlow.jar`
2. **Database Connection**: Ensure PostgreSQL is running with the configured credentials
3. **Login Screen**: Use default admin credentials (if provided) or register a new account

## User Registration & Login

### Registration Process
1. Click "Register New Account" on login screen
2. Fill in required fields:
   - Username (unique, 3-20 characters)
   - Email (valid email format)
   - Password (minimum 8 characters, 1 uppercase, 1 number)
   - Confirm password
3. Select role:
   - **Admin**: Full system access
   - **Faculty**: Course management and timetable creation
   - **Student**: View-only access to assigned timetables
4. Click "Register" and verify email if required

### Login
1. Enter username/email and password
2. Select "Remember Me" for persistent login (optional)
3. Click "Login"
4. Use "Forgot Password" link if needed

## Dashboard Navigation

### Main Dashboard Overview
The dashboard provides quick access to all system features:

#### Top Navigation Bar
- **Home**: Return to dashboard
- **Courses**: Manage course catalog
- **Timetables**: Create and view schedules
- **Reports**: Generate analytics
- **Settings**: Configure preferences
- **Logout**: Secure logout

#### Quick Actions Panel
- **Create New Course**: Quick course creation
- **View My Schedule**: Personal timetable
- **Check Conflicts**: Conflict detection
- **Generate Report**: Quick report creation

#### Notification Center
- **Bell Icon**: Shows unread notifications
- **Email Icon**: Email notification settings
- **Alert Icon**: System alerts and warnings

## Managing Courses

### Adding a New Course
1. Navigate to **Courses** → **Add New Course**
2. Fill in course details:
   - **Course Name**: Full course title
   - **Course Code**: Unique identifier (e.g., CS101)
   - **Department**: Select from dropdown
   - **Credits**: Numeric value (1-6)
   - **Description**: Detailed course information
   - **Prerequisites**: Select prerequisite courses
3. **Advanced Options**:
   - **Semester**: Assign to specific semester
   - **Faculty**: Assign instructor
   - **Capacity**: Maximum enrollment
4. Click **Save Course**

### Editing Courses
1. Navigate to **Courses** → **Course List**
2. Find course using search or filters
3. Click **Edit** icon (pencil)
4. Modify required fields
5. Click **Update Course**

### Course Management Features
- **Search**: Filter by name, code, department, or instructor
- **Sort**: By name, code, credits, or department
- **Export**: Download course list as CSV or PDF
- **Bulk Operations**: Import multiple courses via CSV

## Creating Timetables

### Creating a New Timetable
1. Navigate to **Timetables** → **Create New**
2. **Basic Information**:
   - **Name**: Timetable identifier
   - **Semester**: Select academic period
   - **Department**: Filter courses by department
3. **Add Courses**:
   - **Search Courses**: Find courses by name/code
   - **Add to Timetable**: Select courses to include
   - **Set Schedule**: Define day/time for each course

### Schedule Configuration
For each course, specify:
- **Day of Week**: Monday-Sunday
- **Start Time**: HH:MM format
- **End Time**: Must be after start time
- **Location**: Classroom or online link
- **Instructor**: Assigned faculty member

### Visual Schedule Builder
- **Calendar View**: Monthly/weekly calendar display
- **Drag & Drop**: Move courses between time slots
- **Color Coding**: Different colors for departments
- **Conflict Highlighting**: Red indicators for overlaps

## Conflict Detection

### Automatic Conflict Detection
The system automatically detects:
- **Time Conflicts**: Overlapping schedules
- **Room Conflicts**: Double-booked locations
- **Instructor Conflicts**: Faculty double-booked
- **Student Conflicts**: Student schedule overlaps

### Conflict Resolution
1. **View Conflicts**: Click "Check Conflicts" button
2. **Conflict Details**: Shows affected courses, times, and users
3. **Resolution Options**:
   - **Auto-resolve**: System suggests optimal changes
   - **Manual adjustment**: User selects new times
   - **Split sessions**: Divide into multiple time slots
4. **Apply Changes**: Update timetable with resolved conflicts

### Conflict Prevention
- **Real-time validation**: Immediate feedback during scheduling
- **Availability checking**: Instructor/room availability display
- **Capacity warnings**: Enrollment limit alerts

## Reports & Analytics

### Available Reports
1. **Course Enrollment Report**
   - Student count per course
   - Department-wise distribution
   - Semester trends

2. **Instructor Workload**
   - Teaching hours per faculty
   - Course distribution
   - Schedule density analysis

3. **Room Utilization**
   - Usage percentage per room
   - Peak hours analysis
   - Capacity optimization

4. **Conflict Analysis**
   - Historical conflict data
   - Common conflict patterns
   - Resolution effectiveness

### Report Generation
1. **Select Report Type**: Choose from available reports
2. **Set Parameters**: Date range, department, semester filters
3. **Generate**: Click "Generate Report"
4. **Export Options**: PDF, Excel, CSV formats

### Dashboard Analytics
- **Quick Stats**: Total courses, users, conflicts
- **Trend Charts**: Enrollment trends, conflict rates
- **Performance Metrics**: System usage statistics

## Settings & Preferences

### User Profile Settings
- **Personal Information**: Update name, email, contact details
- **Password Change**: Secure password update
- **Notification Preferences**: Email alerts configuration
- **Display Settings**: Theme, language preferences

### System Settings (Admin Only)
- **User Management**: Add/edit/delete users
- **Department Configuration**: Create/modify departments
- **Course Templates**: Standard course configurations
- **Email Templates**: Custom notification messages

### Backup & Restore
- **Manual Backup**: Create database backup
- **Scheduled Backup**: Automatic daily backups
- **Restore**: Point-in-time recovery options
- **Export Data**: User data portability

## 📞 Support & Troubleshooting

### Common Issues

#### Login Problems
- **Forgot Password**: Use "Forgot Password" link
- **Account Locked**: Contact administrator
- **Invalid Credentials**: Check username/email spelling

#### Schedule Conflicts
- **Cannot Save**: Check for time conflicts
- **Room Unavailable**: Verify room availability
- **Instructor Busy**: Check instructor schedule

#### Performance Issues
- **Slow Loading**: Check database connection
- **UI Freezing**: Close other applications
- **Memory Issues**: Increase JVM heap size

### Getting Help
- **In-App Help**: Click "Help" → "User Manual"
- **Email Support**: support@timeflow.com
- **Knowledge Base**: docs.timeflow.com
- **Community Forum**: forum.timeflow.com

## 🎯 Best Practices

### For Administrators
- **Regular Backups**: Schedule daily database backups
- **User Training**: Conduct regular training sessions
- **System Monitoring**: Monitor performance metrics
- **Security Updates**: Keep system and dependencies updated

### For Faculty
- **Course Planning**: Plan courses well in advance
- **Conflict Prevention**: Check availability before scheduling
- **Communication**: Notify students of schedule changes
- **Documentation**: Maintain detailed course descriptions

### For Students
- **Regular Check-ins**: Review timetable frequently
- **Conflict Reporting**: Report scheduling issues immediately
- **Backup Plans**: Have alternative course options
- **Communication**: Stay updated with notifications

## 📱 Mobile Access

While TimeFlow is primarily a desktop application, you can:
- **Export schedules** to mobile-friendly formats
- **Email schedules** to mobile devices
- **Use web interface** (if available) for mobile access
- **Sync with calendar apps** via exported files

## 🔗 Integration Capabilities

### Third-Party Integrations
- **Calendar Apps**: Google Calendar, Outlook, etc.
- **Student Information Systems**: Custom API integration



