# Storage Factory — Factory Pattern with Environment-Driven Creation

## What This Demonstrates

Factory Pattern applied to storage provider selection. `StorageFactory` maps a
string token (`"s3"`, `"azure"`, `"local"`) to the correct `StorageProvider`
implementation. A second entry point, `createFromEnvironment()`, reads the
`STORAGE_PROVIDER` system property so the selection is fully environment-driven
with no code changes between deployments.

**Pressure: Object Creation Variation** — storage provider varies by environment:
local dev needs the filesystem, CI needs a mock, staging might use Azure, and
production uses S3. Without a factory, every service that uploads a file contains
its own `if (env.equals("prod")) new S3StorageProvider(bucket) else new LocalDiskStorageProvider(path)`
block. When a fourth environment was added (an on-premise staging environment
using NFS), that block had to be changed in 11 services, and 2 were missed,
causing silent write failures in that environment.

## Class Diagram

```
<<interface>>
StorageProvider
+ upload(key: String, data: byte[]): void
+ download(key: String): byte[]
+ delete(key: String): void
        △
        |
   ─────────────────────────────────────────
   |                |                       |
S3StorageProvider  AzureBlobStorageProvider LocalDiskStorageProvider
bucket: String     account+container:String basePath: String
                   (simulated, in-memory)   (real /tmp writes)

StorageFactory
────────────────────────────────────────────────────────────
- DEFAULT_S3_BUCKET        = "my-app-assets"
- DEFAULT_AZURE_ACCOUNT    = "myappstorageaccount"
- DEFAULT_AZURE_CONTAINER  = "assets"
- DEFAULT_LOCAL_BASE_PATH  = "/tmp/local-storage"
────────────────────────────────────────────────────────────
+ create(provider: String): StorageProvider   [static]
   └─ "s3"    → new S3StorageProvider(DEFAULT_S3_BUCKET)
   └─ "azure" → new AzureBlobStorageProvider(account, container)
   └─ "local" → new LocalDiskStorageProvider(DEFAULT_LOCAL_BASE_PATH)
   └─ other   → IllegalArgumentException

+ createFromEnvironment(): StorageProvider   [static]
   └─ reads System.getProperty("STORAGE_PROVIDER", "local")
   └─ delegates to create(provider)
```

## Creation Flow

### Explicit provider selection

```
StorageFactory.create("s3")
   └─ new S3StorageProvider("my-app-assets")   → S3StorageProvider as StorageProvider

StorageFactory.create("azure")
   └─ new AzureBlobStorageProvider("myappstorageaccount", "assets")

StorageFactory.create("local")
   └─ new LocalDiskStorageProvider("/tmp/local-storage")

StorageFactory.create("gcs")
   └─ IllegalArgumentException: Unknown storage provider: 'gcs'. Supported values: s3, azure, local
```

### Environment-driven selection

```
java -DSTORAGE_PROVIDER=s3 Main
   └─ createFromEnvironment() reads "s3"
   └─ delegates to create("s3")
   └─ logs: [StorageFactory] STORAGE_PROVIDER=s3

java Main   (no property)
   └─ createFromEnvironment() defaults to "local"
   └─ writes to /tmp — works everywhere without cloud credentials
```

## Design Decisions

- **`LocalDiskStorageProvider` writes to `/tmp` — actually functional, not a mock** —
  this is a deliberate design choice. Local dev and CI can use real storage
  operations without any cloud credentials or mocked responses. Integration tests
  can verify actual bytes written and read back. The factory makes `/tmp` the
  default, so `createFromEnvironment()` works out of the box on every machine.
- **Factory holds all provider configs as private constants** — callers provide
  only the string token. The S3 bucket name, Azure account, and local path are
  factory concerns. Changing the bucket name requires editing one place, not
  searching every call site.
- **`createFromEnvironment()` defaults to `"local"`** — new environments that
  forget to set `STORAGE_PROVIDER` get local disk storage rather than crashing
  or silently using a production bucket. The default is the safest possible choice.
- **Case-insensitive matching via `toLowerCase()`** — `"S3"`, `"s3"`, and `"S3"` all
  map to the S3 provider. Config files and environment variables have inconsistent
  casing conventions; the factory normalises before mapping.

## How to Run

```bash
cd volume-4-architect-level-thinking/paper-12-factory-pattern/storage-factory
javac *.java && java Main
```

To force a specific provider via system property:

```bash
java -DSTORAGE_PROVIDER=s3 Main
java -DSTORAGE_PROVIDER=azure Main
```

Expected output (abbreviated):

```
=== S3 Storage Provider ===
[S3] Initialized — bucket: my-app-assets
[S3] PUT s3://my-app-assets/uploads/2024/report.txt (21 bytes)
  Verified content: "Hello, Storage World!"
[S3] DELETE s3://my-app-assets/uploads/2024/report.txt

=== Azure Blob Storage Provider ===
[Azure] PUT assets/assets/images/logo.png (21 bytes)
  Verified content: "Hello, Storage World!"

=== Local Disk Storage Provider ===
[Local] WRITE /tmp/local-storage/temp/test-file.txt (21 bytes)
  Verified content: "Hello, Storage World!"

=== From Environment (STORAGE_PROVIDER system property) ===
[StorageFactory] STORAGE_PROVIDER=local

=== Unknown Provider Error ===
Caught expected error: Unknown storage provider: 'gcs'. Supported values: s3, azure, local
```

## When to Apply

- Object creation varies by environment, config, or deployment target.
- The set of variants is stable (or changes rarely), and each variant requires
  its own constructor arguments that callers should not need to know.

## When NOT to Apply

- Only one implementation exists — a direct `new` call is clearer.
- Each call needs a unique, caller-provided configuration rather than a shared
  default — a factory that ignores caller context cannot serve this well.
