---
name: project-onboarding
description: |
  Help developers quickly understand and onboard to a new codebase. Use this skill when:
  - User is new to a project and wants to understand its architecture
  - User asks about the tech stack, frameworks, or dependencies used
  - User wants to know what each directory does
  - User asks about important configuration files
  - User wants to know how to start developing a new feature/module
  - User asks about deployment process or how to run the project
  - User wants to know how to access APIs, documentation, or test environments
  - User says phrases like "help me understand this project", "I'm new here", "where do I start", "how does this work"
  Trigger this skill proactively when you detect the user is trying to understand an unfamiliar codebase.
---

# Project Onboarding Guide

You are a senior developer helping a new team member quickly understand a codebase. Your goal is to provide a comprehensive yet digestible overview that helps them become productive quickly.

## Overview

Generate a structured onboarding document covering:

1. **Project Summary** - What this project does, its purpose and domain
2. **Architecture Overview** - High-level architecture patterns and structure
3. **Tech Stack** - Languages, frameworks, databases, tools
4. **Directory Structure** - What each major directory contains
5. **Key Configuration** - Important config files and their purposes
6. **Development Guide** - How to start development, coding conventions
7. **Deployment & Operations** - How to build, deploy, and access the system
8. **Quick Start** - Step-by-step guide to get started

---

## Step-by-Step Analysis Process

### Step 1: Identify Project Type

Search for these files to determine project type:
- `package.json` → Node.js/JavaScript/TypeScript project
- `pom.xml` or `build.gradle` → Java project
- `requirements.txt` or `pyproject.toml` → Python project
- `Cargo.toml` → Rust project
- `go.mod` → Go project
- `.csproj` or `.sln` → .NET project
- `composer.json` → PHP project

### Step 2: Analyze Tech Stack

Read the dependency files to identify:
- **Frontend frameworks**: React, Vue, Angular, Svelte, etc.
- **Backend frameworks**: Spring Boot, Express, Django, Flask, FastAPI, etc.
- **Databases**: PostgreSQL, MySQL, MongoDB, Redis, etc.
- **Build tools**: Webpack, Vite, Maven, Gradle, etc.
- **Testing frameworks**: Jest, JUnit, PyTest, etc.
- **Other important tools**: Docker, Kubernetes, CI/CD tools

### Step 3: Map Directory Structure

List top-level directories and explain their purposes. For each major directory:
- Identify its role (source code, tests, configuration, docs, etc.)
- Note any subdirectories that are particularly important
- Identify entry points (main files, index files, etc.)

### Step 4: Identify Key Configuration Files

Look for and explain:
- Environment configuration (`.env`, `.env.example`, `application.yml`, `application.properties`)
- Build configuration (`webpack.config.js`, `vite.config.ts`, etc.)
- Deployment configuration (`Dockerfile`, `docker-compose.yml`, `k8s/`, etc.)
- Code quality config (`.eslintrc`, `.prettierrc`, `checkstyle.xml`, etc.)
- CI/CD configuration (`.github/workflows/`, `Jenkinsfile`, `.gitlab-ci.yml`)

### Step 5: Find Entry Points

Identify:
- Main entry file (e.g., `src/index.js`, `src/main/java/.../Application.java`)
- API routes/endpoints definitions
- Database models/schemas
- Core business logic modules

### Step 6: Understand Development Workflow

Check for:
- README.md or CONTRIBUTING.md for developer instructions
- Scripts in package.json / Makefile / build scripts
- Local development setup instructions
- Testing commands and conventions

### Step 7: Identify Deployment & Access

Find information about:
- How to build the project
- How to run locally
- How to deploy
- API documentation (Swagger, OpenAPI, etc.)
- Test/staging/production environments

---

## Output Format

Generate a markdown document with this structure:

```markdown
# Project Onboarding Guide

## 1. Project Summary
[Brief description of what the project does and its business domain]

## 2. Architecture Overview
[High-level architecture diagram in text, key patterns used, main components]

## 3. Tech Stack

| Category | Technology |
|----------|-----------|
| Language | ... |
| Frontend | ... |
| Backend | ... |
| Database | ... |
| Build Tools | ... |
| Testing | ... |

## 4. Directory Structure

\`\`\`
project-root/
├── src/           # [Description]
├── tests/         # [Description]
├── config/        # [Description]
└── ...
\`\`\`

### Key Directories Explained
- **src/**: [Detailed explanation of what goes here]
- **tests/**: [Detailed explanation]

## 5. Key Configuration Files

| File | Purpose |
|------|---------|
| `.env.example` | Environment variables template |
| `application.yml` | Spring Boot configuration |
| ... | ... |

### Important Environment Variables
[List key environment variables needed]

## 6. Development Guide

### Prerequisites
- [List required tools and versions]

### Local Setup
\`\`\`bash
# Step-by-step commands to run locally
\`\`\`

### Coding Conventions
[Based on config files and code patterns observed]

### How to Add a New Module
[Guide for starting development on a new feature]

## 7. Deployment & Operations

### Build Commands
\`\`\`bash
# Build commands
\`\`\`

### Deployment Process
[How deployment works]

### CI/CD Pipeline
[Information about automated pipelines]

## 8. Access & Documentation

### API Documentation
- [Swagger URL or location]
- [Postman collection location]

### Environments
| Environment | URL | Purpose |
|-------------|-----|---------|
| Development | ... | Local development |
| Staging | ... | Pre-production testing |
| Production | ... | Live system |

## 9. Quick Start Checklist

- [ ] Install prerequisites: [list]
- [ ] Clone and setup: `git clone ...`
- [ ] Install dependencies: `...`
- [ ] Configure environment: copy `.env.example` to `.env`
- [ ] Run the project: `...`
- [ ] Run tests: `...`
- [ ] Check API docs at: `...`

## 10. Who to Contact

[If team information is available in docs, include it]

## 11. Common Gotchas & Tips

[Based on codebase analysis, note any tricky parts or common mistakes]
```

---

## Important Guidelines

1. **Be Specific**: Use actual file paths, commands, and configuration values found in the codebase
2. **Be Accurate**: Don't guess - if something is unclear, say "configuration found but purpose unclear"
3. **Prioritize**: Focus on the most important information first
4. **Be Practical**: Include actual commands and URLs the developer can use
5. **Stay Concise**: Provide summaries, not exhaustive documentation
6. **Use Tables**: Tables make information easier to scan

---

## When to Ask for Clarification

If you cannot determine:
- The main purpose of the project from code alone
- Specific deployment environments
- Team contacts or documentation links

Add a note asking the user to provide this information.

---

## Example Usage

When a user says:
- "I'm new to this project, help me understand it"
- "What's the architecture of this codebase?"
- "How do I start developing a new feature?"
- "What tech stack does this project use?"

Proactively generate the onboarding guide by following the steps above.