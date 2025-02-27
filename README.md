# SwiftCode API

## Summary
This fullstack application is designed to manage and decode SWIFT codes for banking systems. It provides a RESTful API built with Spring Boot and MongoDB, allowing users to interact with SWIFT code data efficiently. The project emphasizes clean code practices, security, and containerization with Docker. It includes unit tests and JaCoCo for code coverage analysis.

## Technologies Used

### Backend
![Java](https://img.shields.io/badge/Java-21-blue)  
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen)  
![Maven](https://img.shields.io/badge/Maven-4.0.0-red)  
![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green)

### Tools
![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)  
![JaCoCo](https://img.shields.io/badge/JaCoCo-0.8.10-yellow)

## Prerequisites
- ![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white) (Ensure Docker and Docker Compose are installed)

## How to Run the App

1. **Clone the repository:**
    ```sh
    git clone https://github.com/your-username/swiftcode-decoder-api.git
    cd swiftcode-decoder-api
    ```
2. **Run the app with Docker Compose:**
    ```sh
    cd docker
    docker-compose up
    ```
3. **Access the app:**
    ```sh
    http://localhost:8080
    ```
4. **Stop the app:**
    ```sh
    docker-compose down
    ```
## Accessing the Swagger UI
Once the application is running, explore the API documentation via Swagger UI at:

```sh
http://localhost:8080/swagger-ui.html
```
## Project Structure
- **`swiftcode-decoder-api/`**: Contains the Spring Boot application and `Dockerfile`.
- **`docker/`**: Contains the `docker-compose.yml` file for orchestrating the API and MongoDB services.

## Configuration
The application uses the following configurations (defined in `application.properties`):
- **Application Name**: SwiftCode API
- **MongoDB**:
  - Host: `bank-api-db`
  - Port: `27017`
  - Database: `bank_database`
  - Username: `root`
  - Password: `root123`
- **OpenAPI**:
  - Title: SWIFT Code API
  - Version: 1.0.0
  - Description: API for bank-SwiftCode operations
  - Server URL: `http://localhost:8080`

## Tests
- **Unit Tests**: Implemented using JUnit and Mockito to ensure backend reliability.
- **Code Coverage**: JaCoCo (version 0.8.10) is integrated to measure test coverage. Run the following to generate a report:
    ```sh
    mvn verify
    ```

## Docker setup
Docker Setup
- **API Service:** Built from the Dockerfile in swiftcode-decoder-api/, exposing port 8080.
- **MongoDB Service:** Uses the mongo:7.0.16-jammy image, exposing port 27017, with persistent data stored in a Docker volume (mongo_data).

## Notes
- Modify the spring.data.mongodb.* properties in application.properties if you need to connect to a different MongoDB instance.