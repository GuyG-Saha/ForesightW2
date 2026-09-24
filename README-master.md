# ForesightWorks — Project Management System (Take-Home Assignment)

This repository contains a Spring Boot implementation of a take-home coding
assignment: a small backend for managing a hierarchy of **Projects →
Subprojects → Tasks**, with uploaded JSON persisted to an in-memory H2
database, computed start/end dates, and a REST API documented via Swagger.

The full assignment description is in [`Description.pdf`](./Description.pdf).

## Two branches, two approaches

This repo intentionally holds two different solutions to the same problem,
built a couple of years apart:

### `master` — the original submission

This branch models every node (Project, Subproject, Task) as a **single entity class**
with a `type` discriminator field and a self-referencing `parentUid`, backed
by an H2 table via Spring Data JPA. Hierarchy is reconstructed at read time
from a flat set of `HashMap`s keyed by type and by uid.

This design was a deliberate, pragmatic choice: it maps directly onto the
flat JSON contract the assignment specifies, avoids fighting JPA with
polymorphic entity mapping, and keeps persistence trivial (one table, one
self-referencing foreign key).

### `composite-dp-version` — a design-pattern rework, for practice

Some time after the original submission, I revisited this same problem to
explore how it could be modeled using the **Composite design pattern**
instead — treating Project/Subproject/Task as a `Component`/`Composite`/`Leaf`
class hierarchy rather than one flat entity type. This branch is a from-scratch
rework of the service layer around that model. It's a learning exercise, not
a resubmission — see that branch's own README for the full writeup of the
design and the reasoning behind it.

## Running the project (`master`)

```bash
./gradlew bootRun
```

H2 console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`)

Swagger UI: see the controller package for the exposed endpoints.
