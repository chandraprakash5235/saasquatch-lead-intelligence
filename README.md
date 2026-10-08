# SaaSquatch Lead Intelligence

> **Caprae Capital Full Stack Developer AI-Readiness Challenge — Final Release**

---

## 🎯 Product Overview

**SaaSquatch Lead Intelligence** is an enterprise sales enablement web application designed to optimize lead-generation workflows by helping B2B sales teams instantly identify, evaluate, prioritize, and act on high-potential opportunities.

The application answers two fundamental questions for sales representatives:
1. **Prioritization**: *"Which lead should I contact first, and why?"* (Powered by an explainable, deterministic scoring engine)
2. **Actionability**: *"What should I say when reaching out?"* (Powered by a grounded AI outreach brief generator)

---

## 💡 Business Problem

Modern lead generation workflows produce high volumes of raw prospect data daily. However, not every lead has equal business value. Sales teams face:
- **Prioritization Fatigue**: Spending hours manually scanning spreadsheets to determine which leads to call first.
- **Black-Box AI Skepticism**: Unexplainable AI scores that sales teams don't trust or understand.
- **Context Switching**: Researching company background and decision maker details from scratch for every outreach message.
- **AI Hallucinations**: Generic AI outreach assistants that invent fake press releases, funding rounds, or titles.

---

## 🧪 Product Hypothesis

By decoupling **Lead Prioritization** (100% deterministic, explainable rule engine) from **Outreach Generation** (grounded LLM assistant), sales teams gain full transparency and trust in lead ranking while still benefiting from AI-assisted outreach preparation.

---

## 🚀 Key Features

- **CSV Ingestion & Deduplication**: Batch upload lead CSVs with automatic deduplication by company name and validation error reporting.
- **Deterministic Lead Scoring Engine (0–100)**: Evaluates ICP fit, revenue tier, employee size, growth signals, decision-maker quality, and metadata completeness.
- **Priority Tier Classification**: Categorizes leads into 🔴 **HIGH** (≥ 80), 🟡 **MEDIUM** (60–79), and ⚪ **LOW** (< 60).
- **Explainable Score Breakdown UI**: "Why this lead?" drawer displaying itemized point breakdowns and human-readable score justifications.
- **Grounded AI Outreach Brief**: Generates personalized executive summaries, key talking points, personalization hooks, email openings, and data gap warnings using strictly recorded lead facts/data.
- **Safety & Anti-Hallucination Audit**: Strict rules preventing the AI from inventing executive titles, funding, products, or unverified claims.
- **One-Click Clipboard Copying**: Easily copy suggested email openings to clipboard.
- **Graceful Error Handling**: Clear handling for Ollama connectivity issues, request timeouts, and deterministic fallback when the local model cannot provide a grounded response.

---

## 🏗️ Architecture

### System Flow
```text
┌────────────────────────────────────────────────────────┐
│                      React + TypeScript                 │
│                     Ant Design UI                      │
└───────────────────────────┬────────────────────────────┘
                            │ REST (JSON)
                            ↓
┌────────────────────────────────────────────────────────┐
│                    Spring Boot API                     │
├────────────────────────────────────────────────────────┤
│  • LeadService (CSV Ingestion & Entity Management)     │
│  • LeadScoringService (Stateless Scoring Engine)       │
│  • AIOutreachService (Grounded LLM Integrator)         │
└───────────────┬────────────────────────┬───────────────┘
                │                        │
                ↓                        ↓
┌────────────────────────┐   ┌──────────────────────────┐
│ PostgreSQL             │   │ Ollama qwen2.5:3b        │
└────────────────────────┘   └─────────────┬────────────┘
                                             ↓
                                 ┌──────────────────────┐
                                 │ Grounding Validator  │
                                 └──────────┬───────────┘
                                            ↓
                                 ┌──────────────────────┐
                                 │ Deterministic        │
                                 │ Fallback             │
                                 └──────────────────────┘
```

---

## 🛠️ Technology Stack

### Backend
- **Java 21**
- **Spring Boot 3.2.4** (Spring Web, Spring Data JPA, Bean Validation, Actuator)
- **PostgreSQL 16** (Production-ready persistence via Docker Compose)
- **H2 Database** (Zero-config fallback for automated testing)
- **Apache Commons CSV** (Robust CSV ingestion)
- **Java 21 HttpClient & Jackson** (Native REST client for Ollama API)

### Frontend
- **React 18 + TypeScript 5 + Vite 5**
- **Ant Design (antd v5)** UI library
- **Axios** for HTTP API client

---

## 🧠 Lead Scoring Methodology

Lead scoring is **deterministic and transparent** so sales reps understand exactly why a lead was ranked high or low.

| Component | Max Points | Criteria & Rules |
| :--- | :---: | :--- |
| **ICP Fit** | 30 | Preferred industry (SaaS, Tech, Software, Mfg, Healthcare, Logistics, FinServ) = 30; Recognized = 15; Missing = 0 |
| **Revenue Fit** | 20 | `≥ $10M → 20` \| `≥ $5M → 15` \| `≥ $2M → 10` \| `≥ $500K → 5` \| `< $500K → 0` |
| **Employee Fit** | 15 | `≥ 100 → 15` \| `≥ 50 → 12` \| `≥ 20 → 8` \| `≥ 10 → 4` \| `< 10 → 0` |
| **Growth Signal** | 15 | Hiring/Expansion/New Location = 15; Funding = 12; Partnership = 8; None = 0 |
| **Decision Maker** | 10 | Name + Title + LinkedIn = 10; Name + Title = 8; Name only = 5; None = 0 |
| **Completeness** | 10 | `90-100% → 10` \| `75-89% → 8` \| `50-74% → 5` \| `25-49% → 2` \| `< 25% → 0` |
| **Total** | **100** | **Deterministic Total** |

---

## 🤖 AI Outreach Assistant & Grounding

The **AI Outreach Brief Generator** uses local **Ollama** (`qwen2.5:3b`) running in Docker for grounded outreach generation.

### Local Ollama Setup (Docker)

Start the Ollama container:
```bash
docker run -d \
  --name caprae-ollama \
  -p 11434:11434 \
  -v caprae-ollama:/root/.ollama \
  ollama/ollama
```

Pull the required model:
```bash
docker exec -it caprae-ollama ollama pull qwen2.5:3b
```

### Grounding & Safety Audit Rules
1. **Score Integrity**: AI does **never** alter or calculate lead scores or priorities.
2. **Strict Attribute Reliances**: Prompt builder supplies only recorded facts from the DB record.
3. **No Fact Fabrication**: Prohibits inventing executive titles, LinkedIn profiles, funding amounts, client lists, or product claims.
4. **Data Gap Handling**: If decision maker title or LinkedIn is missing, AI notes the gap rather than guessing.
5. **Channel Safety**: Recommends `LinkedIn InMail` only when a verified LinkedIn URL exists; otherwise defaults to `Email` or `Unknown (Requires Verification)`.

### Connectivity & Deterministic Fallback
- **LLM Connectivity**: The application checks local Ollama availability before generating the outreach brief and surfaces timeout/connectivity issues clearly.
- **Request Timeout Handling**: If Ollama is slow or unreachable, the API returns a controlled failure state instead of silently fabricating output.
- **Deterministic Fallback**: When the model cannot respond, the system falls back to a deterministic, rule-based outreach structure using recorded lead attributes and safety-checked phrasing.

---

## 🗄️ Database Schema

### `leads` Table
```sql
CREATE TABLE leads (
    id                      BIGSERIAL PRIMARY KEY,
    company_name            VARCHAR(255) NOT NULL UNIQUE,
    website                 VARCHAR(255),
    industry                VARCHAR(100),
    revenue                 NUMERIC(15, 2),
    employee_count          INTEGER,
    location                VARCHAR(255),
    growth_signal           VARCHAR(100) DEFAULT 'None',
    decision_maker_name     VARCHAR(255),
    decision_maker_title    VARCHAR(255),
    decision_maker_linkedin VARCHAR(255),
    data_completeness       INTEGER NOT NULL DEFAULT 0,
    score                   INTEGER DEFAULT 0,
    priority                VARCHAR(20) DEFAULT 'LOW',
    created_at              TIMESTAMP NOT NULL,
    updated_at              TIMESTAMP NOT NULL
);
```

---

## 🔌 API Documentation

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/api/health` | `GET` | Health check endpoint |
| `/api/leads` | `GET` | Paginated lead listing with filters (`industry`, `priority`, `growthSignal`) & sorting |
| `/api/leads/{id}` | `GET` | Single lead detail |
| `/api/leads/{id}/score` | `GET` | Explainable lead score breakdown & rationale |
| `/api/leads/{id}/outreach` | `POST` | Generate AI Outreach Brief |
| `/api/leads/import` | `POST` | Import leads from CSV file |
| `/api/leads/recalculate-scores` | `POST` | Recalculate scores for all leads |

---

## ⚡ Local Setup

### 1. Prerequisites
- Java 21+
- Maven 3.9+
- Node.js 18+
- Docker & Docker Compose

### 2. Start PostgreSQL & Ollama
```bash
docker-compose up -d
docker run -d --name caprae-ollama -p 11434:11434 -v caprae-ollama:/root/.ollama ollama/ollama
docker exec -it caprae-ollama ollama pull qwen2.5:3b
```

### 3. Run Backend
```bash
cd backend
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

### 4. Run Frontend
```bash
cd frontend
npm install
npm run build
npm run dev
```
Open `http://localhost:5173`

---

## 🔐 Environment Variables

| Variable | Default | Purpose |
| :--- | :--- | :--- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/caprae_leads` | Database JDBC URL |
| `DB_USERNAME` | `postgres` | Database Username |
| `DB_PASSWORD` | `postgres` | Database Password |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama HTTP endpoint |
| `OLLAMA_MODEL` | `qwen2.5:3b` | Ollama LLM model |
| `OLLAMA_TIMEOUT_SECONDS` | `120` | Ollama request timeout in seconds |

---

## 🧪 Testing

Run all backend unit & integration tests:
```bash
cd backend
mvn clean test
```

Test coverage includes:
- `LeadScoringServiceTest`: Deterministic scoring rules, priority bounds, growth signals, and data completeness.
- `AIOutreachPromptBuilderTest`: Prompt grounding rules, prohibited hallucination checks, and null field safety.
- `AIOutreachServiceTest`: Ollama connectivity and timeout/fallback handling.
- `LeadControllerTest`: REST API contracts.

---

## 🎬 Demo Workflow (2-Minute Walkthrough)

1. **Launch App**: Open `http://localhost:5173`. Notice KPI cards automatically update from backend data.
2. **Import Leads**: Click **Import Leads** -> select `sample-data/leads.csv` -> click **Import File**.
3. **Prioritize**: Notice table automatically sorts by `Lead Score DESC` with 🔴 **HIGH** priority leads highlighted at the top.
4. **Filter**: Select Priority = `HIGH` to filter top-tier prospects.
5. **Inspect**: Click **"Why this lead?"** on Nova Technology to review the itemized score breakdown (97/100).
6. **Act**: Click **"Generate AI Outreach Brief"** to receive grounded talking points and copy the opening email line.

---

## ⏱️ 5-Hour Engineering Tradeoffs

Given the 5-hour project constraint, engineering decisions prioritized **decision intelligence, scoring explainability, and AI safety** over generic infrastructure:

1. **CSV Ingestion vs Live Web Scraping**: Ingestion was built around structured CSV import to focus development on lead prioritization and AI outreach rather than anti-scraping maintenance.
2. **Synchronous Ingestion vs Distributed Queues**: Ingestion processes synchronously in transaction blocks, keeping deployment simple without Kafka/RabbitMQ overhead.
3. **Stateless Scoring Engine**: `LeadScoringService` was built as a pure, stateless Java engine, ensuring 100% testability without database locks.

---

## 🔮 Future Architecture & Roadmap

```text
SaaSquatch / Web Scraper
           ↓
Ingestion Microservice (Kafka Event Stream)
           ↓
Lead Processing & Enrichment Pipeline
           ↓
PostgreSQL Cluster + Redis Cache
           ↓
Deterministic Lead Scoring Engine
           ↓
AI Outreach Brief Generator (LLM Router)
           ↓
Salesforce / HubSpot CRM Integration
```

### Planned Improvements
- **CRM Integrations**: Direct sync with Salesforce & HubSpot.
- **Domain Identity Deduplication**: Deduplicating leads by domain identity and tax ID.
- **Custom Prompt Builder**: Allowing sales ops to customize outreach templates per vertical.
