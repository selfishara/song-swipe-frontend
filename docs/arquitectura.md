flowchart LR
subgraph Frontend [Frontend (SPA) — React+TS, TanStack Query]
UI[Views/State/Router]
end

subgraph REST [Entrada (REST) — Controllers, DTOs, Validation]
CTRL[Controllers]
end

subgraph App [Aplicación — Casos de Uso]
UC1[UseCase: X]
UC2[UseCase: Y]
end

subgraph Domain [Dominio — DDD: Entities, VOs, Services, Ports]
ENT[Entities/Aggregates]
SRV[Domain Services]
PREPO[[Repository Port]]
PEXT[[Client Port]]
PCACHE[[Cache Port]]
end

subgraph Infra [Salida (Infra) — Implementaciones]
REPO[(RepositorioSQL — JPA/MySQL)]
EXT[(ClienteAPIExterna — HTTP)]
CACHE[(Cache — Redis)]
end

UI --> CTRL
CTRL --> UC1
UC1 --> SRV
SRV --> ENT
UC1 -. usa .-> PREPO
UC1 -. usa .-> PEXT
UC1 -. usa .-> PCACHE

REPO -. implements .-> PREPO
EXT -. implements .-> PEXT
CACHE -. implements .-> PCACHE

REPO -->|SQL/Transacciones| DB[(DB)]
EXT -->|HTTP/JSON| API[(API Externa)]
CACHE -->|GET/SET| RC[(Cache)]
