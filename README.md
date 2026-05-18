# OBJ2100 – NorthLine Services Support System

Hjemmeeksamen OBJ2100 – Objektorientert programmering 2  
Innleveringsfrist: 21.05.2026 kl. 12:00

## Systemoversikt

Sentralisert klient-server-system for håndtering av støttehenvendelser (tickets).

### Roller
- **Registrator** – registrerer og kansellerer henvendelser
- **Supportagent** – henter og fullfører henvendelser

### Teknisk stack
- Java (TCP-sockets, ObjectInputStream/ObjectOutputStream)
- Flertrådede klienttilkoblinger
- Synkronisert delt tilstand

## Prosjektstruktur

```
src/
├── protocol/     # Delte meldingsklasser (Request, Response, enums)
├── server/       # Server, ClientHandler, TicketStore, Logger
└── client/       # RegistrarClient, AgentClient
```

## Branch-strategi

| Branch | Formål |
|--------|--------|
| `main` | Stabil, kompilerbar kode – kun merge fra `dev` via PR |
| `dev` | Integrasjonsbranch – alle features merges hit |
| `feature/protocol` | Protokollklasser og meldingsobjekter |
| `feature/server` | Server og synkroniseringslogikk |
| `feature/client` | Registrar- og agentklienter |
| `feature/logging` | Logger og loggingsmekanisme |

## Kjøring

```bash
# Kompiler
javac -d out src/**/*.java

# Start server
java -cp out server.Server

# Start registratorklient
java -cp out client.RegistrarClient

# Start agentklient
java -cp out client.AgentClient
```
