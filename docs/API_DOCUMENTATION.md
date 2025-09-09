# TimeFlow API Documentation

## Overview
TimeFlow provides a comprehensive RESTful API for programmatic access to timetable management functionality. This documentation covers all available endpoints, authentication methods, and usage examples.

## Base URL
```
http://localhost:8080/api/v1
```

## Authentication
All API endpoints require authentication using API keys passed in the Authorization header:

```
Authorization: Bearer YOUR_API_KEY
```

## Rate Limiting
- **Standard**: 100 requests per minute
- **Authenticated**: 1000 requests per minute
- **Admin**: 5000 requests per minute

## Response Format
All responses are in JSON format with the following structure:

```json
{
  "success": true,
  "data": {...},
  "message": "Operation completed successfully",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Error Handling
Error responses include detailed error information:

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input parameters",
    "details": ["Course code is required", "Start time must be before end time"]
  }
}
```

## API Endpoints

### Users

#### Get All Users
```http
GET /users
```

**Query Parameters:**
- `role` (optional): Filter by role (ADMIN, FACULTY, STUDENT)
- `department` (optional): Filter by department ID
- `page` (optional): Page number (default: 1)
- `limit` (optional): Items per page (default: 20)

**Response:**
```json
{
  "data": {
    "users": [
      {
        "id": 1,
        "username": "john_doe",
        "email": "john@university.edu",
        "role": "FACULTY",
        "department": {
          "id": 1,
          "name": "Computer Science"
        },
        "createdAt": "2024-01-10T08:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 45,
      "pages": 3
    }
  }
}
```

#### Get User by ID
```http
GET /users/{id}
```

**Response:**
```json
{
  "data": {
    "user": {
      "id": 1,
      "username": "john_doe",
      "email": "john@university.edu",
      "role": "FACULTY",
      "department": {
        "id": 1,
        "name": "Computer Science"
      },
      "timetables": [...],
      "createdAt": "2024-01-10T08:00:00Z"
    }
  }
}
```

#### Create User
```http
POST /users
```

**Request Body:**
```json
{
  "username": "jane_smith",
  "email": "jane@university.edu",
  "password": "securePassword123",
  "role": "FACULTY",
  "departmentId": 1
}
```

**Response:**
```json
{
  "data": {
    "user": {
      "id": 2,
      "username": "jane_smith",
      "email": "jane@university.edu",
      "role": "FACULTY",
      "department": {
        "id": 1,
        "name": "Computer Science"
      },
      "createdAt": "2024-01-15T10:30:00Z"
    }
  }
}
```

#### Update User
```http
PUT /users/{id}
```

**Request Body:**
```json
{
  "email": "new_email@university.edu",
  "role": "ADMIN"
}
```

#### Delete User
```http
DELETE /users/{id}
```

### Departments

#### Get All Departments
```http
GET /departments
```

**Response:**
```json
{
  "data": {
    "departments": [
      {
        "id": 1,
        "name": "Computer Science",
        "code": "CS",
        "description": "Department of Computer Science",
        "courses": [...],
        "createdAt": "2024-01-01T00:00:00Z"
      }
    ]
  }
}
```

#### Create Department
```http
POST /departments
```

**Request Body:**
```json
{
  "name": "Mathematics",
  "code": "MATH",
  "description": "Department of Mathematics"
}
```

### Courses

#### Get All Courses
```http
GET /courses
```

**Query Parameters:**
- `department` (optional): Filter by department ID
- `semester` (optional): Filter by semester ID
- `search` (optional): Search by name or code
- `page` (optional): Page number
- `limit` (optional): Items per page

**Response:**
```json
{
  "data": {
    "courses": [
      {
        "id": 1,
        "name": "Introduction to Programming",
        "code": "CS101",
        "description": "Basic programming concepts",
        "credits": 3,
        "department": {
          "id": 1,
          "name": "Computer Science"
        },
        "semester": {
          "id": 1,
          "name": "Fall 2024"
        },
        "prerequisites": []
      }
    ]
  }
}
```

#### Get Course by ID
```http
GET /courses/{id}
```

#### Create Course
```http
POST /courses
```

**Request Body:**
```json
{
  "name": "Data Structures",
  "code": "CS201",
  "description": "Advanced data structures and algorithms",
  "credits": 4,
  "departmentId": 1,
  "semesterId": 1,
  "prerequisiteIds": [1]
}
```

#### Update Course
```http
PUT /courses/{id}
```

#### Delete Course
```http
DELETE /courses/{id}
```

### Timetables

#### Get All Timetables
```http
GET /timetables
```

**Query Parameters:**
- `user` (optional): Filter by user ID
- `course` (optional): Filter by course ID
- `semester` (optional): Filter by semester ID
- `day` (optional): Filter by day of week
- `date` (optional): Filter by specific date

**Response:**
```json
{
  "data": {
    "timetables": [
      {
        "id": 1,
        "user": {
          "id": 1,
          "username": "john_doe"
        },
        "course": {
          "id": 1,
          "name": "Introduction to Programming",
          "code": "CS101"
        },
        "dayOfWeek": "MONDAY",
        "startTime": "09:00:00",
        "endTime": "10:30:00",
        "location": "Room 101",
        "semester": {
          "id": 1,
          "name": "Fall 2024"
        }
      }
    ]
  }
}
```

#### Create Timetable Entry
```http
POST /timetables
```

**Request Body:**
```json
{
  "userId": 1,
  "courseId": 1,
  "dayOfWeek": "MONDAY",
  "startTime": "09:00:00",
  "endTime": "10:30:00",
  "location": "Room 101",
  "semesterId": 1
}
```

#### Check Conflicts
```http
POST /timetables/check-conflicts
```

**Request Body:**
```json
{
  "userId": 1,
  "dayOfWeek": "MONDAY",
  "startTime": "09:00:00",
  "endTime": "10:30:00",
  "semesterId": 1
}
```

**Response:**
```json
{
  "data": {
    "conflicts": [
      {
        "type": "TIME_CONFLICT",
        "message": "Time slot already booked",
        "conflictingTimetable": {
          "id": 2,
          "course": {
            "name": "Calculus I"
          },
          "startTime": "09:00:00",
          "endTime": "10:30:00"
        }
      }
    ]
  }
}
```

### Semesters

#### Get All Semesters
```http
GET /semesters
```

**Response:**
```json
{
  "data": {
    "semesters": [
      {
        "id": 1,
        "name": "Fall 2024",
        "startDate": "2024-08-15",
        "endDate": "2024-12-15",
        "academicYear": "2024-2025"
      }
    ]
  }
}
```

#### Create Semester
```http
POST /semesters
```

**Request Body:**
```json
{
  "name": "Spring 2025",
  "startDate": "2025-01-15",
  "endDate": "2025-05-15",
  "academicYear": "2024-2025"
}
```

### Reports

#### Generate Enrollment Report
```http
GET /reports/enrollment
```

**Query Parameters:**
- `semester` (optional): Semester ID
- `department` (optional): Department ID
- `format` (optional): json, csv, pdf

**Response:**
```json
{
  "data": {
    "report": {
      "totalCourses": 45,
      "totalEnrollments": 1200,
      "departmentStats": [
        {
          "department": "Computer Science",
          "courses": 15,
          "enrollments": 450
        }
      ],
      "format": "json"
    }
  }
}
```

#### Generate Instructor Workload Report
```http
GET /reports/instructor-workload
```

#### Generate Room Utilization Report
```http
GET /reports/room-utilization
```

### Notifications

#### Get Notifications
```http
GET /notifications
```

**Query Parameters:**
- `user` (optional): Filter by user ID
- `read` (optional): Filter by read status
- `type` (optional): Filter by notification type

#### Send Notification
```http
POST /notifications
```

**Request Body:**
```json
{
  "userId": 1,
  "type": "SCHEDULE_CHANGE",
  "message": "Your schedule has been updated",
  "data": {
    "timetableId": 1,
    "changeType": "TIME_UPDATE"
  }
}
```

## Error Codes

| Code | Description |
|------|-------------|
| 400 | Bad Request - Invalid input parameters |
| 401 | Unauthorized - Invalid or missing API key |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Resource already exists |
| 422 | Unprocessable Entity - Validation error |
| 429 | Too Many Requests - Rate limit exceeded |
| 500 | Internal Server Error - Server error |

## SDKs and Libraries

### Java SDK
```xml
<dependency>
  <groupId>com.timeflow</groupId>
  <artifactId>timeflow-sdk</artifactId>
  <version>1.0.0</version>
</dependency>
```

### JavaScript SDK
```bash
npm install timeflow-sdk
```

### Python SDK
```bash
pip install timeflow-sdk
```

## Webhooks

### Available Events
- `timetable.created`
- `timetable.updated`
- `timetable.deleted`
- `conflict.detected`
- `user.registered`

### Webhook Payload
```json
{
  "event": "timetable.created",
  "timestamp": "2024-01-15T10:30:00Z",
  "data": {
    "timetable": {...}
  }
}
```

## Rate Limiting Headers
- `X-RateLimit-Limit`: Request limit per window
- `X-RateLimit-Remaining`: Remaining requests
- `X-RateLimit-Reset`: Reset time (Unix timestamp)

## Support
For API support, contact api-support@timeflow.com or create an issue in the GitHub repository.
