# Conditional Classification Examples

## Example 1 - Encryption

```java
if(type == AES)
   ...

if(type == DES)
   ...

if(type == BLOWFISH)
   ...
```

**Classification:** Behavior Variation

**Recommended Abstraction:** Strategy Pattern

## Example 2 - StopWatch

```java
if(state == IDLE)
   ...

if(state == RUNNING)
   ...

if(state == SUSPENDED)
   ...
```

**Classification:** State Variation

**Recommended Abstraction:** State Pattern

## Example 3 - Search Filters

```java
if(price > 1000)
   ...

if(category == ELECTRONICS)
   ...

if(rating > 4)
   ...
```

**Classification:** Rules Variation

**Recommended Abstraction:** Specification Pattern

## Example 4 - Collision Engine

```java
if(ship && station)
   ...

if(ship && comet)
   ...

if(station && asteroid)
   ...
```

**Classification:** Behavior Variation

**Recommended Abstraction:** Visitor Pattern or Lookup Table

## Architect Rule

Do not classify conditionals by syntax.

Classify them by the pressure that causes them to grow.
