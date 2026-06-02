i went create databse for this pos managment like this MRC TRADING/ REGISTERED NAME Address Branch POS Terminal ID PHONE POS SERIAL NUMBER BANK NAME MRC ACCOUNT SiMCard Number SiM Type Production Date Status QR Remarks Combined KCV location location comment and for Daily POS Deployment to w Email address Fill Date Merchant License Name Merchant Address Merchant Phone number Received By (Person at Marchant) Device Serial Number How many Branch have this Merchant IF more than 20 Branch list here SantimPay Employee Full Name Write the Detail Conversation Deployed POS Photo Upload Signature Select Branch Name Merchant Settlemet Bank Name Merchant Settlemet Bank Account Number Trade Name Terminal ID Upload a clear photo of the merchant’s business/shop Column 12 Trello Card ID and for all POS Transaction Month is TerminalId TerminalName MerchantId TotalPurchaseCount TotalPurchaseAmount GatewayTransactionCount GatewayTransactionAmount TotalTransactionCount TotalTransactionAmount SantimpayCommission TotalCommissionBr TotalCommissionCut and for 2026 POS Phone Call FollowUp Name Merchant License Name Merchant Phone number Device Serial Number Call Center Follow first follow up Comment Contacted Person: Full Name New Contacted Person: phone number and for Merchant's KYC Change Request (Responses) Timestamp Email address Date Current Merchant Trade Name Merchant ID Terminal ID Merchant Owner Person Full Name Phone Number Owner What Type Change Requested Settlement Bank Account ( If not account change say No) Trade Name ( If not Trade Name say No) Both Change ( If not change BOTH say No) Reason for Change Declaration Checkbox SantimPay Employee Full Name Merchant City Data Encoder Conformationgive me enterprise-grade POS Management Platform like those used by fintech companies, use this stronger prompt. very extream and task claude giv me prompt i went create databse for this pos managment like this MRC TRADING/ REGISTERED NAME Address Branch POS Terminal ID PHONE POS SERIAL NUMBER BANK NAME MRC ACCOUNT SiMCard Number SiM Type Production Date Status QR Remarks Combined KCV location location comment and for Daily POS Deployment to w Email address Fill Date Merchant License Name Merchant Address Merchant Phone number Received By (Person at Marchant) Device Serial Number How many Branch have this Merchant IF more than 20 Branch list here SantimPay Employee Full Name Write the Detail Conversation Deployed POS Photo Upload Signature Select Branch Name Merchant Settlemet Bank Name Merchant Settlemet Bank Account Number Trade Name Terminal ID Upload a clear photo of the merchant’s business/shop Column 12 Trello Card ID and for all POS Transaction Month is TerminalId TerminalName MerchantId TotalPurchaseCount TotalPurchaseAmount GatewayTransactionCount GatewayTransactionAmount TotalTransactionCount TotalTransactionAmount SantimpayCommission TotalCommissionBr TotalCommissionCut and for 2026 POS Phone Call FollowUp Name Merchant License Name Merchant Phone number Device Serial Number Call Center Follow first follow up Comment Contacted Person: Full Name New Contacted Person: phone number and for Merchant's KYC Change Request (Responses) Timestamp Email address Date Current Merchant Trade Name Merchant ID Terminal ID Merchant Owner Person Full Name Phone Number Owner What Type Change Requested Settlement Bank Account ( If not account change say No) Trade Name ( If not Trade Name say No) Both Change ( If not change BOTH say No) Reason for Change Declaration Checkbox SantimPay Employee Full Name Merchant City Data Encoder Conformation i went develop kubernats basd on teraform web application using react basd on docker and database post gress and claudflare dns tunul and host in ubuntu ltc 24 and it is perpose for store stacture data store in and here use for employes torecord that data nd for the future apply ai and automation here is the sales to recrate using android app flater and web in ofice incoder to sto maerchant information merchat information look like this for diferent roles and register role for employe and asighn task sales call senter and diverant type of employe and amazing and advanced for future ai tu use this and automation  Act as a senior enterprise fintech solutions architect, database designer, backend engineer, and product strategist.
# MANDATORY TECHNOLOGY DECISIONS (NON-NEGOTIABLE)

You must NOT change the technology stack.

Do NOT recommend alternatives unless there is a critical technical reason.

The following technologies are mandatory and must be used throughout the entire architecture, implementation plan, folder structure, database design, CI/CD design, Kubernetes design, and deployment strategy.

## Backend (MANDATORY)

Use:

* Java 21 LTS
* Spring Boot 3.x
* Spring Security
* Spring Data JPA
* Hibernate
* Flyway
* Maven
* JWT Authentication
* OpenAPI / Swagger
* MapStruct
* Lombok
* Bean Validation

Architecture Style:

* Clean Architecture
* Domain Driven Design (DDD)
* Modular Monolith first
* Microservice-ready architecture
* SOLID Principles
* Repository Pattern
* Service Layer Pattern
* Event-Driven Design where appropriate

Do NOT use:

* Node.js
* NestJS
* Express
* Django
* Laravel
* ASP.NET

Spring Boot is the mandatory backend framework.

---

## Frontend (MANDATORY)

Use:

* React 19+
* TypeScript
* Vite
* Material UI (MUI)
* React Query (TanStack Query)
* React Router
* Axios
* React Hook Form
* Zod Validation

Architecture:

* Feature-Based Structure
* Modular Components
* Enterprise Dashboard Design
* Reusable UI Components

Do NOT use:

* Angular
* Vue
* jQuery

React is mandatory.

---

## Mobile (MANDATORY)

Use:

* Flutter
* Dart

Architecture:

* Clean Architecture
* Riverpod
* Dio
* Go Router

Purpose:

* Sales Team
* Field Agents
* Merchant Registration
* POS Deployment
* KYC Collection
* Offline Data Capture
* Photo Upload
* Signature Capture

---

## Database (MANDATORY)

Use:

* PostgreSQL

Design for:

* High Performance
* Scalability
* Reporting
* Auditability
* AI Readiness

---

## Infrastructure (MANDATORY)

Use:

* Proxmox VE
* Ubuntu Server 24.04 LTS
* Docker
* Kubernetes
* Terraform
* Ansible
* GitHub
* GitHub Actions
* ArgoCD

All services must run on self-hosted infrastructure.

No AWS.
No Azure.
No GCP.

---

## Security (MANDATORY)

Use:

* Keycloak
* OAuth2
* OpenID Connect (OIDC)
* RBAC
* MFA

---

## Monitoring (MANDATORY)

Use:

* Prometheus
* Grafana
* Loki
* Tempo

---

## Storage (MANDATORY)

Use:

* MinIO

Store:

* Merchant Photos
* Signatures
* Deployment Images
* Documents
* KYC Files

Do NOT store large files directly in PostgreSQL.

---

## Networking (MANDATORY)

Use:

* Cloudflare DNS
* Cloudflare Tunnel
* Cloudflare WAF
* Cloudflare SSL
* Cloudflare Zero Trust

Services must be published securely through Cloudflare Tunnel.

Avoid exposing public ports directly whenever possible.

---

## Development Standards

Assume this project will be maintained for at least 5 years.

Assume:

* Multiple developers
* Multiple environments
* CI/CD pipelines
* Thousands of merchants
* Thousands of POS devices
* Millions of transactions

Every recommendation must be enterprise-grade, scalable, maintainable, secure, and production-ready.

Do not simplify the architecture.

Design as if this system is being reviewed by a fintech CTO before production approval.

You are a Principal DevOps Architect, Principal Platform Engineer, Kubernetes Architect, Cloud Infrastructure Architect, Security Architect, and Fintech Infrastructure Consultant.

I am building an enterprise-grade POS Management Platform for a fintech company.

I do NOT want a simple application design.

I want a complete enterprise DevOps architecture and deployment strategy similar to what large fintech companies use.

====================================================
DEPLOYMENT ENVIRONMENT
====================================================

Everything must be self-hosted.

Infrastructure:

- Proxmox VE Cluster
- Ubuntu Server 24.04 LTS VMs
- Local Datacenter Deployment
- Cloudflare Tunnel for public access
- Cloudflare DNS
- Cloudflare WAF
- Cloudflare SSL
- Cloudflare Zero Trust

No AWS.

No Azure.

No Google Cloud.

Design everything for on-premise deployment.

====================================================
APPLICATION STACK
====================================================

Frontend:
- React
- TypeScript

Mobile:
- Flutter Android

Backend:
- Recommend best option
- Golang or NestJS

Database:
- PostgreSQL

Cache:
- Redis

Object Storage:
- MinIO

Search:
- OpenSearch or Elasticsearch

Message Queue:
- RabbitMQ or Kafka

Authentication:
- Keycloak

====================================================
DEVOPS REQUIREMENTS
====================================================

Design complete DevOps architecture including:

Source Control:

- GitHub
- GitHub Organization Structure
- Repository Strategy
- Branching Strategy
- Trunk-Based Development
- Git Flow comparison

CI/CD:

- GitHub Actions
- Self-hosted GitHub Runners
- Build Pipeline
- Test Pipeline
- Security Pipeline
- Deployment Pipeline

Containerization:

- Docker
- Docker Compose
- Multi-stage Dockerfiles
- Container Security

Container Registry:

- Harbor
- GitHub Container Registry

Infrastructure as Code:

- Terraform
- Terraform Modules
- Terraform State Management
- Environment Separation

Configuration Management:

- Ansible
- Secrets Management

====================================================
KUBERNETES REQUIREMENTS
====================================================

Design complete Kubernetes architecture.

Include:

- Control Plane
- Worker Nodes
- Namespace Strategy
- Resource Quotas
- Limit Ranges
- RBAC
- Network Policies
- Pod Security Standards

Recommend:

- K3s
- RKE2
- Kubernetes
- MicroK8s

Choose the best solution and explain why.

Design:

- Development Environment
- Staging Environment
- Production Environment

====================================================
GITOPS
====================================================

Design GitOps architecture using:

- ArgoCD

or

- FluxCD

Recommend best choice.

Include:

- Repository Structure
- Application Sync
- Rollback Strategy
- Promotion Strategy

====================================================
OBSERVABILITY
====================================================

Design enterprise monitoring stack.

Include:

Metrics:

- Prometheus

Visualization:

- Grafana

Logs:

- Loki

Tracing:

- Tempo
- Jaeger

Alerting:

- AlertManager

Dashboards:

- Infrastructure
- Kubernetes
- PostgreSQL
- Application
- Business KPIs

====================================================
DATABASE
====================================================

Design PostgreSQL architecture.

Include:

- HA PostgreSQL
- Replication
- Failover
- Backups
- PITR
- WAL Archiving
- Connection Pooling

Recommend:

- Patroni
- PgBouncer

====================================================
STORAGE
====================================================

Design storage architecture.

Include:

- MinIO
- PostgreSQL Storage
- Backup Storage

Recommend:

- Longhorn
- Ceph
- NFS

Choose the best solution for Proxmox.

====================================================
SECURITY
====================================================

Design complete security architecture.

Include:

Identity:

- Keycloak

Authentication:

- OIDC
- OAuth2

Secrets:

- Vault
- Sealed Secrets

Security Scanning:

- Trivy
- Dependabot

Container Security:

- Image Scanning

Network Security:

- Network Policies

Audit Logging:

- Complete Audit Trail

====================================================
CLOUDFLARE
====================================================

Design Cloudflare architecture.

Include:

- Cloudflare Tunnel
- DNS
- SSL
- WAF
- Rate Limiting
- Zero Trust Access

Explain how services become publicly accessible without opening inbound firewall ports.

====================================================
BACKUP STRATEGY
====================================================

Design:

- Daily Backup
- Weekly Backup
- Monthly Backup

Include:

- PostgreSQL
- Kubernetes
- MinIO
- GitHub Repositories
- Terraform State

====================================================
DISASTER RECOVERY
====================================================

Design:

- RPO
- RTO

Include:

- Database Recovery
- Kubernetes Recovery
- Full Datacenter Failure Recovery

====================================================
AI & AUTOMATION READINESS
====================================================

Design infrastructure ready for:

- AI Analytics
- Merchant Scoring
- Predictive Reporting
- Workflow Automation
- LLM Integration
- Future AI Agents

====================================================
OUTPUT FORMAT
====================================================

Return:

1. Complete Infrastructure Diagram
2. Proxmox Architecture
3. VM Layout
4. Kubernetes Cluster Design
5. Network Design
6. GitHub Organization Structure
7. Repository Structure
8. GitHub Actions Pipelines
9. Docker Standards
10. Terraform Structure
11. Ansible Structure
12. ArgoCD GitOps Design
13. PostgreSQL HA Design
14. MinIO Design
15. Monitoring Stack
16. Security Stack
17. Cloudflare Design
18. Backup Design
19. Disaster Recovery Design
20. Step-by-Step Production Deployment Plan
21. Enterprise Best Practices
22. Common Mistakes To Avoid

Act like a fintech platform architect reviewing a production deployment before go-live.

Challenge bad decisions.

Recommend enterprise-grade alternatives.

Do not simplify anything.

Provide production-ready architecture only.I am building an enterprise-grade POS Management Platform for a fintech company. This system must be designed like the internal platform used by a serious payment company, with strong data structure, scalability, auditability, role-based access, and future AI/automation readiness.

You are a senior enterprise software architect, database designer, UI/UX strategist, and product engineer. 
Design a complete, modern, ultra-professional POS Management System for a fintech company, built at the level of a big enterprise company.

I need an expert-level solution for database design, system architecture, user roles, workflows, dashboards, reporting, and a clean advanced UI/UX.

Important goals:
- Make it look and work like a top company system
- Be highly organized, scalable, secure, and easy to maintain
- Support different merchants, different banks, different cities, regions, and locations
- Support multiple POS devices, merchants, branches, transactions, call follow-up, and device health monitoring
- Include strong access control with 4 roles
- Make the database normalized and professional
- Include audit logs, timestamps, status tracking, and reporting fields
- Design for web app / admin dashboard use

I need you to design these modules:

1) POS Inventory / Registration
Fields:
- MRC TRADING / REGISTERED NAME
- Address
- Branch
- POS Terminal ID
- PHONE
- POS SERIAL NUMBER
- BANK NAME
- MRC ACCOUNT
- SIM Card Number
- SIM Type
- Production Date
- Status
- QR
- Remarks
- Combined KCV
- location
- location comment

2) Daily POS Deployment
Fields:
- Email address
- Fill Date
- Merchant License Name
- Merchant Address
- Merchant Phone number
- Received By (Person at Merchant)
- Device Serial Number
- How many Branch have this Merchant
- If more than 20 Branch list here
- SantimPay Employee Full Name
- Write the Detailed Conversation
- Deployed POS Photo
- Upload Signature
- Select Branch Name
- Merchant Settlement Bank Name
- Merchant Settlement Bank Account Number
- Trade Name
- Terminal ID
- Upload a clear photo of the merchant’s business/shop
- Column 12
- Trello Card ID

3) Monthly POS Transaction Report
Fields:
- TerminalId
- TerminalName
- MerchantId
- TotalPurchaseCount
- TotalPurchaseAmount
- GatewayTransactionCount
- GatewayTransactionAmount
- TotalTransactionCount
- TotalTransactionAmount
- SantimPayCommission
- TotalCommissionBr
- TotalCommissionCut

4) 2026 POS Phone Call Follow-Up
Fields:
- Name
- Merchant License Name
- Merchant Phone number
- Device Serial Number
- Call Center First Follow Up
- Comment
- Contacted Person: Full Name
- New Contacted Person: phone number

5) POS Health / Device Information First Report
Fields:
- Latest Date
- POS SERIAL NUMBER
- Device Status
- Battery Level
- Mobile Data
- IP
- latitude
- longitude
- Cpu Usage
- Available memory
- Available Storage

Business rules:
- One merchant can have multiple branches
- One merchant can have multiple POS devices
- Merchant can use different bank accounts
- Merchant can be in different city, region, and location
- Keep full history of changes
- Track which employee did each action
- Track deployment, follow-up, health status, and transaction history

Access control:
Create exactly 4 roles with different permissions, such as:
- Super Admin
- Operations Manager
- Call Center Agent
- Field/Deployment Staff

For each role, define:
- What they can view
- What they can create
- What they can edit
- What they can delete
- What they cannot access

What I want from you:
1. First, explain the best enterprise-level system design for this POS management platform.
2. Then design the full database schema.
3. Break it into tables with primary keys, foreign keys, relationships, and data types.
4. Normalize the database properly.
5. Suggest indexes for performance.
6. Suggest validations for each field.
7. Create a role/permission matrix.
8. Design the workflow from POS registration → deployment → transaction tracking → call follow-up → device health monitoring.
9. Suggest dashboard pages and reports.
10. Suggest the best UI/UX layout like a modern company system.
11. Give me API module names if building backend services.
12. If any fields should be split into separate tables, do that and explain why.
13. Identify missing fields that should be added for a real enterprise system.
14. Give me a final clean table structure with recommended columns.

Output format:
- Use clear headings
- Use tables where helpful
- Be very specific
- Be professional
- Think like an enterprise solution architect
- Do not give a beginner answer
- Do not give a shallow answer
- Give me the best possible design

If anything is ambiguous, make the smartest enterprise assumption and continue.
I can also turn this into a much stronger prompt for building the actual app/database in Flutter + backend + SQL.


I can also turn this into a much stronger prompt for building the actual app/database in Flutter + backend + SQL.

Yes. If your goal is to build a real enterprise-grade POS Management Platform like those used by fintech companies, use this stronger prompt.

Act as a Chief Solution Architect, Enterprise Database Architect, Senior Backend Engineer, Senior Flutter Engineer, DevOps Architect, and Fintech Product Designer.

Design and build a complete enterprise-grade POS Management Platform for a fintech company similar to large payment providers.

The system must be scalable to manage:

- 1,000,000+ merchants
- 5,000,000+ POS devices
- Millions of transactions
- Multiple regions and cities
- Multiple banks
- Multiple branches per merchant
- Multiple employees and departments
- Complete audit trails
- Enterprise security and compliance

====================================================
BUSINESS OVERVIEW
====================================================

The company deploys POS devices to merchants.

The system must manage:

1. Merchant Management
2. Branch Management
3. POS Device Inventory
4. POS Deployment Tracking
5. Transaction Analytics
6. Device Health Monitoring
7. Call Center Follow-up
8. Employee Management
9. Regional Management
10. Bank Management
11. SIM Management
12. QR Management
13. Reporting and BI
14. Workflow Automation
15. Audit Logging

====================================================
MODULE 1: MERCHANT MANAGEMENT
====================================================

Merchant Fields:

- Merchant ID
- Merchant License Name
- Trade Name
- Registered Name
- TIN Number
- License Number
- Merchant Category
- Business Type
- Phone
- Email
- Status
- Registration Date
- Created By
- Updated By

Rules:

- One merchant can have multiple branches.
- One merchant can have multiple bank accounts.
- One merchant can have multiple POS devices.

====================================================
MODULE 2: BRANCH MANAGEMENT
====================================================

Branch Fields:

- Branch ID
- Merchant ID
- Branch Name
- Branch Code
- Region
- City
- Sub City
- Woreda
- Latitude
- Longitude
- Full Address
- Contact Person
- Contact Phone
- Status

====================================================
MODULE 3: POS DEVICE INVENTORY
====================================================

Fields:

- POS ID
- Serial Number
- Terminal ID
- Device Model
- Device Manufacturer
- Production Date
- QR Code
- KCV
- Device Status
- Warehouse Status
- Deployment Status
- Assigned Merchant
- Assigned Branch
- Assigned Employee
- Last Activity Date

====================================================
MODULE 4: BANK MANAGEMENT
====================================================

Fields:

- Bank ID
- Bank Name
- Bank Code
- Bank Status

Merchant Bank Account Fields:

- Account ID
- Merchant ID
- Bank ID
- Account Number
- Account Holder Name
- Settlement Account
- Status

====================================================
MODULE 5: SIM MANAGEMENT
====================================================

Fields:

- SIM ID
- SIM Number
- SIM Type
- Telecom Provider
- Activation Date
- Expiration Date
- Status
- Assigned Device

====================================================
MODULE 6: DAILY POS DEPLOYMENT
====================================================

Fields:

- Deployment ID
- Deployment Date
- Merchant ID
- Branch ID
- POS ID
- Employee ID
- Received By
- Merchant Photo
- Signature Photo
- Shop Photo
- Deployment Notes
- Trello Card ID
- Deployment Status
- GPS Location

Store uploaded files separately using a document table.

====================================================
MODULE 7: POS TRANSACTION MANAGEMENT
====================================================

Transaction Fields:

- Transaction ID
- Terminal ID
- Merchant ID
- Branch ID
- Transaction Date
- Transaction Type
- Transaction Amount
- Commission Amount
- Gateway Amount
- Status
- Settlement Status

Create transaction summary tables:

Daily Summary
Weekly Summary
Monthly Summary
Yearly Summary

====================================================
MODULE 8: CALL CENTER FOLLOW-UP
====================================================

Fields:

- FollowUp ID
- Merchant ID
- POS ID
- Call Date
- Agent ID
- Contact Person
- Contact Number
- Call Outcome
- Merchant Satisfaction Score
- Issue Category
- Notes
- Next Follow-up Date

====================================================
MODULE 9: DEVICE HEALTH MONITORING
====================================================

Fields:

- Health Report ID
- POS ID
- Report Date
- Device Status
- Battery Level
- Mobile Data Status
- IP Address
- Latitude
- Longitude
- CPU Usage
- RAM Usage
- Storage Usage
- Signal Strength
- App Version
- Android Version
- Last Sync Time

Health reports should support millions of records.

Recommend partitioning strategy.

====================================================
MODULE 10: EMPLOYEE MANAGEMENT
====================================================

Fields:

- Employee ID
- Full Name
- Department
- Position
- Region
- Phone
- Email
- Status

====================================================
ROLE-BASED ACCESS CONTROL
====================================================

Design permissions for:

1. Super Admin
2. Operations Manager
3. Call Center Agent
4. Field Deployment Officer

Provide:

- CRUD permissions
- Module permissions
- Data visibility rules
- Regional restrictions

====================================================
AUDIT SYSTEM
====================================================

Track:

- Login history
- Data changes
- Status changes
- Device assignment changes
- Deployment updates
- User actions

Store:

- Who changed
- What changed
- Previous value
- New value
- Timestamp
- IP address

====================================================
DASHBOARDS
====================================================

Executive Dashboard

Show:

- Total Merchants
- Active Merchants
- Active POS
- Offline POS
- Today's Deployments
- Monthly Transactions
- Revenue
- Regional Performance

Operations Dashboard

Show:

- Device Inventory
- Pending Deployments
- Device Status
- Employee Performance

Call Center Dashboard

Show:

- Follow-ups Due
- Open Cases
- Merchant Satisfaction

====================================================
TECHNICAL ARCHITECTURE
====================================================

Recommend:

Frontend:
- Flutter Web
- Flutter Android

Backend:
- NestJS or Spring Boot

Database:
- PostgreSQL

Cache:
- Redis

Queue:
- RabbitMQ

Storage:
- S3 Compatible Storage

Authentication:
- JWT
- Refresh Tokens
- MFA

Monitoring:
- Prometheus
- Grafana

CI/CD:
- GitHub Actions

Containers:
- Docker
- Kubernetes

====================================================
DATABASE DESIGN
====================================================

Generate:

1. Complete ERD
2. All tables
3. Primary Keys
4. Foreign Keys
5. Indexes
6. Constraints
7. Normalization explanation
8. Data retention strategy
9. Backup strategy
10. High availability strategy

====================================================
API DESIGN
====================================================

Generate:

- REST API endpoints
- Request examples
- Response examples
- Validation rules
- Security requirements

====================================================
UI/UX DESIGN
====================================================

Design pages similar to enterprise fintech systems.

Include:

- Navigation structure
- Dashboard layouts
- Merchant pages
- Device pages
- Transaction pages
- Deployment pages
- Call center pages
- Reporting pages
- Admin pages

====================================================
DELIVERABLE
====================================================

Provide:

1. Enterprise Architecture Diagram
2. Complete Database Schema
3. ERD
4. Table Structures
5. API Architecture
6. Flutter Application Structure
7. Backend Structure
8. Security Architecture
9. Deployment Architecture
10. Future Scalability Plan

Think like a fintech architect building a system for millions of records and thousands of employees.

Technology stack I plan to use:

- Frontend: React web app

- Mobile app: Flutter Android app

- Backend: scalable API service

- Database: PostgreSQL

- Infrastructure: Docker, Kubernetes, Terraform, Ubuntu 24 LTS

- DNS and secure tunnel: Cloudflare

- Future expansion: AI, workflow automation, analytics, task assignment, and reporting



The platform must support multiple employee roles such as:

- Sales

- Call Center

- Data Encoder

- Field Officer

- Support Team

- Finance

- Supervisor

- Admin

- Manager

- IT/Technical Team



Core goal:

Create a normalized, enterprise-grade database and system architecture to store, track, and manage merchant, POS, transaction, deployment, KYC, follow-up, and operational data in a structured way. The system must support future growth, automation, dashboards, AI assistance, and reporting.



I want you to design the system around these business areas:



1. POS Inventory / Registration data

Fields include:

- MRC Trading / Registered Name

- Address

- Branch

- POS Terminal ID

- Phone

- POS Serial Number

- Bank Name

- MRC Account

- SIM Card Number

- SIM Type

- Production Date

- Status

- QR

- Remarks

- Combined KCV

- Location

- Location Comment



2. Daily POS Deployment records

Fields include:

- Email Address

- Fill Date

- Merchant License Name

- Merchant Address

- Merchant Phone Number

- Received By (person at merchant)

- Device Serial Number

- How many branches this merchant has

- If more than 20 branches, list them

- SantimPay Employee Full Name

- Detailed conversation notes

- Deployed POS photo

- Uploaded signature

- Select branch name

- Merchant settlement bank name

- Merchant settlement bank account number

- Trade name

- Terminal ID

- Clear photo of merchant’s business/shop

- Column 12

- Trello Card ID



3. Monthly POS Transaction summary

Fields include:

- Month

- Terminal ID

- Terminal Name

- Merchant ID

- Total Purchase Count

- Total Purchase Amount

- Gateway Transaction Count

- Gateway Transaction Amount

- Total Transaction Count

- Total Transaction Amount

- SantimPay Commission

- Total Commission BR

- Total Commission Cut



4. 2026 POS Phone Call Follow-Up

Fields include:

- Name

- Merchant License Name

- Merchant Phone Number

- Device Serial Number

- Call Center Follow First Follow-Up

- Comment

- Contacted Person Full Name

- New Contacted Person Phone Number



5. Merchant KYC Change Request

Fields include:

- Timestamp

- Email Address

- Date

- Current Merchant Trade Name

- Merchant ID

- Terminal ID

- Merchant Owner Full Name

- Owner Phone Number

- What Type Change Requested

- Settlement Bank Account

- Trade Name

- Both Change

- Reason for Change

- Declaration Checkbox

- SantimPay Employee Full Name

- Merchant City

- Data Encoder Confirmation



What I need from you:

1. Design a complete enterprise-grade PostgreSQL database schema.

2. Normalize the data properly into tables, relationships, foreign keys, indexes, and constraints.

3. Separate master data, operational data, and reporting data.

4. Recommend the best table structure for merchants, branches, terminals, SIM cards, deployments, calls, KYC change requests, transactions, users, roles, tasks, and audit logs.

5. Propose a clean naming convention for tables and columns.

6. Show which fields should be mandatory, optional, unique, or validated.

7. Recommend how to store images, signatures, QR data, and uploaded files safely.

8. Design RBAC permissions for each employee role.

9. Include audit trail, version history, and status tracking.

10. Make the design future-proof for AI/automation, workflow rules, alerts, and reporting.

11. Suggest how to integrate task management and external systems like Trello later.

12. Provide API endpoint structure for the backend.

13. Provide a high-level architecture for React + Flutter + PostgreSQL + Docker + Kubernetes + Cloudflare.

14. Recommend whether to use event-driven design, queue workers, background jobs, and webhooks.

15. Suggest observability, logging, backup, and disaster recovery strategy.

16. Give me a production-ready design, not a simple student project.



Important requirements:

- Think like a fintech architect.

- Use enterprise standards.

- Avoid duplicate data.

- Support many merchants, many branches, many terminals, many employees, and many workflows.

- Make the system secure, scalable, maintainable, and easy to report on.

- Include reporting tables or materialized views only if needed.

- Show how to store historical changes without losing old data.

- Design for future AI features such as auto-follow-up suggestions, anomaly detection, merchant risk scoring, and workflow automation.

- Do not oversimplify.

- Do not give me only generic advice. I want concrete table names, field names, relationships, and architecture decisions.



Output format I want:

A. System overview

B. ERD-style table list with relationships

C. PostgreSQL schema design

D. Role-based access model

E. API design

F. Deployment architecture

G. Security and audit design

H. AI/automation readiness

I. Recommended implementation roadmap



After that, also give me:

- A minimal version for MVP

- A more advanced version for enterprise scale

- A sample SQL schema for the most important tables

- A sample folder structure for backend and frontend

- A sample dashboard layout for managers and supervisors



Use professional, precise, and implementation-ready language.You are a Principal Fintech Architect, Principal Database Engineer, Principal Platform Engineer, Enterprise Solution Architect, and Staff Product Designer.

Your task is to design a production-grade POS Management Platform equivalent to what would be used internally by a large fintech company processing millions of transactions and managing thousands of merchants and POS terminals.

Do not act like a tutorial writer.

Do not provide generic explanations.

Do not provide simplified examples.

Think and respond as if you are preparing architecture documentation for a fintech CTO, Head of Operations, Head of Technology, Platform Engineering Team, Backend Team, Mobile Team, Data Team, and Security Team.

=================================================
BUSINESS CONTEXT
=================================================

The company manages:

- POS devices
- Merchants
- Merchant branches
- Deployments
- KYC changes
- Merchant onboarding
- Call center follow-ups
- Transaction monitoring
- Settlement accounts
- Employee task assignment
- Field operations
- Device inventory
- Device lifecycle
- AI-driven automation (future)
- Executive reporting

The platform must support:

- Thousands of merchants
- Multiple branches per merchant
- Multiple POS devices per branch
- Multiple settlement accounts
- Multiple employee roles
- Audit tracking
- Historical changes
- AI readiness
- Workflow automation

=================================================
TECHNOLOGY STACK
=================================================

Frontend:
- React
- TypeScript
- Material UI or enterprise UI framework

Mobile:
- Flutter Android

Backend:
- Golang OR NestJS (recommend one)
- REST API
- GraphQL where appropriate

Database:
- PostgreSQL

Cache:
- Redis

Storage:
- S3 compatible object storage

Infrastructure:
- Docker
- Kubernetes
- Terraform

Hosting:
- Ubuntu 24 LTS

Networking:
- Cloudflare
- Cloudflare Tunnel
- WAF
- SSL

Monitoring:
- Prometheus
- Grafana
- Loki

CI/CD:
- GitHub Actions

=================================================
BUSINESS MODULES
=================================================

MODULE 1
POS INVENTORY MANAGEMENT

Fields include:

- MRC Trading Name
- Registered Name
- Address
- Branch
- POS Terminal ID
- Phone
- POS Serial Number
- Bank Name
- Merchant Account
- SIM Card Number
- SIM Type
- Production Date
- Status
- QR
- Remarks
- Combined KCV
- Location
- Location Comment

=================================================

MODULE 2
DAILY POS DEPLOYMENT

Fields include:

- Email Address
- Fill Date
- Merchant License Name
- Merchant Address
- Merchant Phone Number
- Received By
- Device Serial Number
- Number Of Branches
- Branch List
- SantimPay Employee Name
- Conversation Notes
- Deployment Photo
- Signature
- Branch Name
- Settlement Bank
- Settlement Account
- Trade Name
- Terminal ID
- Merchant Shop Photo
- Trello Card ID

=================================================

MODULE 3
MONTHLY POS TRANSACTIONS

Fields include:

- Month
- Terminal ID
- Terminal Name
- Merchant ID
- Total Purchase Count
- Total Purchase Amount
- Gateway Transaction Count
- Gateway Transaction Amount
- Total Transaction Count
- Total Transaction Amount
- SantimPay Commission
- Total Commission BR
- Total Commission Cut

=================================================

MODULE 4
POS FOLLOW-UP MANAGEMENT

Fields include:

- Merchant Name
- Merchant License Name
- Merchant Phone
- Device Serial Number
- First Follow-Up
- Comment
- Contacted Person
- New Contact Phone

=================================================

MODULE 5
MERCHANT KYC CHANGE REQUESTS

Fields include:

- Timestamp
- Email
- Date
- Current Merchant Trade Name
- Merchant ID
- Terminal ID
- Merchant Owner Name
- Owner Phone
- Change Type
- Settlement Account Change
- Trade Name Change
- Both Change
- Change Reason
- Declaration
- Employee Name
- Merchant City
- Encoder Confirmation

=================================================
EMPLOYEE MANAGEMENT
=================================================

Design complete RBAC.

Roles:

- Super Admin
- Admin
- Manager
- Supervisor
- Sales
- Call Center
- Data Encoder
- Finance
- Field Officer
- Support
- Auditor
- Compliance
- IT Administrator

Requirements:

- Permission matrix
- Hierarchical permissions
- Approval workflows
- Segregation of duties
- Least privilege principle

=================================================
AI & AUTOMATION REQUIREMENTS
=================================================

The platform must be designed for future AI.

Design data structures supporting:

- Merchant risk scoring
- Churn prediction
- Deployment recommendations
- Sales performance scoring
- Fraud detection
- Merchant segmentation
- Call center recommendations
- Auto-generated tasks
- Intelligent workflow routing
- Predictive maintenance
- Executive AI dashboards

=================================================
DATABASE REQUIREMENTS
=================================================

Produce:

1. Complete ERD

2. Fully normalized PostgreSQL design

3. Table definitions

4. Relationships

5. Foreign keys

6. Indexing strategy

7. Partitioning strategy

8. Data retention strategy

9. Historical versioning strategy

10. Audit log strategy

11. Event sourcing recommendation

12. Multi-tenancy recommendation

13. Backup strategy

14. Disaster recovery strategy

15. Data warehouse strategy

16. Materialized views

17. Reporting architecture

18. Naming conventions

19. UUID strategy

20. Soft delete strategy

=================================================
SECURITY REQUIREMENTS
=================================================

Design:

- RBAC
- MFA
- JWT
- Refresh Tokens
- Secrets Management
- Encryption At Rest
- Encryption In Transit
- Cloudflare Protection
- Audit Trails
- Session Management
- Security Monitoring
- Compliance Controls

=================================================
ARCHITECTURE REQUIREMENTS
=================================================

Provide:

Level 1 Architecture

Level 2 Architecture

Level 3 Architecture

Include:

- Client Layer
- API Layer
- Service Layer
- Data Layer
- Infrastructure Layer

Show:

- API Gateway
- Auth Service
- Merchant Service
- POS Service
- Deployment Service
- KYC Service
- Task Service
- Reporting Service
- Notification Service
- Audit Service

=================================================
API DESIGN
=================================================

Design:

- REST endpoints
- Request examples
- Response examples
- Pagination
- Filtering
- Search
- Bulk imports
- Bulk exports

=================================================
DASHBOARDS
=================================================

Design dashboards for:

- CEO
- Operations Manager
- Sales Manager
- Call Center Manager
- Finance
- Compliance
- Support Team
- Data Encoder

Include KPI definitions.

=================================================
OUTPUT FORMAT
=================================================

Return the response in this exact structure:

# Executive Summary

# Business Domain Model

# Enterprise Architecture

# Service Architecture

# Database Architecture

# Complete PostgreSQL Schema

# ERD Relationship Map

# RBAC Model

# Workflow Engine Design

# AI & Automation Readiness

# Security Architecture

# API Design

# Reporting Architecture

# Dashboard Design

# DevOps Architecture

# Kubernetes Deployment Design

# Terraform Infrastructure Design

# Monitoring & Observability

# Backup & Disaster Recovery

# Scalability Strategy

# Future RoadmapI can also turn this into a much stronger prompt for building the actual app/database in Flutter + backend + SQL.

# Risks & Recommendations

# CTO-Level Final Assessment

Be extremely detailed.

Think like a fintech CTO and principal architect.

Challenge weak assumptions.

Recommend improvements where appropriate.I can also turn this into a much stronger prompt for building the actual app/database in Flutter + backend + SQL.

Do not simplify anything.I can also turn this into a much stronger prompt for building the actual app/database in Flutter + backend + SQL.I can also turn this into a much stronger prompt# MASTER PROJECT INSTRUCTION

Read ALL prompts in this document and treat them as a SINGLE project specification.

Do NOT answer each prompt separately.

Merge all requirements into ONE unified enterprise-grade fintech platform architecture.

Your responsibility is to act as:

* CTO
* Enterprise Solution Architect
* Principal DevOps Engineer
* Principal Platform Engineer
* Principal Database Architect
* Principal Backend Architect
* Principal Frontend Architect
* Principal Flutter Architect
* Principal Kubernetes Architect
* Principal Security Architect
* Principal AI Architect

The final result must be a complete production-ready system design and implementation plan.

## Project Goal

Build a world-class fintech POS Management Platform for SantimPay.

The platform will manage:

* POS Inventory
* Merchant Management
* Merchant Branches
* Merchant Onboarding
* Daily POS Deployment
* Device Lifecycle Management
* Merchant KYC Updates
* Call Center Follow-ups
* Transaction Analytics
* Employee Management
* Task Assignment
* Approval Workflows
* Reporting
* AI Automation
* Future AI Agents

This is NOT a student project.

This is a production enterprise system intended to support thousands of merchants, thousands of POS terminals, hundreds of employees, and future expansion.

---

## Infrastructure Requirements

Deployment Environment:

* Proxmox VE
* Ubuntu Server 24.04 LTS
* Docker
* Kubernetes
* Terraform
* Ansible
* GitHub
* GitHub Actions
* ArgoCD
* Cloudflare Tunnel
* Cloudflare DNS
* Cloudflare WAF
* Cloudflare Zero Trust

Everything must be self-hosted.

No AWS.
No Azure.
No GCP.

---

## Architecture Requirements

Design:

* Enterprise Monolith vs Microservices analysis
* Recommend the best architecture
* Domain Driven Design (DDD)
* Clean Architecture
* Hexagonal Architecture
* Event-Driven Architecture
* API First Design

Provide:

* System Context Diagram
* Container Diagram
* Component Diagram
* Deployment Diagram
* Network Diagram
* Security Diagram

---

## Database Requirements

Create:

* Complete PostgreSQL Schema
* ERD
* Foreign Keys
* Constraints
* Triggers
* Audit Tables
* History Tables
* Soft Deletes
* Partitioning
* Materialized Views
* Reporting Views

Design tables for:

* Users
* Roles
* Permissions
* Employees
* Merchants
* Merchant Owners
* Merchant Branches
* POS Devices
* SIM Cards
* Banks
* Settlement Accounts
* Deployments
* Device Assignments
* Transactions
* KYC Requests
* Follow Ups
* Tasks
* Workflow Approvals
* Notifications
* Audit Logs
* Attachments
* AI Data

---

## Development Standards

Generate:

* Repository Structure
* Backend Folder Structure
* Frontend Folder Structure
* Flutter Folder Structure
* Terraform Structure
* Kubernetes Structure
* GitHub Actions Structure

Use:

* Clean Code
* SOLID Principles
* DDD
* CQRS where appropriate
* Repository Pattern
* Service Layer Pattern

---

## DevOps Requirements

Design:

* GitHub Organization
* Repository Strategy
* Branching Strategy
* CI/CD
* GitOps
* ArgoCD
* Container Registry
* Secrets Management
* Backup Strategy
* Disaster Recovery
* Monitoring
* Logging
* Tracing

Tools should include:

* GitHub
* GitHub Actions
* Docker
* Kubernetes
* Terraform
* Ansible
* ArgoCD
* PostgreSQL
* Redis
* MinIO
* Keycloak
* Prometheus
* Grafana
* Loki
* Tempo
* Harbor
* Trivy
* Vault

---

## AI Requirements

Prepare the platform for future AI features:

* Merchant Risk Scoring
* Merchant Health Score
* Sales Performance Score
* POS Failure Prediction
* Automated Follow-up Generation
* Workflow Automation
* AI Reporting
* AI Assistant
* RAG Architecture
* Vector Database Integration
* LLM Integration

---

## Expected Output

Produce:

1. Executive Summary
2. Architecture Decisions
3. System Diagrams
4. Domain Model
5. Database Design
6. API Design
7. Frontend Design
8. Flutter Design
9. Kubernetes Design
10. Terraform Design
11. Security Design
12. Monitoring Design
13. Backup Design
14. Disaster Recovery Design
15. AI Architecture
16. Development Roadmap
17. Sprint Plan
18. Team Structure
19. Repository Structure
20. Production Deployment Guide

Challenge bad design choices.

Recommend better alternatives.

Think like a fintech CTO preparing a system that must operate reliably for the next 5 years.

The final answer should be detailed enough that a team of developers can start implementation immediately.
 for building the actual app/database in Flutter + backend + SQL.