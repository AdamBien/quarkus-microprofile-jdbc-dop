## Capability
- name: `releases`
- kind: new
- goal: a developer finds a JDK version and every JEP it shipped, from the example set the system ships with
- responsibility: keep the shipped example set of JDK versions and their JEPs, load it on a developer's request, and answer which versions are known and what each shipped

## Actors
- primary: a developer
- supporting: none

## Main scenario
1. `load-examples` — the developer asks the system to load the shipped example set; the request carries no input.
2. The system stores every JDK version of the shipped set, 25 and 27, with all the JEPs each shipped, as a whole, and reports the versions loaded.
3. `list-versions` — the developer asks which JDK versions are known.
4. The system lists the known versions, ascending by feature number, each with its count of JEPs; no limit.
5. `find-version` — the developer asks for one JDK version by its feature number.
6. The system returns the version and every JEP it shipped, each with its number and title, ascending by JEP number; no limit; an unknown version is reported as not found.

## Inputs
- feature number: one whole number, like 25; required; invalid when missing, not a whole number, or below one
- load request: carries no input

## Extensions
- 1a. the example set is already loaded -> nothing changes; the system reports it as already loaded and names the versions present
- 2a. the store cannot be reached -> nothing is stored; the system reports itself temporarily unavailable
- 2b. loading fails midway -> none of the set remains; the system reports the failure; a later load retries
- 4a. the store cannot be reached -> the system reports itself temporarily unavailable
- 6a. the version is not in the loaded set, or nothing is loaded yet -> the system reports the version as not found
- 6b. the store cannot be reached -> the system reports itself temporarily unavailable
- *a. the developer stops responding -> nothing is kept between requests; no effect

## Lifecycle
- none; each request stands alone. The loaded example set is owned data, not kept conversational state.

## Entities
- JDK version, JEP

## Out of scope
- finding one JEP by its number across versions — weighed and dropped; the version is the unit of lookup
- versions other than 25 and 27 in the shipped set — the example is these two
- unloading or resetting the stored set — a repeated load changes nothing, nothing removes it
- recording versions or JEPs while the system runs — the maintainer curates the set before a release (system brief)
- JEP full text, vendor distributions, live sync with openjdk.org — excluded in the system brief

## Decisions
- record: the shipped example set is loaded on a developer's explicit request _(rejected: lazily on the first lookup; at start when the store is empty)_
- record: a repeated load changes nothing and reports already loaded _(rejected: replacing the stored set with the shipped one; rejecting the second load)_
- record: a load brings the whole shipped set, all or nothing _(rejected: loading one version at a time; leaving a partial set behind)_
- record: the shipped set holds JDK versions 25 and 27 _(rejected: a single example version; closes the system brief's open issue)_
- record: a JDK version is identified by its feature number alone _(rejected: a full version string; number or name)_
- record: JEPs are ordered by number _(rejected: by title)_
- record: an unknown version is reported as not found _(rejected: answering with an empty JEP list)_
- record: an unreachable store is reported as temporarily unavailable _(rejected: answering not found)_
- record: listing the known versions is part of the capability _(rejected: lookup by version only; finding a JEP by number)_

## Open issues
- before any load, does listing versions answer with an empty list, or with a notice that nothing is loaded yet?

---

Hand-off, after the brief is read:

```
/sbce new releases-brief.md
```

### Note for `/sbce new`, outside the brief

The author wants the shipped example set expressed as SQL statements that belong to the business component, and PostgreSQL as the store. This is a stack matter, owned by the JDBC persistence rules, and is carried here so the stack step starts from it.
