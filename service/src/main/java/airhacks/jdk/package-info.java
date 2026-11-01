/// # Jdk
/// > Let a developer find a JDK version together with every JEP it shipped, from an example set the system ships with.
///
/// ## Vision
/// - A reader of the project understands how persistence lives inside a business component from the code alone.
///
/// ## Components
/// - No BC calls another. `health` holds only probes without a business owner and reads nothing from `releases`.
///
/// ## Ubiquitous language
/// - JDK version — one OpenJDK release, the unit a developer looks up. Owned by `releases`.
/// - JEP — a JDK Enhancement Proposal that shipped as part of a JDK version, known by its number and title only. Owned by `releases`.
/// - Example set — the JDK versions and JEPs curated before a release of the system and shipped with it, read-only while the system runs. Owned by `releases`.
///
/// ## Decisions
/// - D1 — One `releases` capability holds JDK versions and their JEPs. _(why: one party decides the rules of both, and neither is described without the other; rejected: `versions` and `jeps` apart)_
/// - D2 — The example set is seeded once and read-only at runtime. _(why: the maintainer curates the set before a release, not as a runtime actor; rejected: a maintainer recording at runtime; importing from openjdk.org)_
/// - D3 — The system is named `jdk`, replacing the `qmp` template placeholder. _(why: names the thing the demo is about; rejected: `releases`, `jeps`)_
/// - D4 — The template's `greetings` sample is removed. _(why: the template's hello-world, not part of this system; rejected: keeping it as an existing BC, which would need a package doc)_
///
/// ## Stack
/// - microprofile-server + jdbc-persistence on PostgreSQL · base package `airhacks.jdk`
package airhacks.jdk;
