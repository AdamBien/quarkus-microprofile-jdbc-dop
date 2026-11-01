## System
- name: `jdk`
- mission: let a developer find a JDK version together with every JEP it shipped, from an example set the system ships with
- vision: a reader of the project understands how persistence lives inside a business component from the code alone

## Actors
- primary: a developer — looks up a JDK version and sees every JEP it shipped
- external: none — the example data ships with the system; nothing is relied on at runtime

## Capabilities
1. `releases` — new — a developer finds a JDK version and every JEP it shipped; the system keeps the shipped example set and answers from it. _(first: it is the only business capability and the reason the demo exists)_
2. `health` — existing — an operator or orchestrator learns whether the system is alive and ready; the system answers from checks that no business component owns.

## Interactions
- none known yet — `health` holds only checks without a business owner and reads nothing from `releases`

## Language
- JDK version — one OpenJDK release, the unit a developer looks up; owned by `releases`
- JEP — a JDK Enhancement Proposal that shipped as part of a JDK version, known by its identity and title only; owned by `releases`
- Example set — the JDK versions and JEPs curated before a release of the system and shipped with it, read-only while the system runs; owned by `releases`

## Invariants
- none spanning two capabilities; the rules of the example set belong to `releases` and are carried as notes below

## Success measures
- a system test against the running service shows a developer getting a JDK version with all its JEPs
- a reader of the project sees how persistence sits inside a business component without reading any docs

## Out of scope
- JEP full text — only identity and title; openjdk.org owns the body
- vendor distributions — no Temurin, Zulu or Oracle builds; one JDK version is one OpenJDK release
- live sync with openjdk.org — the data is the shipped example, nothing is fetched
- recording versions or JEPs while the system runs — the maintainer curates the example set before a release, not as a runtime actor
- the template's `greetings` sample — the template's hello-world, not part of the `jdk` system; removed

## Decisions
- record: one `releases` capability holds JDK versions and their JEPs _(rejected: `versions` and `jeps` apart — one party decides the rules of both, and neither is described without the other)_
- record: the example set is seeded once and read-only at runtime _(rejected: a maintainer recording at runtime; importing from openjdk.org)_
- record: the system is named `jdk`, replacing the `qmp` placeholder _(rejected: `releases`, `jeps`)_
- record: `greetings` is removed _(rejected: keeping it as an existing capability, which would need a package doc)_

## Open issues
- the shipped example is one JDK version; does the system hold more than one version, or exactly that one?

---

Hand-off, in build order; read and sign off the brief between the two steps:

```
/capability-brief "releases — new — a developer finds a JDK version and every JEP it shipped; the system keeps the shipped example set and answers from it."
/sbce new
```

`health` gets no line, it is already specified.

### Notes for `/capability-brief releases`

Not part of the brief, not checked by its checklist.

- The example set is seeded once and never changed while the system runs. Every operation is a read.
- Which JDK version is the example is still to be named. The build targets release 25, which is a hint, not an answer.
- A JEP carries identity and title only, no body.
- The system test against the running service is the acceptance evidence the stakeholders named.
- Stack hints for `/sbce new`, not for the brief: MicroProfile server with JDBC persistence.
