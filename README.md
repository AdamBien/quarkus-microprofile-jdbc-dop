# 🚀Quarkus with MicroProfile, plain JDBC and Data-Oriented Programming on BCE

Data-Oriented Programming with plain JDBC on Quarkus/MicroProfile, PostgreSQL as the store: records are the row shapes, SQL hand-written in text blocks, no ORM. Example domain: a JDK release with all its JEPs, persisted inside one BCE business component. Minimal dependencies, system tests in a standalone module.

What the system is for, who uses it and how it is carved is recorded in the [system brief](system-brief.md): one business capability, `releases`, where a developer finds a JDK version and every JEP it shipped from a read-only example set that ships with the system.

BCE-structured 👉 [bce.design](https://bce.design) | AI-assisted with 👉 [airails.dev](https://airails.dev)

## Getting Started

See [AGENTS.md](AGENTS.md#build--test) for build, dev mode, and system test instructions. Dev mode and the tests start PostgreSQL through Quarkus Dev Services, so Docker must be running.

## Modules

- [service](service/README.md) - Quarkus application module with BCE structure
- [service-st](service-st/README.md) - System tests for the service module

## [/sbce](https://sbce.space) Quickstart

Spec-driven BCE 👉 [sbce.space](https://sbce.space): one capability spec ≡ one business component. The spec lives in the BC's `package-info.java` and is the boundary contract; a green test run is the only definition of done. The `/sbce` skill and its companions are installed from 👉 [airails.dev](https://airails.dev).

```
/capability-brief "releases — new — a developer finds a JDK version and every JEP it shipped"   # from the system brief: the capability interview, input to /sbce new
/sbce new                                           # reads the brief: authors the spec, scaffolds boundary/control/entity
/sbce apply releases                                # converge: close the spec-vs-code gap until the test loop is green
```

- `new` writes the spec (boundary ops + [EARS](https://alistairmavin.com/ears/) requirements) — no business code yet.
- `apply` is idempotent: each boundary op becomes a boundary method, each requirement id (`R1.1`, …) a traceable test; code drift without a spec counterpart is surfaced, never absorbed.
