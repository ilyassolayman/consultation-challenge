# MedExpress Consultation API — Genovian Pear Allergy MVP

This repository contains the backend REST API for the MedExpress consultation flow, initially targeting the Genovian Pear Allergy medication.

---

## Running Locally & Exercising the API

See [TESTING_GUIDE.md](./TESTING_GUIDE.md) for startup instructions and a full set of cURL commands covering all happy and unhappy paths.

---

## Architectural Notes & Trade-offs

### 1. Anchoring to `productId` (YAGNI)

The brief describes treating a specific *condition*, but the API and domain model are anchored around `productId`. From both a clinical and billing perspective, contraindications and subscriptions are tied to the specific medication (chemical substance), not the diagnosis alone. Implementing a full Condition-to-Product relational model for a single-product MVP would be premature in my opinion. Anchoring to `productId` solves the immediate business need while leaving the model straightforward to extend.

### 2. Extensibility via Strategy, Template Method, and a Lightweight Rule Structure

Eligibility evaluation is built from three composing patterns:

**Strategy Pattern — routing across products**

**Template Method Pattern — shared evaluation algorithm**

**Lightweight Rule Structure — towards a rules engine**

Each `EligibilityRule` encapsulates a predicate (the condition), a reason string, and a `RuleSeverity`. The `evaluate()` pipeline:
1. Evaluates all rules and collects every match, not just the first, so a patient with multiple contraindications receives a complete picture.
2. Finds the highest severity among matched rules (`CRITICAL` > `HIGH` > `MEDIUM` > `LOW`).
3. Maps that severity to a `ConsultationStatus`: `CRITICAL`/`HIGH` → `REJECTED`, `MEDIUM` → `PENDING_CLINICAL_REVIEW`, `LOW` → `APPROVED_WITH_NOTES`, none → `APPROVED`.
4. Derives `eligible` from the status, `true` only for `APPROVED` and `APPROVED_WITH_NOTES`.

Rules are currently defined in code, which requires a deployment to change. The next step toward a full rules engine would be moving the `RULES` list out of the Java class into a database or rules repository. The evaluation pipeline itself would remain unchanged.

### 3. In-Memory Storage

Per the requirements, no persistent database is used. The data layer is simulated with:

- A `Map<String, List<QuestionDto>>` in `QuestionRepository`
- A `ConcurrentHashMap<String, ConsultationResponse>` in `ConsultationRepository`

### 4. API Design Decisions

`POST /consultations` returns `201 Created` with a `Location` header pointing to the new resource. To make this meaningful, I also added a `GET /consultations/{id}` endpoint so the URL in the `Location` header actually resolves.

### 5. Error Handling

All error responses return a structured `ErrorResponse` JSON body with `message` and `timestamp` fields, ensuring a consistent response contract for clients regardless of whether a request succeeds or fails. The `GlobalExceptionHandler` handles the exceptions and returns a friendly error message.

### 6. Boolean Answer Values

Boolean was used for simplicity given all MVP questions are yes/no. The known limitation is extensibility. If future products require multi-select, numeric, or free-text answers, the production upgrade path would cater for this typed DTO subclasses.

### 7. AI Tooling

AI was used as a productivity aid for boilerplate and scaffolding. All architectural decisions, trade-offs, and design choices are my own.

---

## Future Considerations

These items were deliberately descoped to respect the timebox but represent the natural next steps for a production system:

- **Rules Engine:** The current rule structure( declarative `EligibilityRule` objects with severity, collected and evaluated by a generic pipeline ) is a deliberate step toward a rules engine. The remaining step is externalising the `RULES` list from Java code into a database or rules repository (e.g. Drools), so medical staff can update contraindication criteria without code deployments. The evaluation pipeline would not need to change.
- **Idempotency:** The `POST /consultations` endpoint should be made idempotent in production (e.g. via a client-supplied `requestId`) to prevent duplicate consultation records if a user resubmits.
- **Persistent Storage:** Replacing the in-memory maps with a proper database is a minimal change given the existing repository abstraction.
- **Condition–Product Modelling:** A future multi-condition, multi-product catalogue would introduce a `Condition` entity with a one-to-many relationship to `Product`, at which point the domain model would be revisited.
