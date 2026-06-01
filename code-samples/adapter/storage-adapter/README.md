# Adapter Pattern — Storage Adapter

Demonstrates interface incompatibility: your system uses ObjectStorage,
but AWS S3 and Azure Blob use completely different SDKs.
Adapters translate without modifying either side.

## Pressure
Third-party SDKs have incompatible interfaces. You cannot modify them.
Your service needs to swap providers without changing calling code.

## Run
```bash
javac adapter/storageadapter/*.java && java adapter.storageadapter.Main
```

## Key Point
`Main.runDemo()` never changes regardless of which storage provider is active.
Swap providers by swapping the adapter — zero changes to business code.
