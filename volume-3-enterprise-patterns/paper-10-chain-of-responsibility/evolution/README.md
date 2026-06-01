# Evolution: Chain of Responsibility — File Processing Pipeline

Domain: **File Processing Pipeline** (validate → size-check → virus-scan → decompress → parse → encrypt → watermark → index → archive)

## The Forcing Function

A file pipeline starts with 5 stages. Stages get added quarterly.
You have two pipeline variants: external uploads (full chain) and trusted internal files (skip virus scan and encryption).
Expressing "skip a stage" as boolean parameters leaks internal details to every caller.

## Progression

| File | What it shows | The pain |
|------|--------------|----------|
| `v1_MonolithicProcessor.java` | `process()` does all 5 stages inline, ~80 lines | Adding "encrypt" or "watermark" means editing a 80-line method |
| `v2_GrowingPipeline.java` | 4 more stages added; method is ~200 lines, 9 early-return points | `skipVirusScan`, `skipEncryption`, `skipWatermark` boolean params leak stage names into the caller's contract; adding stage 10 = edit the method + add a 4th boolean |
| `v3_ChainApplied.java` | `FileHandler` abstract class; 9 concrete handlers; `PipelineBuilder` assembles them | Adding a stage = 1 new class + 1 builder line; two pipelines share the same handler objects |

## Two Pipelines, Zero Duplication

```
externalUploadPipeline:
  FormatValidation → SizeCheck → VirusScan → Decompress → Parse
  → Encrypt → Watermark → Index → Archive(/archive/external)

trustedInternalPipeline:
  FormatValidation → SizeCheck → Decompress → Parse
  → Watermark → Index → Archive(/archive/internal)
  (no VirusScan, no Encrypt)
```

Same handler classes in both pipelines. No boolean flags.
Callers never know which stages exist.

## When Chain of Responsibility Wins

- Stages are added or reordered frequently
- You need **different pipeline configurations** for different input types
- Each stage must be independently testable
- A stage that short-circuits (returns early on error) should not need to know about its successor

## When It Is Overkill

- You have a fixed 3-stage pipeline that will never change
- Stages are deeply coupled to each other's state (they need to be in one method to share locals)

## Run it

```bash
cd evolution/
javac v1_MonolithicProcessor.java && java v1_MonolithicProcessor
javac v2_GrowingPipeline.java     && java v2_GrowingPipeline
javac v3_ChainApplied.java        && java v3_ChainApplied
```

Or compile all at once:

```bash
javac *.java && java v3_ChainApplied
```

## Key Insight

In v2, "skip virus scan for trusted files" is a boolean parameter.
That means every caller must know that a virus scan exists.
If you rename or remove the stage, every call site breaks.

In v3, the caller builds a pipeline without `VirusScanHandler`.
The handler doesn't exist in that pipeline. No boolean. No coupling.
The pipeline's composition *is* its configuration.
