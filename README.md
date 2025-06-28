Disclaimer: As the agentic field is evolving, the current design is in the re-evaluation phase. For the time being, no more changes will be done.

# Adaptflow Java Server
This project simplifies RAG/Agent creation and testing even for non-technical users through a visual BPMN 2.0 modeler and execution engine. It bridges the gap between codeless prototyping and production by enabling the export of designed models as standalone applications, initially supporting Java and planned expansion to other languages like Python.
## Table of Contents
<a name=top></a>

*   [Development Guide](#dev-guide)
    - [Infra Prerequisite](#infra)
    - [Dev Tools](#dev-tools)
    - [Other project dependencies at high level](#other-project-dependencies)
    - [Development Setup guide](#dev-setup)
    - [UI](#ui)
    - [Guidelines](#guidelines)
*   [About Project](#about-project)
## Development Guide
<a name="dev-guide"></a>

### Infra Prerequisite <a name="infra"></a>
- Java 21
  - Windows: [openjdk-21.0.2_windows-x64_bin.zip](https://download.java.net/java/GA/jdk21.0.2/f2283984656d49d69e91c558476027ac/13/GPL/openjdk-21.0.2_windows-x64_bin.zip)
  - Linux/x64: [openjdk-21.0.2_linux-x64_bin.tar.gz](https://download.java.net/java/GA/jdk21.0.2/f2283984656d49d69e91c558476027ac/13/GPL/openjdk-21.0.2_linux-x64_bin.tar.gz)
- Maven: 3.9.9 [Download link](https://maven.apache.org/download.cgi)
- Redis: 7.4.2 [free cloud instance](https://cloud.redis.io)
- Postgresql 16.6 [Download link](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads)


### Dev Tools <a name="dev-tools"></a>
- Eclipse: [Download link](https://www.eclipse.org/downloads/packages/)
- Postman: [Download link](https://www.postman.com/downloads/)
- pgAdmin4: [Download link](https://www.pgadmin.org/download/) (optional)
- Redis Insight: [Download link](https://redis.io/downloads/) (optional)

### Other project dependencies at high level <a name="other-project-dependencies"></a>
- Springboot 3.3.5
- activiti 8.6.0 (alfresco)

### Development Setup guide <a name="dev-setup"></a>
1. Git clone
    - `git clone https://github.com/adaptflow/af-serverj.git`
2. Do infra setup as per the current working enviornment mentioned in [Infra Prerequisite](#infra)
3. Update infra details in `af-serverj\src\main\resources\application.yml` file
4. Go to root folder i.e. af-serverj and do `mvn clean install`
5. Now, import this maven project in eclipse
6. Run this main class as Java Project: `com.adaptflow.af_serverj.AfServerjApplication`
7. All API specifications are now accessible via swagger UI at: http://localhost:8080/swagger-ui.html
8. Login to server
    - API: http://localhost:8080/api/auth/login, POST call with the below body
        - `{
        "username": "admin123",
        "password": "qwertyuiop"
        }`
    - JWT token will be set in cookies

### UI <a name="ui"></a>
UI for this project is available here: https://github.com/adaptflow/adaptflow-modeler

### Guidelines
1. Please ensure that any new API or modifications to existing APIs are accompanied by corresponding Swagger implementations to help other developers consume them easily

## About Project
<a name="about-project"></a>
### Problems
1. To test RAG/Agents, we need to write repeativie/boilerplate code which needs technical skills. Non-technical users like Business analysts, operations managers, marketing/sales teams, etc. find it difficult to design and test AI agents. They may use codeless applications for the same.
2. Even after using codeless application to prototype an idea/project, currently there are technical challenges to seamlessly bring such codeless applications into the production.

### Solution
#### Phase 1:
Use cases:
1. [UI] BPMN 2.0 based modeler to allow a user to design RAG/Agent as a process and also facilitate execution to test them and it can be chat system.
2. [BACKEND] BPMN 2.0 based execution engine to execute the processes and return the output to UI(chat system)
---
#### Phase 2:
Use cases:
1. Export designed model as standalone java application.
---
#### Phase 3:
Use cases:
1. In addition to Java, allow other backends like python to execute BPMN process based RAG/Agent models and export them as standalone application.
---

[go top](#top)