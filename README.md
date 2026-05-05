<<<<<<< HEAD


# 🗓️ Leave Management System (LMS)

A full-stack Leave Management System built with **Angular 19**, **Spring Boot 3.2 (JDK 17)**, **MySQL**, and **JWT authentication**.

---

## 📁 Project Structure

```
lms/
├── backend/          # Spring Boot application
├── frontend/         # Angular 19 application
├── database/
│   └── schema.sql    # MySQL schema + sample data
├── postman/
│   └── LMS_Collection.json
└── README.md
```

---

## 👥 Demo Accounts

| Role     | Email              | Password    |
|----------|--------------------|-------------|
| Manager  | manager@lms.com    | password123 |
| Employee | john@lms.com       | password123 |
| Employee | emily@lms.com      | password123 |
| Employee | michael@lms.com    | password123 |

---

## ⚙️ Prerequisites

| Tool          | Version    |
|---------------|------------|
| Java (JDK)    | 17         |
| Maven         | 3.8+       |
| Node.js       | 18+        |
| npm           | 9+         |
| MySQL         | 8.x        |
| Angular CLI   | 19.x       |

---

## 🗄️ Step 1: Database Setup

### Option A – Run schema manually
```sql
mysql -u root -p < database/schema.sql
```

### Option B – Let Spring Boot auto-create tables
Spring Boot is configured with `spring.jpa.hibernate.ddl-auto=update`.  
Just create the database and Spring Boot handles the rest:
```sql
CREATE DATABASE leave_management_db CHARACTER SET utf8mb4;
```
The `DataSeeder` class will insert sample data on first run.

---

## 🚀 Step 2: Backend Setup (Spring Boot)

### 2.1 Configure database credentials
Edit `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/leave_management_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root        # ← change to your MySQL username
spring.datasource.password=root        # ← change to your MySQL password
```

### 2.2 Build and run
```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
```

Backend will start at: **http://localhost:8080**

You should see in console:
```
✅ Sample data seeded successfully!
👤 Manager: manager@lms.com / password123
...
```

### 2.3 Verify backend is running
```bash
curl http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"manager@lms.com","password":"password123"}'
```

### 2.4 Enable approval email notifications
Set SMTP credentials as environment variables before starting the backend:

```bash
APP_MAIL_ENABLED=true
APP_MAIL_FROM=no-reply@yourdomain.com
APP_MAIL_FROM_NAME=Leave Management System
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-smtp-username
MAIL_PASSWORD=your-app-password
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true
```

When a manager approves a leave request, the backend sends an email to the employee asynchronously.
Do not hardcode SMTP usernames or passwords in `application.properties`.

---

## 🌐 Step 3: Frontend Setup (Angular 19)

### 3.1 Install Angular CLI (if not installed)
```bash
npm install -g @angular/cli@19
```

### 3.2 Install dependencies
```bash
cd frontend
npm install
```

### 3.3 Start development server
```bash
ng serve
```
or
```bash
npm start
```

Frontend will start at: **http://localhost:4200**

---

## 🔗 Step 4: Access the Application

Open **http://localhost:4200** in your browser.

- Login as **Manager**: `manager@lms.com` / `password123`
- Login as **Employee**: `john@lms.com` / `password123`

---

## 📬 Step 5: API Testing with Postman

1. Open Postman
2. Click **Import** → select `postman/LMS_Collection.json`
3. Run **"Login - Employee"** first (auto-saves token to collection variable)
4. Run **"Login - Manager"** to save manager token
5. All other requests use these tokens automatically

### API Endpoints Summary

| Method | Endpoint                        | Role     | Description              |
|--------|---------------------------------|----------|--------------------------|
| POST   | /api/auth/register              | Public   | Register new user        |
| POST   | /api/auth/login                 | Public   | Login & get JWT token    |
| POST   | /api/leaves/apply               | Employee | Apply for leave          |
| GET    | /api/leaves/my                  | Employee | Get own leave requests   |
| GET    | /api/leaves/balance             | Employee | Get leave balance        |
| GET    | /api/leaves/dashboard/employee  | Employee | Employee dashboard stats |
| GET    | /api/leaves/team                | Manager  | Get all team leaves      |
| PUT    | /api/leaves/approve/{id}        | Manager  | Approve leave request    |
| PUT    | /api/leaves/reject/{id}         | Manager  | Reject leave request     |
| GET    | /api/leaves/dashboard/manager   | Manager  | Manager dashboard stats  |

---

## 🏗️ Architecture Overview

### Backend (Spring Boot)
```
com.lms/
├── config/
│   ├── SecurityConfig.java       # JWT + CORS + role-based security
│   └── DataSeeder.java           # Sample data on startup
├── controller/
│   ├── AuthController.java       # /api/auth/**
│   └── LeaveController.java      # /api/leaves/**
├── service/
│   ├── AuthService.java
│   └── LeaveService.java
├── repository/
│   ├── UserRepository.java       # Spring Data JPA
│   └── LeaveRequestRepository.java
├── entity/
│   ├── User.java                 # implements UserDetails
│   ├── LeaveRequest.java
│   ├── Role.java
│   ├── LeaveType.java
│   └── LeaveStatus.java
├── dto/                          # Request/Response DTOs
├── security/
│   ├── JwtUtil.java
│   ├── JwtAuthenticationFilter.java
│   └── UserDetailsServiceImpl.java
└── exception/                    # Global exception handling
```

### Frontend (Angular 19)
```
src/app/
├── components/
│   ├── login/                    # Login page
│   ├── register/                 # Registration page
│   ├── navbar/                   # Shared navigation bar
│   ├── employee-dashboard/       # Employee home with stats
│   ├── manager-dashboard/        # Manager home with stats
│   ├── apply-leave/              # Leave application form
│   ├── my-leaves/                # Employee leave history
│   ├── team-leaves/              # Manager: view + approve/reject
│   └── leave-balance/            # Leave quota breakdown
├── services/
│   ├── auth.service.ts           # Auth API calls + localStorage
│   └── leave.service.ts          # Leave API calls
├── guards/
│   └── auth.guard.ts             # authGuard, employeeGuard, managerGuard
├── interceptors/
│   └── jwt.interceptor.ts        # Attaches Bearer token to all requests
├── models/
│   ├── auth.model.ts
│   └── leave.model.ts
├── app.routes.ts                 # Lazy-loaded routes
└── app.config.ts                 # provideHttpClient + interceptors
```

---

## 🔒 Security Notes

- JWT tokens expire after **24 hours** (configurable in `application.properties`)
- Passwords are hashed with **BCrypt**
- All APIs except `/api/auth/**` require a valid JWT
- Role-based access enforced at both Spring Security and `@PreAuthorize` levels
- CORS is configured to allow only `http://localhost:4200`

---

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| DB connection refused | Check MySQL is running: `mysql -u root -p` |
| Password auth error | Update `application.properties` credentials |
| Port 8080 in use | `lsof -i :8080` then kill the process |
| Port 4200 in use | `ng serve --port 4201` |
| `ng` not found | Run `npm install -g @angular/cli@19` |
| CORS error | Ensure backend is running on port 8080 |
| Token expired | Log out and log back in |

---

## 🧪 Running Tests

### Backend unit tests
```bash
cd backend
mvn test
```

### Frontend tests
```bash
cd frontend
ng test
```

---

## 📌 Leave Types Supported

`CASUAL` · `SICK` · `ANNUAL` · `MATERNITY` · `PATERNITY` · `EMERGENCY` · `UNPAID`

---

## 🎯 Features Summary

### Employee
- 📊 Dashboard with leave balance & usage chart
- 📝 Apply for leave (with date validation & balance check)
- 📋 View own leave history with status filtering
- 💰 Detailed leave balance breakdown

### Manager
- 📊 Team dashboard with approval statistics
- 👥 View all team leave requests with search & filter
- ✅ Approve / ❌ Reject leaves with optional reason
- 📈 Approval rate tracking
=======
# lms_project
>>>>>>> 880c0aad4e5c2bf0f5321f2553e18f027028f674
