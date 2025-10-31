# User Identity and Access Management Service
This service is the central hub for authentication, authorization, and user management for the Konecta ERP system. 
It provides secure, stateless authentication using JSON Web Tokens (JWTs) with RSA signatures, 
handles role-based access control (RBAC), and manages the complete user lifecycle.
## How to Start
Follow these steps to get the identity service running locally.
### Step 1: Set Up Environment Variables
This project uses Spring Boot's `.env` file support to externalize database configuration. 
Create a file named `.env` in the root directory of the `identity-service` project.

Add the following three variables, replacing the values with your local database credentials:
```bash
DB_URL=jdbc:postgresql://localhost:5432/your_identity_db
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password
```

Note: The service's `application.yml` is already configured to read these variables.
### Step 2: Generate RSA Keys
This service uses an RS256 asymmetric key pair to sign and validate JWTs. 
The private key signs the tokens, and the public key is exposed for other services to validate them.
1. Navigate to the `src/main/resources/certs` directory.
2. Run the following `openssl` commands:
```bash
# Generate a 2048-bit RSA private key
openssl genrsa -out private.pem 2048

# Extract the public key from the private key
openssl rsa -in private.pem -pubout -out public.pem
```

You should now have `private.pem `and `public.pem` `inside src/main/resources/certs/`.
### Step 3: Run the Application

From the root of the `identity-service` project, run:
```bash
mvn spring-boot:run
```
The service will start, connect to your database, load the RSA keys, and be available through the API gateway at `http://localhost:8080`.