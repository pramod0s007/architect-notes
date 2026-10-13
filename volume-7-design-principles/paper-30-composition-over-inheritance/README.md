# Composition Over Inheritance

**Principle:** Composition over inheritance (a test, not a ban on `extends`)

---

## Read the Full Article on Medium

[Composition Over Inheritance](https://medium.com/@replytopramods.aws/composition-over-inheritance-b5418fa73c11)

---

## What This Paper Is About

Inheritance models exactly one axis of variation: "is a specialized version of, permanently." The moment a second independent axis appears — format, then encryption, then compression — inheritance doesn't add a class per new concern, it multiplies the whole existing tree. This paper shows the multiplication happen on a report generator (format x encryption x compression x signing) and the fix: model each concern as its own object and compose a list of them.

## The Pressure: Multiplying Classes

Two independent concerns (encryption on/off, compression on/off) already needs 4 subclasses through inheritance, including two separate classes just to express "encrypt then compress" vs. "compress then encrypt." A third concern doesn't add a class — it doubles the tree that already exists.

## The Principle

**Composition over inheritance is a test: is the relationship "is-a, permanently, on exactly one axis" (inheritance is fine) or "has-a/does-a, on two or more independent axes" (composition is required)?** Each independent concern becomes its own object; the class that used to sit at the top of a combinatorial hierarchy instead holds a list of collaborators.

## Pros

- A new independent concern is one new class, not a doubled tree
- Order-dependent combinations ("encrypt then compress" vs. the reverse) become list order, not two hand-written classes
- Capabilities can be composed, recombined, or omitted per-instance without touching any existing class

## Cons

- Over-atomizing collaborators that never vary independently just obscures control flow behind too many constructor arguments
- Composition requires the concerns to actually be independent; if they're not, you've added indirection for nothing
- Slightly more objects to construct and wire together per call site compared to `new ConcreteSubclass()`

## When NOT to Use

- Exactly one axis of variation, permanently — `Circle`/`Rectangle extends Shape` never needs a second "kind"
- Framework-mandated extension points that require subclassing to hook into a lifecycle
- A small number of stable template-method base classes where a second axis is genuinely never going to appear

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Report generation | Single-axis inheritance (fine) -> multiplying subclass tree (encrypt x compress) -> fixed via composed `OutputTransform` list |
| [`vehicle-fleet/`](./vehicle-fleet/) | Logistics | Fleet vehicles with independently combinable capabilities (GPS, refrigeration) instead of a subclass per combination |

### How to Run

```bash
cd volume-7-design-principles/paper-30-composition-over-inheritance/evolution/v1
javac *.java && java Main

cd volume-7-design-principles/paper-30-composition-over-inheritance/evolution/v2
javac *.java && java Main

cd volume-7-design-principles/paper-30-composition-over-inheritance/evolution/v3
javac *.java && java Main

cd volume-7-design-principles/paper-30-composition-over-inheritance/vehicle-fleet
javac *.java && java Main
```
