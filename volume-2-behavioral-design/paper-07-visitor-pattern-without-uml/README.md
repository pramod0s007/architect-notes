# Visitor Pattern Without UML

**Pattern:** Visitor Pattern

---

## Read the Full Article on Medium

[Visitor Pattern Without UML](https://medium.com/@replytopramods.aws/visitor-pattern-without-uml-0ec8915fa2a5)

---
## What This Paper Is About

Visitor Pattern addresses the **double dependency problem**: when an operation's behavior depends on two types simultaneously, and adding either a new type or a new operation forces changes across the entire codebase.

Most tutorials start with a UML diagram. This paper starts with the pressure that makes the pattern necessary.

## The Pressure: Object Interaction Matrix

Consider a document processing service that accepts PDF, Word, and HTML documents, and applies operations: validation, metadata extraction, text indexing, thumbnail generation.

```
           validate  extractMeta  indexText  thumbnail
PDF        ✓         ✓            ✓          ✓
Word       ✓         ✓            ✓          ✓
HTML       ✓         ✓            ✓          ✓
```

Every new document type adds 4 methods. Every new operation adds 3 methods. The matrix grows in two directions simultaneously. Logic for each combination gets scattered across service classes.

**This is double dispatch pressure.** Java method dispatch is single — the method that runs depends on one type. Visitor creates structured double dispatch.

## The Pattern

```
document.accept(new ValidationVisitor())
  → ValidationVisitor.visit(PdfDocument)   // dispatch on document type
  → ValidationVisitor.visit(WordDocument)
  → ValidationVisitor.visit(HtmlDocument)
```

New operation = one new Visitor class. Zero changes to document classes.
New document type = add one `visit()` to the interface → compiler enforces completeness.

## When Visitor Wins

- Stable type hierarchy (you know all document/node types)
- Growing operations (linting rules, formatters, code generators)
- Operations need to dispatch on the concrete type

## When Visitor Loses

- Types grow frequently → every new type forces updates to every visitor
- Small finite matrix → a lookup table (`Map<Key, Action>`) is 20 lines and zero hierarchy
- Only one or two operations → polymorphism (virtual methods) is simpler

## Real-World Usage

Every major Java AST tool uses Visitor internally: Checkstyle, PMD, SpotBugs, JavaParser, FindBugs. A parser produces a fixed set of node types. Operations (lint rules, code generation, optimization) multiply over time.

## Read the Full Article


## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`visitor/collision-engine/`](./collision-engine/) | Game engine | Ship/Station/Comet/Asteroid — double dispatch for collision handling |
| [`visitor/document-processor/`](./document-processor/) | Content pipeline | PDF/Word/HTML × validation, metadata, indexing — 3 visitors, 3 types |

### How to Run

```bash
cd code-samples/visitor/collision-engine
javac *.java && java Main

cd code-samples/visitor/document-processor
javac *.java && java Main
```
