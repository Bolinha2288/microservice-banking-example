This project consists of multiple services (`email-manager`, `account-manager`, and `user-manager`) that can be deployed using Docker and Docker Compose.

## Prerequisites

1. Install [Docker](https://docs.docker.com/get-docker/).
2. Install [Docker Compose](https://docs.docker.com/compose/install/).
3. Ensure the JAR files for each service are built using Maven:
   ```bash
   cd account-manager
   mvn clean package
   ``` 
   
   ```bash
   cd users-manager
   mvn clean package
   ```
## Running the Services with Docker Compose

1. Navigate to the **infrastructure** directory where the `docker-compose.yaml` file is located:
    ```bash
    cd infrastructure
    ```
2. Start the services using Docker Compose:
    ```bash
    docker-compose up --build -d
    ```
3. Checking the logs all services are running:
   ```bash
    docker-compose logs -f
    ```
## Accessing the Services


1. Create new user through swagger:

    - **User Manager:** http://localhost:8090/swagger-ui/index.html

2. Check the Circuit Breaker using actuator:

    - **Circuit Breaker Actuator:** http://localhost:8090/actuator/circuitbreakers


## Testing Circuit Breaker


![Check on open](docs/img/circuit-breaker-open.png)

![Check on half open](docs/img/circuit-breaker-half-open.png)

![Check on closed](docs/img/circuit-breaker-closed.png)
