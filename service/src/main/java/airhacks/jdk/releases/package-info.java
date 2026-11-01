/// # Releases
/// > Keep the shipped example set of JDK versions and their JEPs, load it on a developer's request, and answer which versions are known and what each shipped.
///
/// ## Boundary
/// - `load-examples` — load the shipped example set; carries no input
/// - `list-versions` — list the known JDK versions
/// - `find-version` — return one JDK version, by feature number, with every JEP it shipped
///
/// ## Requirements
/// ### R1: Load the example set
/// - R1.1 — When the example set is requested to be loaded and none of it is stored, the BC shall store every JDK version of the shipped set, 25 and 27, with all the JEPs each shipped, and report the versions loaded.
/// - R1.2 — If the example set is already loaded, then the BC shall change nothing and report it as already loaded, naming the versions present. _(why: the load is idempotent; nothing at runtime removes or replaces the set)_
/// - R1.3 — If loading fails before the whole set is stored, then the BC shall leave none of the set stored and report the failure. _(why: a later load retries from a clean store; a partial set would be mistaken for the whole)_
///
/// ### R2: List the known versions
/// - R2.1 — When the known versions are requested, the BC shall list every stored JDK version ascending by feature number, each with its count of JEPs, without limit.
/// - R2.2 — While nothing is loaded, when the known versions are requested, the BC shall answer with an empty list.
///
/// ### R3: Find a version
/// - R3.1 — When a JDK version is requested by feature number and it is stored, the BC shall return the version and every JEP it shipped, each with its number and title, ascending by JEP number, without limit.
/// - R3.2 — If the requested feature number is missing, not a whole number, or below one, then the BC shall reject the request naming the invalid part.
/// - R3.3 — If the requested version is not stored, then the BC shall report it as not found. _(why: an unknown version is an absent target, not a version without JEPs)_
///
/// ### R4: Store availability
/// - R4.1 — If the store cannot be reached during any operation, then the BC shall change nothing and report itself temporarily unavailable.
///
/// ## Entities
/// - JDK version, JEP
///
/// ## Decisions
/// - D1 — The shipped example set is loaded on a developer's explicit request. _(why: the demo shows the load as a visible step; rejected: lazily on the first lookup; at start when the store is empty)_
/// - D2 — A repeated load changes nothing and reports already loaded. _(rejected: replacing the stored set with the shipped one; rejecting the second load)_
/// - D3 — A load brings the whole shipped set, all or nothing. _(rejected: loading one version at a time; leaving a partial set behind)_
/// - D4 — The shipped set holds JDK versions 25 and 27. _(why: closes the system brief's open question of one version or several; rejected: a single example version)_
/// - D5 — A JDK version is identified by its feature number alone. _(rejected: a full version string; number or name)_
/// - D6 — JEPs are ordered by number. _(rejected: by title)_
/// - D7 — An unknown version is reported as not found. _(rejected: answering with an empty JEP list)_
/// - D8 — An unreachable store is reported as temporarily unavailable. _(rejected: answering not found)_
/// - D9 — Listing the known versions is part of the capability. _(rejected: lookup by version only; finding a JEP by number)_
/// - D10 — Before any load, listing versions answers with an empty list. _(rejected: a notice that nothing is loaded yet)_
///
/// ## Out of scope
/// - finding one JEP by its number across versions — the version is the unit of lookup
/// - versions other than 25 and 27 in the shipped set
/// - unloading or resetting the stored set
/// - recording versions or JEPs while the system runs — the maintainer curates the set before a release
/// - JEP full text, vendor distributions, live sync with openjdk.org
package airhacks.jdk.releases;
