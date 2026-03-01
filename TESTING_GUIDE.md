# API Testing Guide

## Starting the Application

From the project root (`/consultation`), run:

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`. Wait for a log line containing `Started ConsultationApplication` before sending any requests.

---

## Testing Options

### Option 1: curl (recommended)

The commands below cover all happy and unhappy paths. They pipe output through `jq` for readable JSON — install it via `brew install jq` if needed, or omit `| jq` if pretty json is not desired. Alternatively you can use `| python3 -m json.tool` if you have python installed.

### Option 2: Swagger UI

An interactive API explorer is available at:

```
http://localhost:8080/swagger-ui.html
```

All endpoints can be explored and executed directly from the browser with no additional tooling required.

---

## Happy Path Tests

### 1. Fetch consultation questions

Retrieves the list of questions a patient must answer before submitting a consultation for a given product. Verifies the questions endpoint returns the correct questions for `pear-allergy-med`.

```bash
curl -s http://localhost:8080/api/v1/products/pear-allergy-med/questions | jq
```

**Expected:** `200 OK` with an array of 3 questions (`q1`, `q2`, `q3`).

---

### 2. Submit a consultation — eligible outcome

Submits a consultation where the patient answers `false` to all questions, meaning no contraindications are present. Verifies the system correctly approves the patient.

```bash
curl -s -X POST http://localhost:8080/api/v1/consultations \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "pear-allergy-med",
    "customerId": "user-123",
    "answers": [
      { "questionId": "q1", "value": false },
      { "questionId": "q2", "value": false },
      { "questionId": "q3", "value": false }
    ]
  }' | jq
```

**Expected:** `201 Created` with `eligible: true`, `status: "APPROVED"`, and no `clinicalNotes` field.

---

### 3. Submit a consultation — single reason, REJECTED

Submits a consultation where the patient answers `true` to q1 (prior adverse reaction to Genovian Pear extract), which is a critical contraindication. Verifies the system rejects the patient with a single reason.

```bash
curl -s -X POST http://localhost:8080/api/v1/consultations \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "pear-allergy-med",
    "customerId": "user-456",
    "answers": [
      { "questionId": "q1", "value": true },
      { "questionId": "q2", "value": false },
      { "questionId": "q3", "value": false }
    ]
  }' | jq
```

**Expected:** `201 Created` with `eligible: false`, `status: "REJECTED"`, and one entry in `clinicalNotes`.

---

### 4. Submit a consultation — two reasons, PENDING_CLINICAL_REVIEW

Submits a consultation where the patient answers `true` to q2 (currently taking antihistamines) and q3 (history of food-allergen-triggered respiratory conditions), but `false` to q1. Both rules fire — the highest severity across the two is MEDIUM (q3), so the case is escalated for clinical review rather than outright rejected. Verifies the system returns both clinical notes and the correct status.

```bash
curl -s -X POST http://localhost:8080/api/v1/consultations \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "pear-allergy-med",
    "customerId": "user-789",
    "answers": [
      { "questionId": "q1", "value": false },
      { "questionId": "q2", "value": true },
      { "questionId": "q3", "value": true }
    ]
  }' | jq
```

**Expected:** `201 Created` with `eligible: false`, `status: "PENDING_CLINICAL_REVIEW"`, and two entries in `clinicalNotes`.

---

### 5. Retrieve a consultation by ID

Fetches a previously submitted consultation by its ID. Copy the `consultationId` value from the response body of any submission above and substitute it below.

```bash
curl -s http://localhost:8080/api/v1/consultations/{consultationId} | jq
```

**Expected:** `200 OK` with the full consultation record matching the original submission.

---

## Unhappy Path Tests

### 6. Unknown product ID → 404

Submits a consultation with a `productId` that the system has no eligibility rules for. Verifies the API returns a 404 with a user-friendly message rather than a 500.

```bash
curl -s -X POST http://localhost:8080/api/v1/consultations \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "unknown-product",
    "customerId": "user-123",
    "answers": [
      { "questionId": "q1", "value": false }
    ]
  }' | jq
```

**Expected:** `404 Not Found` with `message: "Product not found: unknown-product"`.

---

### 7. Retrieve a consultation with an unknown ID → 404

Attempts to fetch a consultation using an ID that does not exist in the system. Verifies the API returns a 404 rather than an empty response.

```bash
curl -s http://localhost:8080/api/v1/consultations/unknown-id | jq
```

**Expected:** `404 Not Found`.

---

### 8. Missing required field → 400

Submits a consultation without the required `productId` field. Verifies that Bean Validation catches the missing field and returns a descriptive error message.

```bash
curl -s -X POST http://localhost:8080/api/v1/consultations \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "user-123",
    "answers": [
      { "questionId": "q1", "value": false }
    ]
  }' | jq
```

**Expected:** `400 Bad Request` with a `message` containing `"Validation failed"` and the name of the missing field.

---

### 9. Empty answers list → 400

Submits a consultation with an empty `answers` array. Verifies that Bean Validation rejects the request before it reaches any business logic.

```bash
curl -s -X POST http://localhost:8080/api/v1/consultations \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "pear-allergy-med",
    "customerId": "user-123",
    "answers": []
  }' | jq
```

**Expected:** `400 Bad Request` with a `message` containing `"Validation failed"`.

---

### 10. Malformed JSON body → 400

Sends a request with a body that is not valid JSON. Verifies the API returns a 400 with a clear message rather than exposing an internal parsing error.

```bash
curl -s -X POST http://localhost:8080/api/v1/consultations \
  -H "Content-Type: application/json" \
  -d 'this is not json' | jq
```

**Expected:** `400 Bad Request` with `message: "Request body is missing or malformed"`.

---

### 11. Wrong Content-Type → 415

Sends a request with `Content-Type: text/plain` instead of `application/json`. Verifies the API rejects the request with a 415 and tells the client what content type is expected.

```bash
curl -s -X POST http://localhost:8080/api/v1/consultations \
  -H "Content-Type: text/plain" \
  -d 'hello' | jq
```

**Expected:** `415 Unsupported Media Type` with a `message` indicating `application/json` should be used.

---

### 12. Wrong HTTP method → 405

Sends a POST request to an endpoint that only supports GET. Verifies the API returns a 405 that names the unsupported method.

```bash
curl -s -X POST http://localhost:8080/api/v1/consultations/some-id | jq
```

**Expected:** `405 Method Not Allowed` with a `message` containing `"POST"`.

---

### 13. Unknown URL → 404

Requests a URL that does not exist in the API. Verifies the API returns a consistent 404 error response rather than the default Spring error page.

```bash
curl -s http://localhost:8080/api/v1/does-not-exist | jq
```

**Expected:** `404 Not Found` with `message: "The requested endpoint does not exist"`.
