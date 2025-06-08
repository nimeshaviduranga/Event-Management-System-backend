## Event Management System Backend

A RESTful API for managing events and attendances with JWT authentication, role-based access control including Redis caching.

### Technologies Used

- **Java 17** - Programming language
- **Spring Boot 3.5.0** - Framework
- **Spring Security** - JWT Authentication & Authorization
- **Spring Data JPA** - Database Mapping
- **PostgreSQL** - Primary database
- **Redis** - Caching
- **H2 Database** - Testing
- **MapStruct** - DTO mapping
- **Docker & Docker Compose** - Containerization
- **Maven** - Build tool

### Prerequisites
- Docker and Docker Compose
- Git

### 1. Clone Repository
```
git clone https://github.com/nimeshaviduranga/Event-Management-System-backend.git
cd event-management-system
```

### 2. Create .env File
Create a `.env` file in the root directory(Check the .env.example file):


### 3. Run Application
```
# Start all services
docker-compose up --build

# Run in background
docker-compose up --build -d

# Stop services
docker-compose down
```

### 4. Run Tests
```
./mvnw test
```

**Application URL:** http://localhost:8080/api/v1

### API Endpoints

#### Auth
```
POST /auth/register - Register new user
POST /auth/login - User login
```
#### Events

````
POST /events - Create event
GET /events - List events with filters 
GET /events/upcoming - List upcoming events
GET /events/{id} - Get event details 
PUT /events/{id} - Update event 
DELETE /events/{id} - Delete event 
GET /events/hosting - Events hosted by user 
GET /events/attending - Events user is attending 
````


#### Attendance

````
POST /attendance - Respond to event 
PUT /attendance/events/{eventId} - Update attendance 
GET /attendance/events/{eventId}/status - Get attendance status 
````