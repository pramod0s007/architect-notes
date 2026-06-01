# Visitor Pattern Without UML

*Every Visitor tutorial starts with a UML diagram. That's why most engineers don't understand when to actually use it.*

---

When I first read about Visitor Pattern, I had two reactions.

First: *"That's incredibly clever."*

Second: *"I will never need this."*

I was wrong about the second one. About eight months later, I was working on a document processing service that accepted three document types — PDF, Word, and HTML — and needed to apply four different operations to each: validation, metadata extraction, text indexing, and thumbnail generation.

My first implementation had twelve methods. `validatePdf()`, `validateWord()`, `validateHtml()`, `extractMetadataPdf()`, and so on. Every time a new document type arrived (EPUB came two sprints later), I was adding four new methods. Every time a new operation arrived (accessibility checking), I was adding three new methods.

The matrix was growing in two directions simultaneously. And the logic for each combination was scattered across four different service classes.

That's when Visitor Pattern clicked. Not from a textbook. From a 3×5 grid of methods that was about to become a 4×5 grid and I couldn't figure out where to put the new code without creating a dependency cycle.

Forget the UML. Start here.

You're building a 2D game. There are four types of game objects: ships, stations, comets, and asteroids. When two objects collide, something happens — damage is dealt, score is updated, objects are destroyed.

First attempt:

```java
void handleCollision(GameObject a, GameObject b) {
    if (a instanceof Ship && b instanceof Station)   { shipHitsStation(a, b);   }
    if (a instanceof Ship && b instanceof Comet)     { shipHitsComet(a, b);     }
    if (a instanceof Ship && b instanceof Asteroid)  { shipHitsAsteroid(a, b);  }
    if (a instanceof Station && b instanceof Comet)  { stationHitsComet(a, b);  }
    if (a instanceof Station && b instanceof Asteroid){ stationHitsAsteroid(a, b); }
    if (a instanceof Comet && b instanceof Asteroid) { cometHitsAsteroid(a, b); }
}
```

Four types. Six pairs. One method. Still manageable.

Then you add a fifth type: a satellite. Now you have ten pairs. A sixth type adds fifteen. A seventh adds twenty-one.

**The matrix grows quadratically.** Every new type adds N-1 new pairs.

And it gets worse. You don't just need collision logic. You also need:
- Rendering behavior (how each object draws itself)
- Physics behavior (how each object responds to gravity)
- Serialization (how each object saves to disk)

One new operation across four types = four new methods. Two new types = updating every operation.

**This is the double dependency problem.** The operation depends on *two* types at once, and adding either a new type or a new operation forces changes across the entire system.

---

## The Core Insight — Double Dispatch

Java method dispatch is single — the method that runs depends on one object's type (the one before the dot).

Collision handling depends on *two* types. Neither `a.collideWith(b)` nor `b.collideWith(a)` dispatches on both. You need both types to be known at the time the logic executes.

This is called **double dispatch**. Visitor Pattern is the canonical implementation.

---

## The Visitor Structure

```java
// Objects define their accept point
interface GameObject {
    void accept(CollisionVisitor visitor);
}

// The visitor defines behavior for every object combination
interface CollisionVisitor {
    void visit(Ship ship);
    void visit(Station station);
    void visit(Comet comet);
    void visit(Asteroid asteroid);
}
```

Each game object implements `accept`:

```java
class Ship implements GameObject {
    public void accept(CollisionVisitor visitor) {
        visitor.visit(this);  // dispatches to visit(Ship)
    }
}

class Station implements GameObject {
    public void accept(CollisionVisitor visitor) {
        visitor.visit(this);  // dispatches to visit(Station)
    }
}
```

The collision visitor handles each type:

```java
class CollisionHandler implements CollisionVisitor {

    private final GameObject other;

    CollisionHandler(GameObject other) { this.other = other; }

    public void visit(Ship ship) {
        // Called when `other` meets a Ship
        other.accept(new ShipCollisionVisitor(ship));
    }

    public void visit(Station station) {
        other.accept(new StationCollisionVisitor(station));
    }

    // ... etc
}
```

The dispatch chain:
```
a.accept(new CollisionHandler(b))
    → CollisionHandler.visit(a)         // first dispatch — know A's type
        → b.accept(new ACollisionVisitor(a))
            → ACollisionVisitor.visit(b) // second dispatch — know B's type
```

Now both types are known when the logic executes.

---

## What Visitor Buys You

**New operation:** Add a new `Visitor` implementation. No changes to `GameObject` classes.

**New object type:** Add a `visit(NewType)` method to the visitor interface — all visitor implementations are forced to handle it (compiler enforces completeness).

**Separation of concerns:** Game objects don't contain collision logic, rendering logic, or serialization logic. They contain only their state and the single `accept` method.

---

## Back to the Document Service

The document processing story I opened with — three types, four operations, growing in both directions — is exactly where Visitor Pattern earns its complexity.

Here's how it resolved:

```java
// The stable type hierarchy
interface Document {
    void accept(DocumentVisitor visitor);
}

class PdfDocument implements Document {
    public void accept(DocumentVisitor visitor) { visitor.visit(this); }
    // PDF-specific fields
}

class WordDocument implements Document {
    public void accept(DocumentVisitor visitor) { visitor.visit(this); }
}

class HtmlDocument implements Document {
    public void accept(DocumentVisitor visitor) { visitor.visit(this); }
}

// Each operation is a visitor
interface DocumentVisitor {
    void visit(PdfDocument doc);
    void visit(WordDocument doc);
    void visit(HtmlDocument doc);
}

class ValidationVisitor implements DocumentVisitor {
    public void visit(PdfDocument doc)  { validatePdf(doc);  }
    public void visit(WordDocument doc) { validateWord(doc); }
    public void visit(HtmlDocument doc) { validateHtml(doc); }
}

class MetadataExtractor implements DocumentVisitor {
    public void visit(PdfDocument doc)  { extractPdfMetadata(doc);  }
    public void visit(WordDocument doc) { extractWordMetadata(doc); }
    public void visit(HtmlDocument doc) { extractHtmlMetadata(doc); }
}
```

When EPUB arrived two sprints later: add `EpubDocument`, add `visit(EpubDocument)` to the visitor interface — every existing visitor implementation is forced to handle it by the compiler. Four files, five minutes.

When accessibility checking arrived three sprints after that: add `AccessibilityVisitor implements DocumentVisitor`. Zero existing files changed.

**New operation: one new class. New type: one new class + compiler-enforced updates to all visitors.**

Before Visitor: a 3×4 matrix of `processTypeOperation()` methods scattered across four service classes. Every new element touched multiple files.

After Visitor: a 3×4 matrix where rows (operations) are classes and columns (types) are enforced by the interface. Every new element touches one file.

## Adding a New Operation — Step by Step

This is the workflow when requirements arrive:

**Requirement: Add accessibility checking to the document service.**

Step 1: Create the visitor implementation:
```java
class AccessibilityCheckVisitor implements DocumentVisitor {

    private final AccessibilityReport report = new AccessibilityReport();

    public void visit(PdfDocument doc) {
        // Check alt text on images, reading order, tagged PDF
        report.addFindings(pdfAccessibilityChecker.check(doc));
    }

    public void visit(WordDocument doc) {
        // Check heading structure, alt text, contrast
        report.addFindings(wordAccessibilityChecker.check(doc));
    }

    public void visit(HtmlDocument doc) {
        // Check ARIA labels, landmark roles, focus order
        report.addFindings(htmlAccessibilityChecker.check(doc));
    }

    public AccessibilityReport getReport() { return report; }
}
```

Step 2: Use it:
```java
AccessibilityCheckVisitor checker = new AccessibilityCheckVisitor();
document.accept(checker);
AccessibilityReport report = checker.getReport();
```

Step 3: That's it.

No change to `PdfDocument`, `WordDocument`, or `HtmlDocument`. No change to any existing visitor. The new operation is entirely self-contained.

---

## The Trade-Off — When Visitor Hurts

Visitor has a hard constraint: **the type set must be stable**.

Adding a new `GameObject` type means adding a `visit(NewType)` method to every visitor. If you have ten visitors and add one new object type, that's ten files to update.

If your type set changes frequently, Visitor is the wrong tool. The pattern is designed for *stable types + growing operations*, not *growing types + stable operations*.

| Situation | Best Tool |
|-----------|-----------|
| Stable types, growing operations | Visitor |
| Growing types, stable operations | Polymorphism (virtual methods) |
| Small finite matrix, stable | Lookup table (Paper 08) |
| Types grow AND operations grow | Reconsider the domain model |

---

## When to Use Visitor

The right conditions:

1. **Multiple distinct object types** that form a closed hierarchy (you know them all)
2. **Multiple distinct operations** that need to run across that hierarchy
3. **Operations grow faster than types** — you add new visitors, not new types
4. **Operations involve two types simultaneously** (double dispatch, as in collision)

AST (abstract syntax tree) processing is the most common real-world example. A parser produces a fixed set of node types: `BinaryExpression`, `Identifier`, `FunctionCall`, `IfStatement`. Operations — type checking, code generation, optimization, pretty printing — multiply over time. Adding a new operation is a new visitor. The AST node types rarely change.

---

## The Interview Answer

**Question:** When should Visitor Pattern be used?

**Weak answer:** *"When you have many classes."*

**Strong answer:**

*"Visitor addresses the double dispatch problem — when an operation's behavior depends on two types simultaneously, and both type sets are known. The pattern works best when the object hierarchy is stable but operations grow: adding a new visitor is one new class; the objects don't change. The trade-off is adding a new object type forces changes to every existing visitor. If types grow frequently, Visitor is the wrong choice — polymorphism or a lookup table handles that better. The signal for Visitor is a growing number of operations over a closed, stable type hierarchy."*

---

## Visitor in the Real World — Compilers and Parsers

The most common production use of Visitor Pattern is in compilers and language tools.

A Java compiler's AST has a fixed set of node types:
- `ClassDeclaration`
- `MethodDeclaration`
- `BinaryExpression`
- `IfStatement`
- `VariableDeclarator`
- `ReturnStatement`
- (and ~30 more)

Operations multiply:
- Type checking
- Code generation
- Dead code elimination
- Constant folding
- Pretty printing
- Linting rules
- Refactoring suggestions

Adding a new linting rule is one new `Visitor` class. The AST types don't change.

This is why tools like Checkstyle, PMD, SpotBugs, and Sonar all use Visitor Pattern internally. And why JavaParser — the most popular Java AST library — exposes a `VoidVisitorAdapter` for you to extend.

```java
class UnusedVariableDetector extends VoidVisitorAdapter<Void> {
    @Override
    public void visit(VariableDeclarator var, Void arg) {
        if (!isUsed(var)) {
            report("Unused variable: " + var.getNameAsString());
        }
        super.visit(var, arg);
    }
}
```

One class. One linting rule. The entire AST traversal is handled by the visitor infrastructure. You focus on the logic; the pattern handles the traversal.

**If you've ever written a custom SpotBugs detector, a Checkstyle rule, or a JavaParser transformation — you've used Visitor Pattern.**

---

## Key Takeaways

- Visitor solves **double dispatch** — operations depending on two types at once.
- Objects expose `accept()`. Operations live in visitors.
- **Stable types + growing operations** = Visitor wins.
- **Growing types + stable operations** = polymorphism wins.
- **Small finite matrix** = lookup table wins (Paper 08).
- AST processing, document rendering, and game collision engines are real-world domains.

---

*All papers and runnable Java samples: [github.com/pramod0s007/architect-notes](https://github.com/pramod0s007/architect-notes)*

*Previous → Paper 06: Command Pattern | Next → Paper 08: Lookup Tables vs Polymorphism*
