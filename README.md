# RetroLab Backend

**RetroLab** is a backend application designed to manage retro console inventories, repair workflows and electronic diagnostic measurements.

It was developed as a Final Degree Project at the Universitat Oberta de Catalunya (UOC). The application provides a REST API that allows users to manage manufacturers, console models, consoles, repair cases, components and diagnostic tests.

The backend also provides JWT authentication, console image management, automatic development data initialization and interactive API documentation through Swagger.

## Configuration and Deployment Guide

This repository contains the **RetroLab backend**, including the Spring Boot application, PostgreSQL configuration and automated tests.

## Requirements

Before running the project, make sure that the following software is installed:

- Java 21
- Docker Desktop
- Git

The following ports must be available:

- `8080`: RetroLab REST API
- `5332`: PostgreSQL database

The project includes the Maven Wrapper, so Maven does not need to be installed separately.

## Getting the project

### Option 1: Direct download

1. Open the GitHub repository.
2. Select the green **Code** button.
3. Select **Download ZIP**.
4. Extract the downloaded file.
5. Open a terminal inside the project directory.

### Option 2: Clone with Git

Run:

```bash
git clone <REPOSITORY_URL>
```

Then open the project directory:

```bash
cd backend_thesis
```

Replace `<REPOSITORY_URL>` with the actual URL of the RetroLab repository.

## Environment variables

Credentials and security values are not included directly in the source code.

Create a file named `.env` in the root directory of the project:

```env
SPRING_PROFILES_ACTIVE=dev

DB_URL=jdbc:postgresql://localhost:5332/backend_api
DB_USERNAME=backend_api
DB_PASSWORD=your_database_password

JWT_SECRET=your_base64_encoded_jwt_secret
JWT_EXPIRATION=3600000

ADMIN_NAME=RetroLab Admin
ADMIN_EMAIL=admin@retrolab.test
ADMIN_PASSWORD=your_admin_password
```

Make sure that the file is named exactly:

```text
.env
```

It must not have a hidden `.txt` extension.

The `.env` file must not be committed to Git because it contains sensitive information. Ensure that `.gitignore` contains:

```gitignore
.env
target/
uploads/
```

### JWT secret

`JWT_SECRET` is used to sign and validate authentication tokens.

It must be a sufficiently long Base64-encoded value suitable for the HS256 algorithm.

### JWT expiration

`JWT_EXPIRATION` defines the token validity period in milliseconds.

For example:

```env
JWT_EXPIRATION=3600000
```

This corresponds to one hour.

## Development profile

The development profile is enabled with:

```env
SPRING_PROFILES_ACTIVE=dev
```

When this profile is active, Spring Boot also loads:

```text
application-dev.properties
```

This profile enables the demonstration data initializer:

```properties
app.test-data.enabled=true
```

The development configuration uses:

```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

This recreates the database schema whenever the application starts and removes it when the application stops.

> **Warning:** All manually introduced database data will be lost after restarting or stopping the application. This configuration is intended only for development and evaluation.

## Database initialization

RetroLab contains two data initializers.

### Administrator initializer

`DataInitializer` creates the initial administrator using the following environment variables:

```env
ADMIN_NAME
ADMIN_EMAIL
ADMIN_PASSWORD
```

The administrator is only created if a user with the configured email does not already exist.

### Development data initializer

`TestDataInitializer` creates demonstration data for development and evaluation.

The generated dataset includes:

- Administrators and technicians
- Manufacturers
- Console models
- Console components
- Registered and external console owners
- Consoles
- Repair cases
- Diagnostic component tests

The initializer runs when the `dev` profile is active and the following property is enabled:

```properties
app.test-data.enabled=true
```

## How to run the application

First, make sure Docker Desktop is running.

Open a terminal in the project root and execute:

### Windows PowerShell

```powershell
.\mvnw.cmd spring-boot:run
```

### macOS or Linux

```bash
./mvnw spring-boot:run
```

Spring Boot will detect the Docker Compose configuration and start the PostgreSQL service when necessary.

The application has started correctly when the terminal displays:

```text
Tomcat started on port 8080
Started BackendThesisApplication
```

The backend will then be available at:

```text
http://localhost:8080
```

Keep the terminal open while using the application.

## Stopping the application

Press:

```text
Ctrl + C
```

in the terminal where the backend is running.

To stop the Docker Compose services manually, run:

```bash
docker compose down
```

To remove the database volume as well:

```bash
docker compose down -v
```

The second command deletes all persisted database data.

## API documentation with Swagger

Interactive API documentation is available at:

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

The generated OpenAPI specification is available at:

[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

Swagger displays the available endpoints grouped by resource:

- Authentication
- Users
- Manufacturers
- Console Models
- Consoles
- Repair Cases
- Components
- Component Tests

## Testing the API with Swagger

Most endpoints are protected using JWT authentication.

To execute them through Swagger:

1. Open the Swagger UI.
2. Find `POST /api/v1/auth/login`.
3. Select **Try it out**.
4. Enter the administrator credentials configured in `.env`.
5. Select **Execute**.
6. Copy the value of `token` from the response body.
7. Select **Authorize** at the top of the Swagger page.
8. Paste only the token, without quotation marks.
9. Select **Authorize**.
10. Execute any protected endpoint.

Swagger will automatically include the following request header:

```http
Authorization: Bearer <token>
```

Do not use a JWT generated by an external website because its signature will not match the `JWT_SECRET` configured for RetroLab.

## Authentication example

Request:

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{
  "email": "admin@retrolab.test",
  "password": "your_admin_password"
}
```

Successful response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600000
}
```

## Main API resources

The API exposes the following base routes:

| Resource | Base endpoint |
|---|---|
| Authentication | `/api/v1/auth` |
| Users | `/api/v1/users` |
| Manufacturers | `/api/v1/manufacturers` |
| Console models | `/api/v1/console-models` |
| Consoles | `/api/v1/consoles` |
| Repair cases | `/api/v1/repair-cases` |
| Components | `/api/v1/components` |
| Component tests | `/api/v1/component-tests` |

The complete operations, request schemas and response formats can be consulted through Swagger.

## Console images

Console images are uploaded using:

```http
POST /api/v1/consoles/{id}/image
Content-Type: multipart/form-data
```

The multipart field must be named:

```text
image
```

Images are stored in:

```text
uploads/consoles
```

The configured upload limits are:

```properties
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=6MB
```

## Running the tests

The project includes unit and integration tests developed with JUnit 5, Mockito and MockMvc.

### Run all tests

On Windows:

```powershell
.\mvnw.cmd test
```

On macOS or Linux:

```bash
./mvnw test
```

### Run a specific test class

On Windows:

```powershell
.\mvnw.cmd test -Dtest=ManufacturerIntegrationTest
```

On macOS or Linux:

```bash
./mvnw test -Dtest=ManufacturerIntegrationTest
```

Test reports are generated under:

```text
target/surefire-reports
```

## Building the application

To compile, test and package the backend, run:

### Windows

```powershell
.\mvnw.cmd clean package
```

### macOS or Linux

```bash
./mvnw clean package
```

The generated executable JAR will be located in:

```text
target/backend_thesis-0.0.1-SNAPSHOT.jar
```

Run the packaged application with:

```bash
java -jar target/backend_thesis-0.0.1-SNAPSHOT.jar
```

## Troubleshooting

### Port 8080 is already in use

If the application reports:

```text
Web server failed to start. Port 8080 was already in use.
```

stop the previous backend instance before restarting the application.

On Windows, identify the process with:

```powershell
netstat -ano | findstr :8080
```

Then stop it using its PID:

```powershell
taskkill /PID <PID> /F
```

### Swagger returns 401

Make sure that the following routes are publicly permitted in `SecurityConfig`:

```java
"/swagger-ui/**",
"/swagger-ui.html",
"/v3/api-docs/**"
```

If a protected operation returns `401`, obtain a new JWT from the RetroLab login endpoint and authorize Swagger again.

### Database data disappears after stopping

This is expected while using:

```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

The database is recreated for every development execution.

## Project status

RetroLab Backend is an academic project developed as part of a UOC Final Degree Project.

## Author
**Manuel Pérez Feria**
Universitat Oberta de Catalunya
