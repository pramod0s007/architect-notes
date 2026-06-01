# Paper 19 — Adapter Pattern: Evolution Examples

**Domain:** Database Connector (SQL vs NoSQL)

**Adapter Rule: translate only. No business logic in adapters.**

## Progression

| Version | File | State | Core Problem |
|---------|------|-------|--------------|
| v1 | `v1_DirectDependency.java` | Coupled to MySqlDriver | `executeQuery()`, `openConnection()` embedded in CustomerService; migration = rewrite |
| v2 | `v2_InterfaceOnly.java` | Interface extracted, gap remains | `DatabaseClient` exists but `MySqlDriver` can't implement it (third-party jar) |
| v3 | `v3_AdapterApplied.java` | Adapters bridge the gap | `MySqlAdapter` and `MongoAdapter` translate; CustomerService and both drivers unchanged |

## Why This Domain

Database migration is the highest-stakes integration problem in enterprise systems.
The Adapter Pattern is the mechanism that makes "swap the database" possible without
rewriting the service — as long as the adapters are kept clean.

## How to Run

```bash
cd volume-5-structural-patterns/paper-19-adapter-pattern/evolution

javac -d out v1_DirectDependency.java v2_InterfaceOnly.java v3_AdapterApplied.java

java -cp out evolution.v1_DirectDependency
java -cp out evolution.v2_InterfaceOnly
java -cp out evolution.v3_AdapterApplied
```

## The Translation Map (v3)

| DatabaseClient | MySqlAdapter → MySqlDriver | MongoAdapter → MongoDriver |
|---------------|--------------------------|---------------------------|
| `connect()` | `openConnection()` | `init()` |
| `query(sql)` | `executeQuery(sql)` | `find(collection, filter)` |
| `update(sql)` | `executeUpdate(sql)` | `insertOne(collection, doc)` |
| `disconnect()` | `closeConnection()` | `close()` |

## The Adapter Rule in Practice

An adapter that grows beyond translation is a liability:

| What you find in the adapter | Where it actually belongs |
|-----------------------------|--------------------------|
| Retry logic | `RetryProxy` wrapping the adapter |
| Caching | `CachingProxy` (see Paper 18 v3) |
| Input validation | Service layer |
| Error mapping | Acceptable — translating driver exceptions to domain exceptions |
| Connection pooling | Infrastructure config, not adapter |

The test for "is this translation?": if you removed the adapter and wrote the driver
call directly in the service, would this code be needed? If yes — it's business logic
and belongs in the service. If no — it might be translation.

## What CustomerService Gained

```
v1: CustomerService -> MySqlDriver (forever)
v3: CustomerService -> DatabaseClient <- MySqlAdapter -> MySqlDriver
                                      <- MongoAdapter -> MongoDriver
                                      <- PostgresAdapter -> PostgresDriver (future)
```

CustomerService did not change between v1 and v3.
MySqlDriver did not change.
MongoDriver did not change.
The adapters bridged incompatible APIs without touching either side.
