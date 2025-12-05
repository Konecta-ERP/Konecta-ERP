# Document Processor (Docker Version)

## How to Build and Run

1. Make sure Docker Desktop is running.

2. Build the image:
   docker build -t invoice-processor .

3. Run the container:
   docker run -p 8000:8000 --env-file .env invoice-processor

4. Test in browser:
   http://localhost:8000/health  → {"status":"ok"}
   http://localhost:8000/docs    → API documentation

## API Endpoints

- POST /process-invoice  
  → Send a document (PDF, image, etc.) and receive extracted data in JSON.

## Example request

POST http://localhost:8000/process-invoice
Content-Type: multipart/form-data
file=<your file>

Response: JSON with invoice fields.

