# LexiFlow

**English** | [简体中文](README.zh-CN.md)

LexiFlow: Lexicon in Flow.

A full-stack application for intensive reading and long-term vocabulary learning from English and Japanese articles. Users can upload foreign-language documents, extract and translate their content, analyze vocabulary by proficiency level, save words to personal libraries, and review them with a spaced-learning workflow.

## Features

- User registration, login, JWT authentication, and profile management
- PDF, DOC, DOCX, TXT, Markdown, and HTML article uploads
- Text extraction with Apache Tika and AI OCR fallback for scanned PDFs
- Automatic English and Japanese language detection
- Asynchronous article translation with processing status tracking
- Vocabulary analysis for CET4, CET6, GRE, IELTS, JLPT N1-N5, and other levels
- Reader view with definitions, pronunciation, level, and first-occurrence navigation
- Personal vocabulary libraries and learning-status management
- Review scheduling based on review quality
- Credits account, usage ledger, recharge orders, and Epay callbacks
- Chinese, English, and Japanese interface languages and reading preferences

## Technology Stack

### Frontend

- Vue 3.5
- TypeScript 5.8
- Vite 7
- Pinia
- Vue Router
- TanStack Vue Query
- Axios
- Tailwind CSS 4
- Lucide Icons

### Backend

- Java 21
- Spring Boot 3.5
- Spring MVC / Validation / Scheduling / Async
- MyBatis-Plus
- MySQL 8
- Redis
- JWT
- Apache Tika 3
- Apache PDFBox 3
- Stanford CoreNLP
- LangChain4j
- AWS S3-compatible object storage

## Architecture

```text
Browser
  │
  ├── Pages and static assets ──> Vue / Vite
  │
  └── API requests ─────────────> Spring Boot
                                      ├── MySQL
                                      ├── Redis
                                      ├── S3-compatible storage
                                      ├── AI translation / OCR
                                      └── Epay gateway
```

Uploaded articles are processed asynchronously. The frontend polls the processing endpoint and opens the reader after text extraction and translation finish. Vocabulary analysis also runs asynchronously, so long-running AI and NLP work does not block the HTTP request.

## Project Layout

```text
LexiFlow/
├── backend/                    # Spring Boot backend
│   ├── pom.xml
│   └── src/main/java/com/btea/lexiflow/
│       ├── article/            # Upload, parsing, translation, and analysis
│       ├── common/             # Results, exceptions, context, and cache utilities
│       ├── infrastructure/     # Security, async, storage, and web configuration
│       ├── learning/           # Review scheduling and learning progress
│       ├── pay/                # Credits, billing, orders, and callbacks
│       ├── user/               # Authentication and profiles
│       └── vocab/              # Libraries, dictionaries, and caching
├── frontend/                   # Vue frontend
│   ├── src/components/
│   ├── src/lib/
│   ├── src/stores/
│   └── src/views/
├── LICENSE
├── README.md
└── README.zh-CN.md
```

Runtime configuration and build artifacts are excluded from Git by default.

## Requirements

- JDK 21
- Maven 3.9+
- Node.js 20+
- pnpm 9+
- MySQL 8+
- Redis 6+
- S3-compatible object storage
- Optional: an OpenAI-compatible model endpoint for translation and PDF OCR
- Optional: an Epay merchant account for Credits recharge

Verify the environment:

```bash
java -version
mvn -version
node -v
pnpm -v
mysql --version
redis-cli --version
```

## Local Development

Prepare the backend runtime configuration under `backend/src/main/resources/` before starting the application. These files are environment-specific and are not committed to the repository.

### Backend

```bash
cd backend
mvn spring-boot:run
```

The backend listens on:

```text
http://localhost:8888
```

### Frontend

```bash
cd frontend
pnpm install
pnpm dev
```

The frontend listens on:

```text
http://localhost:5173
```

The Vite development server proxies `/api` to `http://localhost:8888`. The default frontend API base path is `/api/v1`; override it in `frontend/.env` when necessary:

```dotenv
VITE_API_BASE_URL=/api/v1
```

## Build

### Backend JAR

Standard build:

```bash
cd backend
mvn clean package
```

Build without compiling or running test sources:

```bash
mvn clean package -Dmaven.test.skip=true
```

Artifact:

```text
backend/target/lexiflow-backend-1.0.0-SNAPSHOT.jar
```

`-DskipTests` skips test execution but still compiles test sources. `-Dmaven.test.skip=true` skips both test compilation and execution.

### Frontend dist

```bash
cd frontend
pnpm install --frozen-lockfile
pnpm build
```

Artifact:

```text
frontend/dist/
```

The build first performs Vue TypeScript type checking and then generates production assets.

## License

This project is licensed under the [GNU Affero General Public License v3.0](LICENSE).
