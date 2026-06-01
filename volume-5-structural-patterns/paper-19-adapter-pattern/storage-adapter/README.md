# Storage Adapter — Adapter Pattern

## What This Demonstrates

Adapter Pattern applied to cloud storage providers. `S3Client` and `AzureBlobClient`
have completely incompatible APIs. Two adapters — `S3StorageAdapter` and
`AzureBlobStorageAdapter` — translate each SDK's methods to a single `ObjectStorage`
interface. The `runDemo()` method in `Main` calls `upload`, `exists`, `download`,
and `delete` identically for both providers; only the adapter construction changes.

**Pressure: Interface Incompatibility** — the storage service originally used
`S3Client` directly. When the team was asked to support Azure for a European
deployment, `S3Client.putObject(bucket, key, body)` had to be replaced everywhere
with `AzureBlobClient.uploadBlob(container, name, data)`. Neither SDK could be
modified. Without adapters, every call site would need an `if (cloud.equals("azure"))`
branch, and adding a third provider (GCS, MinIO) would touch every site again.

## Interface Translation Table

```
ObjectStorage           S3StorageAdapter              AzureBlobStorageAdapter
─────────────────────   ──────────────────────────   ─────────────────────────────────
upload(key, data)    →  putObject(bucket, key, data)  uploadBlob(container, key, data)
download(key)        →  getObject(bucket, key)         downloadBlob(container, key)
delete(key)          →  deleteObject(bucket, key)      deleteBlob(container, key)
exists(key)          →  objectExists(bucket, key)      blobExists(container, key)
```

## Class Diagram

```
<<interface>>
ObjectStorage
+ upload(key: String, data: byte[]): void
+ download(key: String): byte[]
+ delete(key: String): void
+ exists(key: String): boolean
        △
        |
   ─────────────────────────────────────────────────────────
   |                                                        |
S3StorageAdapter                               AzureBlobStorageAdapter
- client: S3Client                             - client: AzureBlobClient
- bucket: String  ← from S3Client constructor - container: String
upload()   → client.putObject(bucket, key, data) upload() → client.uploadBlob(container,key,data)
download() → client.getObject(bucket, key)        download()→ client.downloadBlob(container,key)
delete()   → client.deleteObject(bucket, key)     delete() → client.deleteBlob(container,key)
exists()   → client.objectExists(bucket, key)     exists() → client.blobExists(container,key)

S3Client (third-party — unmodifiable)      AzureBlobClient (third-party — unmodifiable)
+ putObject(bucket,key,body)               + uploadBlob(container,name,data)
+ getObject(bucket,key)                    + downloadBlob(container,name)
+ deleteObject(bucket,key)                 + deleteBlob(container,name)
+ objectExists(bucket,key)                 + blobExists(container,name)
```

## Sequence Diagram

```
Main                S3StorageAdapter              S3Client
  │                        │                          │
  │ upload("docs/x", data) │                          │
  │───────────────────────>│                          │
  │                        │ putObject("my-app-bucket","docs/x", data)
  │                        │─────────────────────────>│
  │                        │<─────────────────────────│
  │<───────────────────────│                          │
  │                        │                          │
  │ exists("docs/x")       │                          │
  │───────────────────────>│ objectExists("my-app-bucket","docs/x")
  │                        │─────────────────────────>│ true
  │<───────────────────────│ true                     │

  [Same caller code, different adapter:]

Main              AzureBlobStorageAdapter        AzureBlobClient
  │                        │                          │
  │ upload("docs/x", data) │                          │
  │───────────────────────>│                          │
  │                        │ uploadBlob("my-app-container","docs/x",data)
  │                        │─────────────────────────>│
```

## Design Decisions

- **Both adapters hold their client and the container/bucket name as constructor
  arguments** — callers provide only the key. Where the file lives (which bucket
  or container) is an infrastructure detail that the adapter encapsulates. The
  caller writes `storage.upload("docs/readme.txt", data)` whether the target is
  S3, Azure, or local disk.
- **Adapters do not add business logic** — they translate, they do not transform.
  `S3StorageAdapter.upload()` maps arguments and delegates; it does not add
  compression, encryption, or retry. Those cross-cutting concerns belong to
  Decorator wrappers stacked on top of the `ObjectStorage` interface.
- **`runDemo()` in `Main` is a static method that accepts `ObjectStorage`** — this
  single method exercises all four interface methods identically for both providers.
  It will never change as new providers are added.
- **Third-party SDKs are simulated with in-memory implementations** — `S3Client`
  and `AzureBlobClient` use `HashMap` internally. The adapter's structural role is
  clear without cloud infrastructure or credentials.

## How to Run

```bash
cd volume-5-structural-patterns/paper-19-adapter-pattern/storage-adapter
javac adapter/storageadapter/*.java && java adapter.storageadapter.Main
```

Expected output:

```
=== Using S3 backend ===
[S3] putObject bucket=my-app-bucket key=docs/readme.txt (23 bytes)
Exists: true
[S3] getObject bucket=my-app-bucket key=docs/readme.txt
Downloaded: Hello, Adapter Pattern!
[S3] deleteObject bucket=my-app-bucket key=docs/readme.txt
After delete, exists: false

=== Switching to Azure — caller code unchanged ===
[Azure] uploadBlob container=my-app-container name=docs/readme.txt (23 bytes)
Exists: true
Downloaded: Hello, Adapter Pattern!
After delete, exists: false
```

## When to Apply

- Two or more third-party SDKs or legacy systems expose the same conceptual
  operations with different method names and signatures, and neither can be
  modified.
- Your system needs to be portable across providers (cloud portability,
  on-premise vs cloud).

## When NOT to Apply

- Only one provider will ever be used and no portability requirement exists —
  using the SDK directly avoids the indirection.
- The conceptual mismatch is too deep to paper over with translation — if one
  SDK is fundamentally event-driven and the other is synchronous, an adapter
  hiding that difference creates more confusion than it solves.
