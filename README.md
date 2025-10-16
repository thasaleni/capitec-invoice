# Capitec Invoice App — Help

This project is a Spring Boot 3 application for managing invoices with a clean architecture layout (domain, application, adapters). It exposes a REST API, serves a simple UI from static resources, persists to an in‑memory H2 database, and can render an invoice PDF using OpenHTMLtoPDF + Thymeleaf.

## Prerequisites
- Java 17+
- Internet access for Maven Central (first build)

## Build & Run
- Build: `./gradlew clean build`
- Run: `./gradlew bootRun`
- Jar: `./gradlew bootJar` then `java -jar build/libs/invoice-0.0.1-SNAPSHOT.jar`

Server starts on port 8080 (configurable via `server.port`).

## Data & Database
- In‑memory H2 database is used in dev/test: `jdbc:h2:mem:invoicedb`
- Schema is created by Hibernate (`spring.jpa.hibernate.ddl-auto=create-drop`)
- `data.sql` seeds sample data (3 invoices) and advances the ID sequence
- H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:invoicedb`, user: `sa`, no password)

## UI
- A minimal UI is served from static resources: http://localhost:8080/
- WebJars are used for Vue (no external CDN needed)

## REST API
Base path: `/api`

- GET `/api/invoices` — List invoices
- GET `/api/invoices/{id}` — Get invoice by id
- POST `/api/invoices` — Create invoice
- PUT `/api/invoices/{id}` — Update invoice
- DELETE `/api/invoices/{id}` — Delete invoice
- POST `/api/invoices/{id}/pay` — Record a payment `{ "amount": 100.00 }`
- GET `/api/invoices/overdue` — List overdue invoices
- GET `/api/summary` — Summary aggregates
- GET `/api/invoices/{id}/pdf` — Render invoice PDF (Content-Type: application/pdf)

### Sample requests
- List: `curl -s http://localhost:8080/api/invoices | jq .`
- Get: `curl -s http://localhost:8080/api/invoices/1 | jq .`
- Create:
  curl -s -X POST http://localhost:8080/api/invoices \
    -H 'Content-Type: application/json' \
    -d '{
      "invoiceNumber":"INV-NEW",
      "customerName":"New Co",
      "issueDate":"2025-10-01",
      "dueDate":"2025-10-31",
      "items":[{"description":"Service","quantity":2,"unitPrice":50.00}]
    }'
- Pay: `curl -s -X POST http://localhost:8080/api/invoices/1/pay -H 'Content-Type: application/json' -d '{"amount":50}'`
- PDF (saves to file): `curl -L http://localhost:8080/api/invoices/1/pdf -o invoice-1.pdf`

Notes:
- Status auto-updates based on amount paid, due date, etc. Possible values: UNPAID, PARTIALLY_PAID, OVERDUE, PAID.
- PDF rendering relies on Thymeleaf template `templates/invoice-pdf.html` and OpenHTMLtoPDF. If running in restricted environments, font or rendering warnings may appear but the endpoint should still return a PDF.

## Configuration
Key properties (see `src/main/resources/application.properties`):
- `spring.jpa.defer-datasource-initialization=true` ensures `data.sql` runs after schema creation
- `spring.h2.console.enabled=true` and `spring.h2.console.path=/h2-console`
- `spring.web.cors.allowed-*` are opened for development
- Thymeleaf mode set to HTML; caching disabled for easier development

## Tests
Run all tests: `./gradlew test`

Included tests:
- Unit: `InvoiceServiceTests` (service logic)
- Domain: `InvoiceDomainTests` (domain calculations)
- Integration: `InvoiceControllerIT` (MockMvc REST API)
- Context: `InvoiceApplicationTests` (boot context)

You may see harmless HotSpot VM sharing warnings during test runs; they do not affect test results.

## Docker
Build image:
- `docker build -t capitec-invoice .`

Run container (maps port 8080):
- `docker run --rm -p 8080:8080 capitec-invoice`

Optional: pass JVM options (memory, GC, etc.):
- `docker run --rm -e JAVA_OPTS="-Xms256m -Xmx512m" -p 8080:8080 capitec-invoice`

The app will be available at http://localhost:8080/ and H2 console at http://localhost:8080/h2-console.

## Dependencies (high level)
- Spring Boot Starters: Web, Data JPA, Validation, Thymeleaf
- Database: H2 (runtime)
- PDF: OpenHTMLtoPDF (core, pdfbox, slf4j)
- Front-end libs via WebJars: vue
- Testing: spring-boot-starter-test

## Useful Links
- Spring Boot Gradle Plugin: https://docs.spring.io/spring-boot/3.5.6/gradle-plugin
- Spring Data JPA reference: https://docs.spring.io/spring-boot/3.5.6/reference/data/sql.html#data.sql.jpa-and-spring-data
- Spring MVC reference: https://docs.spring.io/spring-boot/3.5.6/reference/web/servlet.html
- OpenHTMLtoPDF: https://openhtmltopdf.com/

