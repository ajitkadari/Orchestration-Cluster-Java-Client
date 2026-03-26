# Orchestration Cluster Java Client

A Spring Boot application that provides **REST and SOAP API endpoints** for interacting with **Camunda 8 SaaS** (Orchestration Cluster) using the official [Camunda Java Client](https://docs.camunda.io/docs/apis-tools/java-client/). It exposes endpoints for searching and evaluating DMN decision definitions.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
  - [Environment Variables](#environment-variables)
  - [Network Access Requirements](#network-access-requirements)
  - [Setting Environment Variables](#setting-environment-variables)
    - [macOS / Linux](#macos--linux)
    - [Windows](#windows)
  - [Using direnv (macOS / Linux)](#using-direnv-macos--linux)
- [Running the Application](#running-the-application)
- [Swagger UI / OpenAPI Docs](#swagger-ui--openapi-docs)
- [REST API Endpoints](#rest-api-endpoints)
  - [Search Decision Definitions](#search-decision-definitions)
  - [Evaluate a Decision Definition](#evaluate-a-decision-definition)
- [SOAP Endpoint](#soap-endpoint)
  - [WSDL Access](#wsdl-access)
  - [SOAP Request Example](#soap-request-example)
  - [SOAP Response Example](#soap-response-example)
- [Request & Response Models](#request--response-models)
- [Running Tests](#running-tests)
- [Building a JAR](#building-a-jar)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

This application acts as a **Java-based HTTP and SOAP client/proxy** for the Camunda 8 Orchestration Cluster. It is useful for:

- **Searching deployed DMN decision definitions** by ID, name, or retrieving all definitions.
- **Evaluating decision definitions** (DMN) with input variables and returning the result.
- **Exposing SOAP/WSDL endpoints** for enterprise integrations requiring SOAP protocol.
- Serving as a **reference implementation** for integrating the Camunda Java Client (`camunda-client-java`) into a Spring Boot application.

---

## Tech Stack

| Technology | Version |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.3 |
| Spring Web Services | 5.0.0 |
| Bean Validation | Hibernate Validator (via `spring-boot-starter-validation`) |
| Camunda Java Client | 8.8.16 |
| springdoc-openapi (Swagger UI) | 2.8.6 |
| Maven | 3.x (via wrapper `mvnw`) |
| WSDL4J | 1.6.3 |

---

## Project Structure

```
src/
├── main/
│   ├── java/org/camunda/consulting/
│   │   ├── OrchestrationClusterClientApplication.java      # Spring Boot entry point
│   │   ├── CamundaClientConfiguration.java               # CamundaClient bean configuration
│   │   ├── DecisionEvaluationService.java                 # Business logic service (search & evaluate)
│   │   ├── OpenApiConfig.java                             # Swagger / OpenAPI UI configuration
│   │   ├── NTdecisionDTO.java                             # Request DTO for evaluation endpoint
│   │   ├── DecisionVariables.java                         # Nested variables DTO (team, state)
│   │   │
│   │   ├── rest/
│   │   │   └── DecisionDefinitionController.java          # REST controller (search + evaluate)
│   │   │
│   │   └── soap/
│   │       ├── SoapWebServiceConfig.java                  # SOAP servlet + WSDL configuration
│   │       ├── DecisionEvaluationSoapEndpoint.java        # SOAP endpoint implementation
│   │       └── model/
│   │           ├── EvaluateDecisionRequest.java           # SOAP request model
│   │           ├── EvaluateDecisionResponse.java          # SOAP response model
│   │           └── SoapDecisionVariables.java             # SOAP variables model
│   │
│   └── resources/
│       ├── application.yaml                               # App config (reads from env vars)
│       └── decision-evaluation.xsd                        # SOAP schema for WSDL generation
│
└── test/
    └── java/org/camunda/consulting/
        ├── OrchestrationClusterClientApplicationTests.java # Application smoke test (contextLoads)
        ├── rest/
        │   └── DecisionDefinitionControllerTest.java       # REST endpoint tests
        └── soap/
            └── DecisionEvaluationSoapEndpointTest.java    # SOAP endpoint tests
```

---

## Prerequisites

- **Java 21** or later
- **Maven 3.x** (or use the included `./mvnw` wrapper)
- A **Camunda 8 SaaS** account with:
  - A running cluster
  - An M2M API client (Client ID + Client Secret)

---

## Configuration

### Environment Variables

The application reads the following **environment variables** at startup. These must be set before running the application.

| Environment Variable    | Description                                   | Example Value                          |
|-------------------------|-----------------------------------------------|----------------------------------------|
| `CAMUNDA_CLUSTER_ID`    | The UUID of your Camunda SaaS cluster         | `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |
| `CAMUNDA_CLUSTER_REGION`| The region where your cluster is deployed     | `cle-1`                                |
| `CAMUNDA_CLIENT_ID`     | OAuth M2M application Client ID               | `your-client-id`                       |
| `CAMUNDA_CLIENT_SECRET` | OAuth M2M application Client Secret           | `your-client-secret`                   |

These map to the following entries in `application.yaml`:

```yaml
camunda:
  cluster:
    # Loaded from environment variables (for example via direnv/.envrc or shell export)
    id: ${CAMUNDA_CLUSTER_ID}
    region: ${CAMUNDA_CLUSTER_REGION}
  client:
    # Loaded from environment variables (for example via direnv/.envrc or shell export)
    id: ${CAMUNDA_CLIENT_ID}
    secret: ${CAMUNDA_CLIENT_SECRET}
```

---

### Network Access Requirements

To access Camunda 8 SaaS Orchestration APIs, your network must allow outbound access to the following endpoints:

- `ZEEBE_ADDRESS`
  - Format: `{CAMUNDA_CLUSTER_ID}.{CAMUNDA_CLUSTER_REGION}.zeebe.camunda.io:443`
  - This value is cluster-specific.
- `CAMUNDA_OAUTH_URL`
  - Value: `https://login.cloud.camunda.io/oauth/token`
  - This value is constant for the Camunda SaaS environment (not cluster-specific).
  - This endpoint is used by the client to obtain OAuth tokens.

---

### Setting Environment Variables

#### macOS / Linux

**Option 1 — Export in the current terminal session (temporary):**

```bash
export CAMUNDA_CLUSTER_ID=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
export CAMUNDA_CLUSTER_REGION=your-cluster-region
export CAMUNDA_CLIENT_ID=your-client-id
export CAMUNDA_CLIENT_SECRET=your-client-secret
```

These values are lost when the terminal session ends.

**Option 2 — Persist in your shell profile (permanent):**

Add the `export` lines above to your shell profile file (`~/.zshrc` for Zsh, `~/.bashrc` or `~/.bash_profile` for Bash), then reload it:

```bash
source ~/.zshrc   # or: source ~/.bashrc
```

**Option 3 — Pass inline when starting the app:**

```bash
CAMUNDA_CLUSTER_ID=xxx \
CAMUNDA_CLUSTER_REGION=yyy \
CAMUNDA_CLIENT_ID=aaa \
CAMUNDA_CLIENT_SECRET=bbb \
./mvnw clean spring-boot:run
```

---

#### Windows

**Option 1 — Set in the current Command Prompt session (temporary):**

```cmd
set CAMUNDA_CLUSTER_ID=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
set CAMUNDA_CLUSTER_REGION=your-cluster-region
set CAMUNDA_CLIENT_ID=your-client-id
set CAMUNDA_CLIENT_SECRET=your-client-secret
```

**Option 2 — Set in the current PowerShell session (temporary):**

```powershell
$env:CAMUNDA_CLUSTER_ID     = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
$env:CAMUNDA_CLUSTER_REGION = "your-cluster-region"
$env:CAMUNDA_CLIENT_ID      = "your-client-id"
$env:CAMUNDA_CLIENT_SECRET  = "your-client-secret"
```

**Option 3 — Persist as User environment variables (permanent, GUI):**

1. Open **Settings → System → About → Advanced system settings → Environment Variables**.
2. Under **User variables**, click **New** and add each variable name and value.
3. Click **OK**, then restart any open terminals or your IDE for the changes to take effect.

**Option 4 — Persist via PowerShell (permanent, scripted):**

```powershell
[System.Environment]::SetEnvironmentVariable("CAMUNDA_CLUSTER_ID",     "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", "User")
[System.Environment]::SetEnvironmentVariable("CAMUNDA_CLUSTER_REGION", "your-cluster-region",              "User")
[System.Environment]::SetEnvironmentVariable("CAMUNDA_CLIENT_ID",      "your-client-id",     "User")
[System.Environment]::SetEnvironmentVariable("CAMUNDA_CLIENT_SECRET",  "your-client-secret", "User")
```

Then run the application from Command Prompt or PowerShell:

```cmd
mvnw.cmd clean spring-boot:run
```

> **Note (all platforms):** If launching from an IDE (e.g., IntelliJ), the IDE may not inherit shell-level environment variables. Either configure them in the IDE's **Run/Debug Configuration → Environment variables** field, or start the application from the terminal to ensure they are correctly loaded.

---

### Using direnv (macOS / Linux)

The recommended way to manage environment variables locally on macOS/Linux is with [direnv](https://direnv.net/). It automatically loads and unloads variables when you enter/leave the project directory.

1. **Install direnv:**
   ```bash
   brew install direnv
   ```

2. **Add the hook to your shell** (e.g., `~/.zshrc`):
   ```bash
   eval "$(direnv hook zsh)"
   ```

3. **Create a `.envrc` file** in the project root:
   ```bash
   export CAMUNDA_CLUSTER_ID=your-cluster-id
   export CAMUNDA_CLUSTER_REGION=your-cluster-region
   export CAMUNDA_CLIENT_ID=your-client-id
   export CAMUNDA_CLIENT_SECRET=your-client-secret
   ```

4. **Allow direnv to load it:**
   ```bash
   direnv allow .
   ```

> **Note:** direnv is not available natively on Windows. Windows users should use one of the permanent variable options described above.

---

## Running the Application

### Using Maven

```bash
cd "/Users/ajit.kadari/github-local/Orchestration-Cluster-Java-Client"
./mvnw clean spring-boot:run
```

The application starts on **http://localhost:8080** by default.

---

## Swagger UI / OpenAPI Docs

Once the application is running, interactive API documentation is available via **Swagger UI** (provided by [springdoc-openapi](https://springdoc.org/)):

| URL | Description |
|-----|-------------|
| `http://localhost:8080/swagger-ui/index.html` | Interactive Swagger UI (canonical URL) |
| `http://localhost:8080/swagger-ui.html` | Interactive Swagger UI |
| `http://localhost:8080/v3/api-docs` | OpenAPI spec (JSON) |
| `http://localhost:8080/v3/api-docs.yaml` | OpenAPI spec (YAML) |

The Swagger UI lets you explore and test all endpoints directly from the browser without needing a separate HTTP client.

---

## REST API Endpoints

All REST endpoints are available under both `/decision-definitions` and `/v2/decision-definitions` prefixes for backward compatibility.

---

### Search Decision Definitions

#### Search All

```
GET /v2/decision-definitions/search
```

Returns a list of all deployed decision definitions in the cluster.

**Success Response:** `200 OK` with an array of decision definition objects.

**Example Response:**
```json
[
  {
    "decisionDefinitionId": "decision-1",
    "name": "Approval Decision",
    "key": "approval"
  }
]
```

---

#### Search by Name

```
GET /v2/decision-definitions/search/by-name/{name}
```

| Path Parameter | Description                        |
|----------------|------------------------------------|
| `name`         | The name of the decision definition |

Returns decision definitions matching the given name.

**Example:**
```bash
curl http://localhost:8080/v2/decision-definitions/search/by-name/Approval%20Decision
```

---

#### Search by Decision Definition ID

```
GET /v2/decision-definitions/search/by-id/{id}
```

| Path Parameter | Description                                  |
|----------------|----------------------------------------------|
| `id`           | The DMN decision definition ID (not the key) |

Returns a decision definition matching the given ID.

**Example:**
```bash
curl http://localhost:8080/v2/decision-definitions/search/by-id/decision-1
```

---

### Evaluate a Decision Definition

```
POST /v2/decision-definitions/evaluation
Content-Type: application/json
```

Evaluates a DMN decision with the provided input variables.

**Request Body:**

```json
{
  "decisionDefinitionId": "myDecisionId",
  "decisionDefinitionKey": "",
  "decisionVariables": {
    "team": "engineering",
    "state": "active"
  }
}
```

| Field                  | Type   | Required | Description                                                                 |
|------------------------|--------|----------|-----------------------------------------------------------------------------|
| `decisionDefinitionId` | String | Either/Or| The DMN decision ID (takes priority over `decisionDefinitionKey`)           |
| `decisionDefinitionKey`| String | Either/Or| The numeric cluster key or DMN ID (used if `decisionDefinitionId` is blank) |
| `decisionVariables`    | Object | Optional | Input variables to pass to the decision engine                              |
| `decisionVariables.team` | String | Optional | Team input variable                                                       |
| `decisionVariables.state`| String | Optional| State input variable                                                       |

> **Note:** Either `decisionDefinitionId` or `decisionDefinitionKey` must be provided.

**Success Response:** `200 OK` with the decision evaluation result from Camunda.

**Error Responses:**
- `400 Bad Request`: Missing both ID and key, or invalid request format.
- `500 Internal Server Error`: Error during decision evaluation.

---

## SOAP Endpoint

SOAP is exposed under `/ws/*`.

### WSDL Access

| URL | Description |
|-----|-------------|
| `http://localhost:8080/ws/decisionEvaluation.wsdl` | Generated WSDL for decision evaluation |

The WSDL can be imported into SOAP clients like **SoapUI**, **Postman**, or **Insomnia** for testing.

---

### SOAP Operation

- **Request element:** `evaluateDecisionRequest`
- **Response element:** `evaluateDecisionResponse`
- **Namespace:** `http://camunda.org/consulting/decision-evaluation`
- **Port:** `/ws`

---

### SOAP Request Example

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:dec="http://camunda.org/consulting/decision-evaluation">
  <soapenv:Header/>
  <soapenv:Body>
    <dec:evaluateDecisionRequest>
      <dec:decisionDefinitionId>myDecisionId</dec:decisionDefinitionId>
      <dec:decisionVariables>
        <dec:team>engineering</dec:team>
        <dec:state>active</dec:state>
      </dec:decisionVariables>
    </dec:evaluateDecisionRequest>
  </soapenv:Body>
</soapenv:Envelope>
```

---

### SOAP Response Example

**Success Response:**
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
  <SOAP-ENV:Body>
    <ns2:evaluateDecisionResponse xmlns:ns2="http://camunda.org/consulting/decision-evaluation">
      <ns2:success>true</ns2:success>
      <ns2:result>{"evaluationResult":"approved","executionId":"exec-123"}</ns2:result>
    </ns2:evaluateDecisionResponse>
  </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

**Error Response:**
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
  <SOAP-ENV:Body>
    <ns2:evaluateDecisionResponse xmlns:ns2="http://camunda.org/consulting/decision-evaluation">
      <ns2:success>false</ns2:success>
      <ns2:errorMessage>Either decisionDefinitionId or decisionDefinitionKey must be provided.</ns2:errorMessage>
    </ns2:evaluateDecisionResponse>
  </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

---

## Request & Response Models

### `NTdecisionDTO`

Request model for decision evaluation.

```json
{
  "decisionDefinitionId": "string (optional)",
  "decisionDefinitionKey": "string (optional)",
  "decisionVariables": {
    "team": "string (optional)",
    "state": "string (optional)"
  }
}
```

### `DecisionVariables`

Nested variables object for decision input.

```json
{
  "team": "string",
  "state": "string"
}
```

### SOAP Models

**EvaluateDecisionRequest**
```xml
<dec:evaluateDecisionRequest>
  <dec:decisionDefinitionId>string</dec:decisionDefinitionId>
  <dec:decisionDefinitionKey>string</dec:decisionDefinitionKey>
  <dec:decisionVariables>
    <dec:team>string</dec:team>
    <dec:state>string</dec:state>
  </dec:decisionVariables>
</dec:evaluateDecisionRequest>
```

**EvaluateDecisionResponse**
```xml
<dec:evaluateDecisionResponse>
  <dec:success>boolean</dec:success>
  <dec:result>string (JSON)</dec:result>
  <dec:errorMessage>string</dec:errorMessage>
</dec:evaluateDecisionResponse>
```

---

## Running Tests

### Unit & Integration Tests

```bash
cd "/Users/ajit.kadari/github-local/Orchestration-Cluster-Java-Client"
./mvnw test
```

### Test Coverage

Tests include:
- **REST endpoint tests** for all search operations (`/search`, `/search/by-name/{name}`, `/search/by-id/{id}`)
- **REST endpoint tests** for decision evaluation (`/evaluation`)
- **Error handling tests** for missing identifiers and runtime exceptions
- **SOAP endpoint tests** for success and error scenarios

### Running Specific Tests

```bash
./mvnw test -Dtest=DecisionDefinitionControllerTest
./mvnw test -Dtest=DecisionEvaluationSoapEndpointTest
./mvnw test -Dtest=OrchestrationClusterClientApplicationTests
```

---

## Building a JAR

### Full Build (with Tests)

```bash
cd "/Users/ajit.kadari/github-local/Orchestration-Cluster-Java-Client"
./mvnw clean package
```

### Fast Build (Skip Tests)

```bash
./mvnw clean package -DskipTests
```

This creates a JAR at:
```
target/orchestration-cluster-java-client-0.0.1-SNAPSHOT.jar
```

### Run the JAR

```bash
java -jar target/orchestration-cluster-java-client-0.0.1-SNAPSHOT.jar
```

---

## Maven Build Phases

| Command | Description |
|---------|-------------|
| `./mvnw clean` | Removes `target/` directory |
| `./mvnw compile` | Compiles main source code |
| `./mvnw test` | Runs unit and integration tests |
| `./mvnw package` | Builds JAR artifact |
| `./mvnw clean spring-boot:run` | Cleans and runs the app in dev mode |
| `./mvnw dependency:tree` | Shows dependency tree |
| `./mvnw -DskipTests clean package` | Skips tests during build |

---

## Troubleshooting

### Application fails to start: Missing environment variables

**Error:**
```
Could not resolve placeholder 'CAMUNDA_CLUSTER_ID' in value "${CAMUNDA_CLUSTER_ID}"
```

**Solution:**
Ensure all required environment variables are set before starting:
```bash
export CAMUNDA_CLUSTER_ID=xxx
export CAMUNDA_CLUSTER_REGION=yyy
export CAMUNDA_CLIENT_ID=aaa
export CAMUNDA_CLIENT_SECRET=bbb
./mvnw spring-boot:run
```

Or use direnv (see [Using direnv](#using-direnv) section).

### Swagger UI shows "Failed to fetch OpenAPI spec"

**Solution:**
- Ensure the app is running on port 8080.
- Check browser console for CORS errors.
- Verify `/v3/api-docs` is accessible: `curl http://localhost:8080/v3/api-docs`

### WSDL not accessible

**Error:**
```
HTTP 404 - Not Found for WSDL
```

**Solution:**
- Verify the app is running.
- Ensure SOAP servlet is mapped to `/ws/*`.
- Check logs for SOAP configuration errors.
- Try: `curl http://localhost:8080/ws/decisionEvaluation.wsdl`

---

## Contributing

Contributions are welcome! Please:

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/my-feature`).
3. Commit your changes (`git commit -m 'Add my feature'`).
4. Push to the branch (`git push origin feature/my-feature`).
5. Open a Pull Request.

---

## License

This project currently does not include a `LICENSE` file in the repository. Add one (for example Apache 2.0) before publishing or sharing externally.

---

## Additional Resources

- [Camunda 8 Documentation](https://docs.camunda.io/)
- [Camunda Java Client GitHub](https://github.com/camunda/camunda-bpm-client-java)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Web Services Documentation](https://spring.io/projects/spring-ws)
- [OpenAPI 3.0 Specification](https://spec.openapis.org/oas/v3.0.3)

---

**Last Updated:** March 2026

